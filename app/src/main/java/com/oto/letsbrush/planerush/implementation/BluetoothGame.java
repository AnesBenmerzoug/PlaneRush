package com.oto.letsbrush.planerush.implementation;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;

import com.oto.letsbrush.planerush.framework.Audio;
import com.oto.letsbrush.planerush.framework.BluetoothInput;
import com.oto.letsbrush.planerush.framework.FileIO;
import com.oto.letsbrush.planerush.framework.Game;
import com.oto.letsbrush.planerush.framework.Graphics;
import com.oto.letsbrush.planerush.framework.Input;
import com.oto.letsbrush.planerush.framework.Pool;
import com.oto.letsbrush.planerush.framework.Pool.PoolObjectFactory;
import com.oto.letsbrush.planerush.framework.Screen;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class BluetoothGame extends Activity implements Game, Renderer {
    enum BluetoothGameState {
        Initialized,
        Running,
        Paused,
        Finished,
        Idle
    }

    GLSurfaceView glView;
    GLGraphics glGraphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    BluetoothGameState state = BluetoothGameState.Initialized;

    BluetoothAdapter bluetoothAdapter;

    BluetoothDevice arduino;

    public AndroidBluetooth bluetooth;

    public boolean connected;

    final MyHandler handler = new MyHandler(this);

    DataThread dataThread;

    String readString;

    Pool<BluetoothInput.BluetoothEvent> BluetoothPool;

    List<BluetoothInput.BluetoothEvent> bluetoothEvents;

    Object stateChanged = new Object();
    long startTime = System.nanoTime();
    PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        glView = new GLSurfaceView(this);
        glView.setRenderer(this);
        setContentView(glView);

        glGraphics = new GLGraphics(glView);
        fileIO = new AndroidFileIO(this);
        audio = new AndroidAudio(this);
        input = new AndroidInput(this, glView, 1, 1);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "BluetoothGame lock");

        BluetoothPool = new Pool <> (new Pool.PoolObjectFactory<BluetoothInput.BluetoothEvent>() {
            @Override
            public BluetoothInput.BluetoothEvent createObject() {
                return new BluetoothInput.BluetoothEvent();
            }
        }, 200);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            finish();
        }

        readString = "";

        arduino = null;

        connected = false;

        bluetooth = new AndroidBluetooth(this, handler);

        bluetoothEvents = new ArrayList<>();

        dataThread = new DataThread();

        dataThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();
        connect(arduino);
        glView.onResume();
        wakeLock.acquire();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glGraphics.setGL(gl);

        synchronized (stateChanged) {
            if (state == BluetoothGameState.Initialized)
                screen = getStartScreen();
            state = BluetoothGameState.Running;
            screen.resume();
            startTime = System.nanoTime();
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    public void onDrawFrame(GL10 gl) {
        BluetoothGameState state = null;

        synchronized (stateChanged) {
            state = this.state;
        }

        if (state == BluetoothGameState.Running) {
            float deltaTime = (System.nanoTime() - startTime) / 1000000000.0f;
            startTime = System.nanoTime();

            screen.update(deltaTime);

            screen.present(deltaTime);
        }

        if (state == BluetoothGameState.Paused) {
            screen.pause();
            synchronized (stateChanged) {
                this.state = BluetoothGameState.Idle;
                stateChanged.notifyAll();
            }
        }

        if (state == BluetoothGameState.Finished) {
            screen.pause();
            screen.dispose();
            synchronized (stateChanged) {
                this.state = BluetoothGameState.Idle;
                stateChanged.notifyAll();
            }
        }
    }

    @Override
    public void onPause() {
        synchronized (stateChanged) {
            if (isFinishing()) {
                state = BluetoothGameState.Finished;

                dataThread.cancel();
                dataThread = null;

                if (bluetoothAdapter.isEnabled()) {
                    bluetooth.stop();
                }
            } else {
                state = BluetoothGameState.Paused;
                bluetooth.stop();
            }

            while (true) {
                try {
                    stateChanged.wait();
                    break;
                } catch (InterruptedException e) {
                }
            }
        }
        wakeLock.release();
        glView.onPause();
        super.onPause();
    }

    public GLGraphics getGLGraphics() {
        return glGraphics;
    }

    public Input getInput() {
        return input;
    }

    public FileIO getFileIO() {
        return fileIO;
    }

    public Graphics getGraphics() {
        throw new IllegalStateException("We are using OpenGL!");
    }

    public Audio getAudio() {
        return audio;
    }

    public void setScreen(Screen newScreen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");
        this.screen.pause();
        this.screen.dispose();
        newScreen.resume();
        newScreen.update(0);
        this.screen = newScreen;
    }

    public Screen getCurrentScreen() {
        return screen;
    }

    public void connect(BluetoothDevice arduino) {
        if (bluetooth.getState() != AndroidBluetooth.State.CONNECTED) {
            if (arduino != null) {
                bluetooth.connect(arduino);
            } else {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().equals("HC-06")) {
                            arduino = device;
                            bluetooth.connect(arduino);
                        }
                    }
                }
            }
        }
    }

    public List<BluetoothInput.BluetoothEvent> getBluetoothEvents() {
        synchronized (bluetoothEvents) {
            int len = bluetoothEvents.size();
            for( int i = 0; i < len; i++ )
                BluetoothPool.free(bluetoothEvents.get(i));
            List<BluetoothInput.BluetoothEvent> events = new ArrayList<BluetoothInput.BluetoothEvent>();
            events.addAll(bluetoothEvents);
            bluetoothEvents.clear();
            return events;
        }
    }


    private class DataThread extends Thread {
        String cachedString;
        String[] accels;
        char lastOne;
        boolean run = true;
        boolean first, second, third;

        public DataThread(){
            cachedString = "";
            accels = new String[]{"", "", ""};
            lastOne = ' ';
            first = second = third = false;
        }

        public void run(){
            BluetoothInput.BluetoothEvent event;
            synchronized (readString) {
                while(run) {
                    int i;
                    int j = 0;
                    char c;

                    int len = readString.length();
                    for(i = 0; i < len && run; i++){
                        c = readString.charAt(i);
                        if(c == 'x'){
                            j = i;
                            lastOne = 'x';
                            accels[0] = "";
                            first =  ( !first & !second & !third ) | first;
                            second = ( first & !second & !third ) | ( second & second );
                            third = ( first & second & !third)  | ( third & second & first );
                        }
                        else if(c == 'y'){
                            j = i;
                            lastOne = 'y';
                            accels[1] = "";
                            first =  ( !first & !second & !third ) | first;
                            second = ( first & !second & !third ) | ( second & second );
                            third = ( first & second & !third)  | ( third & second & first );
                        }
                        else if(c == 'z'){
                            j = i;
                            lastOne = 'z';
                            accels[2] = "";
                            first =  ( !first & !second & !third ) | first;
                            second = ( first & !second & !third ) | ( second & second );
                            third = ( first & second & !third)  | ( third & second & first );
                        }
                        else {
                            switch(lastOne){
                                case 'x':
                                    accels[0] += c;
                                    break;
                                case 'y':
                                    accels[1] += c;
                                    break;
                                case 'z':
                                    accels[2] += c;
                                    break;
                            }
                        }
                    }
                    if(first & second & third) {
                        readString = readString.substring(j);

                        try {
                            event = BluetoothPool.newObject();
                            event.roll = Integer.parseInt(accels[0]);
                            event.pitch = Integer.parseInt(accels[1]);
                            event.yaw = Integer.parseInt(accels[2]);
                            synchronized (bluetoothEvents) {
                                bluetoothEvents.add(event);
                            }
                        } catch (NumberFormatException e) {

                        } catch (NullPointerException e){

                        }

                    }
                    first = second = third = false;
                }
            }
        }

        public void cancel(){
            run = false;
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<BluetoothGame> mActivity;

        public MyHandler(BluetoothGame game){
            mActivity = new WeakReference<BluetoothGame>(game);
        }

        @Override
        public void handleMessage(Message message){
            BluetoothGame game = mActivity.get();
            if(game == null)
                return;
            switch(message.what){
                case AndroidBluetooth.MessageType_State:
                    if(game.bluetooth.getState() == AndroidBluetooth.State.CONNECTED){
                        game.connected = true;
                    }
                    break;
                case AndroidBluetooth.MessageType_Read:
                    game.readString += (new String((byte[])message.obj)).substring(0, message.arg1);
                    break;
            }
        }
    }
}
