package com.oto.letsbrush.planerush;

import com.oto.letsbrush.planerush.framework.Game;
import com.oto.letsbrush.planerush.framework.Input.TouchEvent;
import com.oto.letsbrush.planerush.gl.Camera2D;
import com.oto.letsbrush.planerush.gl.SpriteBatcher;
import com.oto.letsbrush.planerush.implementation.BluetoothGame;
import com.oto.letsbrush.planerush.implementation.BluetoothScreen;
import com.oto.letsbrush.planerush.implementation.GLScreen;
import com.oto.letsbrush.planerush.math.OverlapTester;
import com.oto.letsbrush.planerush.math.Rectangle;
import com.oto.letsbrush.planerush.math.Vector2;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class MainMenu extends BluetoothScreen {
    Camera2D guiCam;
    SpriteBatcher batcher;
    Rectangle playBounds;
    Vector2 touchPoint;
    boolean buttonDown;

    public MainMenu(Game game){
        super(game);
        guiCam = new Camera2D(glGraphics, 480, 320);
        batcher = new SpriteBatcher(glGraphics, 10);
        playBounds = new Rectangle(240 - 70, 130 - 18, 140, 35);
        touchPoint = new Vector2();
        buttonDown = false;
    }

    @Override
    public void resume(){
        if(Settings.soundEnabled) {
            Assets.mainTheme.play();
        }
    }

    @Override
    public void pause(){

        Settings.save(game.getFileIO());
        if(Settings.soundEnabled) {
            if (Assets.mainTheme.isPlaying())
                Assets.mainTheme.stop();
        }
    }

    @Override
    public void update(float deltaTime){
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        game.getInput().getKeyEvents();

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if(event.type == TouchEvent.TOUCH_UP) {
                touchPoint.set(event.x, event.y);
                guiCam.touchToWorld(touchPoint);

                buttonDown = false;

                if (OverlapTester.pointInRectangle(playBounds, touchPoint)) {
                    if(((BluetoothGame)game).connected) {
                        String start = "s";
                        ((BluetoothGame)game).bluetooth.write(start.getBytes());
                        game.setScreen(new GameScreen(game));
                    }
                }

            }
            if(event.type == TouchEvent.TOUCH_DOWN){
                touchPoint.set(event.x, event.y);
                guiCam.touchToWorld(touchPoint);

                if (OverlapTester.pointInRectangle(playBounds, touchPoint)) {
                    Assets.playSound(Assets.clickSound);
                    buttonDown = true;
                }
                else{
                    buttonDown = false;
                }
            }
        }
    }

    @Override
    public void present(float deltaTime){
        GL10 gl = glGraphics.getGL();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        guiCam.setViewportAndMatrices();

        gl.glEnable(GL10.GL_TEXTURE_2D);

        batcher.beginBatch(Assets.Atlas);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        batcher.drawSprite(240, 160, 480, 320, Assets.backgroundRegion);

        if(buttonDown){
            batcher.drawSprite(244, 127, 160, 38, Assets.buttonDown);
            batcher.drawSprite(240, 126, 140, 35, Assets.play);
        }
        else {
            batcher.drawSprite(240, 130, 160, 40, Assets.buttonUp);
            batcher.drawSprite(240, 130, 140, 35, Assets.play);
        }

        batcher.endBatch();

        gl.glDisable(GL10.GL_BLEND);
    }

    @Override
    public void dispose() {
        if(Settings.soundEnabled)
            if(Assets.mainTheme.isPlaying())
                Assets.mainTheme.stop();
    }
}
