package com.oto.letsbrush.planerush.framework;


import java.util.List;

public interface BluetoothInput {
    public static class BluetoothEvent {

        public BluetoothEvent(int roll, int pitch, int yaw){
            this.roll = roll;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public BluetoothEvent(){
            this.roll = 0;
            this.pitch = 0;
            this.yaw = 0;
        }

        public int roll;
        public int pitch;
        public int yaw;

    }

    public int getRoll();

    public int getPitch();

    public int getYaw();

    public List<BluetoothEvent> getBluetoothEvents();
}
