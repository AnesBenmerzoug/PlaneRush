package com.oto.letsbrush.planerush;

import com.oto.letsbrush.planerush.framework.Game;
import com.oto.letsbrush.planerush.gl.Camera2D;
import com.oto.letsbrush.planerush.gl.SpriteBatcher;
import com.oto.letsbrush.planerush.gl.Texture;
import com.oto.letsbrush.planerush.gl.TextureRegion;
import com.oto.letsbrush.planerush.implementation.BluetoothScreen;
import com.oto.letsbrush.planerush.implementation.GLScreen;

import javax.microedition.khronos.opengles.GL10;

public class LoadingScreen2 extends BluetoothScreen {
    Camera2D guiCam;
    SpriteBatcher batcher;
    Texture letsbrush;
    TextureRegion letsbrushRegion;
    final float DURATION = 3.0f;
    float time;

    public LoadingScreen2(Game game){
        super(game);
        guiCam = new Camera2D(glGraphics, 480, 320);
        batcher = new SpriteBatcher(glGraphics, 10);
    }

    @Override
    public void resume(){
        letsbrush = new Texture(glGame, "letsbrush.png");
        letsbrushRegion = new TextureRegion(letsbrush, 0, 0, letsbrush.width, letsbrush.height);
    }

    @Override
    public void pause(){
        letsbrush.dispose();
    }

    @Override
    public void update(float deltaTime){
        game.getInput().getTouchEvents();
        game.getInput().getKeyEvents();
        time += deltaTime;
        if(time >= DURATION){
            GL10 gl = glGraphics.getGL();
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            game.setScreen(new MainMenu(game));
        }
    }

    @Override
    public void present(float deltaTime){
        GL10 gl = glGraphics.getGL();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        guiCam.setViewportAndMatrices();

        gl.glEnable(GL10.GL_TEXTURE_2D);

        batcher.beginBatch(letsbrush);

        batcher.drawSprite(240, 160, 480, 320, letsbrushRegion);

        batcher.endBatch();
    }

    @Override
    public void dispose() {
    }
}
