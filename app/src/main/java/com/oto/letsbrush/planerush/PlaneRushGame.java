package com.oto.letsbrush.planerush;

import com.oto.letsbrush.planerush.framework.Screen;
import com.oto.letsbrush.planerush.implementation.BluetoothGame;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlaneRushGame extends BluetoothGame {
    boolean firstTimeCreate = true;

    @Override
    public Screen getStartScreen(){
        return new LoadingScreen1(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        if(firstTimeCreate) {
            //Settings.load(getFileIO());
            Assets.load(this);
            firstTimeCreate = false;
        } else {
            Assets.reload();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getCurrentScreen().pause();
        if(Settings.soundEnabled)
            if(Assets.mainTheme.isPlaying())
                Assets.mainTheme.stop();
    }
}
