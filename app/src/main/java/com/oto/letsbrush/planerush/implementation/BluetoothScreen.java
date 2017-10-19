package com.oto.letsbrush.planerush.implementation;

import com.oto.letsbrush.planerush.framework.Game;
import com.oto.letsbrush.planerush.framework.Screen;

public abstract class BluetoothScreen extends Screen {
    protected final GLGraphics glGraphics;
    protected final BluetoothGame glGame;

    public BluetoothScreen(Game game) {
        super(game);
        glGame = (BluetoothGame)game;
        glGraphics = glGame.getGLGraphics();
    }
}
