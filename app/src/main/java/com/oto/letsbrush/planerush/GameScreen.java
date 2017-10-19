package com.oto.letsbrush.planerush;

import android.util.Log;

import com.oto.letsbrush.planerush.World.WorldListener;
import com.oto.letsbrush.planerush.framework.BluetoothInput;
import com.oto.letsbrush.planerush.framework.Game;
import com.oto.letsbrush.planerush.framework.Input.TouchEvent;
import com.oto.letsbrush.planerush.gl.Camera2D;
import com.oto.letsbrush.planerush.gl.SpriteBatcher;
import com.oto.letsbrush.planerush.implementation.BluetoothGame;
import com.oto.letsbrush.planerush.implementation.BluetoothScreen;
import com.oto.letsbrush.planerush.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class GameScreen extends BluetoothScreen {

    static final float FRUSTUM_WIDTH = 18;
    static final float FRUSTUM_HEIGHT = 12;

    static final int GAME_READY = 0;
    static final int GAME_RUNNING = 1;
    static final int GAME_PAUSED = 2;
    static final int GAME_OVER = 3;

    int state;
    Camera2D guiCam;
    Camera2D actionCam;
    Vector2 touchPoint;
    SpriteBatcher batcher;
    World world;
    WorldListener worldListener;
    WorldRenderer renderer;
    int lastScore;
    String scoreString;
    List<BluetoothInput.BluetoothEvent> events;

    public GameScreen(Game game){
        super(game);
        state = GAME_READY;
        guiCam = new Camera2D(glGraphics, 480, 320);
        actionCam = new Camera2D(glGraphics, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
        touchPoint = new Vector2();
        batcher = new SpriteBatcher(glGraphics, 100);
        worldListener = new WorldListener() {
            @Override
            public void star() {
                if(Settings.soundEnabled){
                    Assets.playSound(Assets.starSound);
                }
            }

            @Override
            public void planeFly(){
            }

            @Override
            public void rockHit(){
            }
        };
        world = new World(worldListener);
        renderer = new WorldRenderer(glGraphics, batcher, world);
        lastScore = 0;
        scoreString = "0";

        events = new ArrayList<>();
    }

    @Override
    public void update(float deltaTime){
        //if(deltaTime > 0.1f)
         //   deltaTime = 0.1f;

        switch(state){
            case GAME_READY:
                updateReady();
                break;
            case GAME_RUNNING:
                updateRunning(deltaTime);
                break;
            case GAME_OVER:
                updateGameOver(deltaTime);
                break;
        }
    }

    private void updateReady(){
        ((BluetoothGame)game).getBluetoothEvents();
        if(game.getInput().getTouchEvents().size() > 0) {
            state = GAME_RUNNING;
        }
    }

    private void updateRunning(float deltaTime){
        game.getInput().getTouchEvents();
        game.getInput().getKeyEvents();

        events.clear();
        events.addAll(((BluetoothGame)game).getBluetoothEvents());

        int size = events.size();

        if(size > 4){
            world.update(deltaTime, events.subList((events.size() - 4), (events.size() - 1 )));
        }
        else {
            world.update(deltaTime);
        }

        /*try {
            world.update(deltaTime, events.subList((events.size() - 4), (events.size() - 1 )));
        }
        catch(ArrayIndexOutOfBoundsException e){
            world.update(deltaTime);
            Log.d("Exception", "something 1");
        }
        catch(IndexOutOfBoundsException e){
            world.update(deltaTime);
            Log.d("Exception", "something 2");
        }*/

        if(world.score != lastScore) {
            lastScore = world.score;
            scoreString = "" + lastScore;
            Log.d("String", scoreString);
        }

        if(world.plane.position.x > World.World_Width - World.Screen_Width * .5f){
            state = GAME_OVER;
        }
    }

    private void updateGameOver(float deltaTime){
        game.getInput().getKeyEvents();
        ((BluetoothGame)game).getBluetoothEvents();

        world.plane.state = Plane.PLANE_STATE_CENTER;

        world.update(deltaTime);

        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        game.getInput().getKeyEvents();

        int len = touchEvents.size();
        for(int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if(event.type != TouchEvent.TOUCH_UP)
                continue;
            game.setScreen(new MainMenu(game));
        }

    }

    @Override
    public void present(float deltaTime){
        GL10 gl = glGraphics.getGL();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        renderer.render();

        guiCam.setViewportAndMatrices();
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        batcher.beginBatch(Assets.Atlas);
        switch(state) {
            case GAME_READY:
                presentReady();
                break;
            case GAME_RUNNING:
                presentRunning();
                break;
            case GAME_OVER:
                presentGameOver();
                break;
        }
        batcher.endBatch();
        gl.glDisable(GL10.GL_BLEND);
    }

    private void presentReady(){
        batcher.drawSprite(240, 160, 280, 80, Assets.getReady);
        drawScore();
    }

    private void presentRunning(){
        drawScore();
    }

    private void presentGameOver(){
        batcher.drawSprite(240, 160, 280, 80, Assets.gameOver);
        drawScore();
    }

    private void drawScore(){
        int len = scoreString.length();
        int lastWidth = 0;
        for(int i = 0; i<len; i++){
            switch(scoreString.charAt(i)){
                case '0':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(0));
                    lastWidth += 25;
                    break;
                case '1':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(1));
                    lastWidth += 25;
                    break;
                case '2':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(2));
                    lastWidth += 25;
                    break;
                case '3':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(3));
                    lastWidth += 25;
                    break;
                case '4':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(4));
                    lastWidth += 25;
                    break;
                case '5':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(5));
                    lastWidth += 25;
                    break;
                case '6':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(6));
                    lastWidth += 25;
                    break;
                case '7':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(7));
                    lastWidth += 25;
                    break;
                case '8':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(8));
                    lastWidth += 25;
                    break;
                case '9':
                    batcher.drawSprite(lastWidth + 13, 320 - 13, 25, 25, Assets.numbers.get(9));
                    lastWidth += 25;
                    break;
            }
        }
    }

    @Override
    public void pause() {
        if(state == GAME_RUNNING)
            state = GAME_PAUSED;
        if(Settings.soundEnabled) {
            if (Assets.gameTheme.isPlaying())
                Assets.gameTheme.stop();
        }
    }

    @Override
    public void resume() {
        if(state == GAME_PAUSED)
            state = GAME_RUNNING;
        if(Settings.soundEnabled){
                Assets.gameTheme.play();
        }

    }

    @Override
    public void dispose() {
        if(Settings.soundEnabled) {
            if (Assets.gameTheme.isPlaying())
                Assets.gameTheme.stop();
        }
    }
}
