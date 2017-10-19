package com.oto.letsbrush.planerush.implementation;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.view.WindowManager;

import com.oto.letsbrush.planerush.framework.Audio;
import com.oto.letsbrush.planerush.framework.FileIO;
import com.oto.letsbrush.planerush.framework.Game;
import com.oto.letsbrush.planerush.framework.Graphics;
import com.oto.letsbrush.planerush.framework.Input;
import com.oto.letsbrush.planerush.framework.Screen;


public abstract class AndroidGame extends Activity implements Game {
    AndroidFastRenderView renderView;
    Graphics graphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    WakeLock wakeLock;
    BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        int frameBufferWidth = isLandscape?480:320;
        int frameBufferHeight = isLandscape?320:480;

        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.RGB_565);

        float scaleX = (float) frameBufferWidth / getWindowManager().getDefaultDisplay().getWidth();
        float scaleY = (float) frameBufferHeight / getWindowManager().getDefaultDisplay().getHeight();

        renderView = new AndroidFastRenderView(this, frameBuffer);
        fileIO = new AndroidFileIO(this);
        input =  new AndroidInput(this, renderView, scaleX, scaleY);
        audio = new AndroidAudio(this);
        screen = getStartScreen();
        graphics = new AndroidGraphics(getAssets(), frameBuffer);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setContentView(renderView);

        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "my game lock");
    }

    @Override
    protected void onResume(){
        super.onResume();
        wakeLock.acquire();
        screen.resume();
        renderView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        wakeLock.release();
        renderView.pause();
        screen.pause();

        if(isFinishing())
            screen.dispose();
    }

    public Input getInput(){
        return input;
    }

    public FileIO getFileIO(){
        return fileIO;
    }

    public Graphics getGraphics(){
        return graphics;
    }

    public Audio getAudio(){
        return audio;
    }

    public void setScreen(Screen screen){
        if(screen == null)
            throw new IllegalArgumentException("Screen must not be null");

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }

    public Screen getCurrentScreen(){
        return screen;
    }
}
