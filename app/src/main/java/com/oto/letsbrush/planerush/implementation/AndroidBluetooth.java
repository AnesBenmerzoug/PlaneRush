package com.oto.letsbrush.planerush.implementation;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class AndroidBluetooth {

    public static final int MessageType_State = 0;
    public static final int MessageType_Read = 1;
    public static final int MessageType_Write = 2;
    public static final int MessageType_Device = 3;
    public static final int MessageType_Notify = 4;

    public enum State {
        NONE,
        LISTEN,
        CONNECTING,
        CONNECTED
    }

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothTest";
    // Unique UUID for this application
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private State mState;
    private Context mContext;

    public AndroidBluetooth(Context context, Handler handler){
        mContext = context;
        mHandler = handler;
        mState = State.NONE;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private synchronized void setState(State state){
        mState = state;

        mHandler.obtainMessage(AndroidBluetooth.MessageType_State, -1, -1, state).sendToTarget();
    }

    public synchronized State getState(){
        return mState;
    }

    public synchronized void start() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;}

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(State.LISTEN);
    }

    public synchronized void connect(BluetoothDevice device){
        if(mState == State.CONNECTING){
            if(mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(State.CONNECTING);
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device){

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        mHandler.obtainMessage(AndroidBluetooth.MessageType_Device, -1, -1, device.getName()).sendToTarget();
        setState(State.CONNECTED);
    }

    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(State.NONE);
    }

    public void write(byte[] out){
        ConnectedThread r;

        synchronized (this){
            if(mState != State.CONNECTED)
                return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    public void sendErrorMessage(int messageId){
        setState(State.LISTEN);
        mHandler.obtainMessage(AndroidBluetooth.MessageType_Notify, -1, -1,
                mContext.getResources().getString(messageId)).sendToTarget();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, SPP_UUID);
            }
            catch (IOException e){
            }
            mmServerSocket = tmp;
        }

        public void run(){
            setName("AcceptThread");
            BluetoothSocket socket = null;

            while(mState != AndroidBluetooth.State.CONNECTED){
                try {
                    socket = mmServerSocket.accept();
                }
                catch (IOException e){
                    break;
                }
            }

            if(socket != null){
                synchronized (AndroidBluetooth.this){
                    switch(mState){
                        case LISTEN:
                        case CONNECTING:
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case NONE:
                        case CONNECTED:
                            try {
                                socket.close();
                            }
                            catch (IOException e){

                            }
                            break;
                    }
                }
            }
        }

        public void cancel(){
            try {
                mmServerSocket.close();
            }
            catch (IOException e){

            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device){
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
            }
            catch (IOException e){

            }
            mmSocket = tmp;
        }

        public void run(){
            setName("ConnectThread");

            mAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            }
            catch (IOException e){
                try {
                    mmSocket.close();
                }
                catch (IOException g){

                }
                connect(mmDevice);
                return;
            }

            synchronized(AndroidBluetooth.this){
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel(){
            try{
                mmSocket.close();
            }
            catch (IOException e){

            }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes = 0;

            while(true){
                try {
                    bytes = mmInStream.read(buffer, bytes, buffer.length - bytes);
                    /*if(bytes >= 12) {
                        mHandler.obtainMessage(AndroidBluetooth.MessageType_Read, bytes, -1, buffer).sendToTarget();
                        bytes = 0;
                    }*/
                    mHandler.obtainMessage(AndroidBluetooth.MessageType_Read, bytes, -1, buffer).sendToTarget();
                    bytes = 0;
                }
                catch (IOException e){
                    break;
                }
            }
        }

        public void write(byte[] buffer){
            try {
                mmOutStream.write(buffer);
                mHandler.obtainMessage(AndroidBluetooth.MessageType_Write, -1, -1,
                        buffer).sendToTarget();
            }
            catch (IOException e){

            }
        }

        public void cancel(){
            try {
                mmSocket.close();
            }
            catch (IOException e){

            }
        }
    }
}
