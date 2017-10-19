package com.oto.letsbrush.planerush;


import com.oto.letsbrush.planerush.framework.BluetoothInput;
import com.oto.letsbrush.planerush.math.OverlapTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {
    public interface WorldListener {
        public void star();
        public void planeFly();
        public void rockHit();
    }

    public static final float Screen_Width = 18;
    public static final float World_Width = Screen_Width * 10;
    public static final float World_Height = 12;

    public static final int WORLD_STATE_RUNNING = 0;
    public static final int WORLD_STATE_GAME_OVER = 1;

    public final Plane plane;
    public final List<Star> stars;

    public final WorldListener worldListener;
    public final Random random;

    public int state;
    public int score;

    public World(WorldListener listener){
        this.worldListener = listener;
        this.plane = new Plane(2, World_Height / 2);
        this.stars = new ArrayList<Star>();
        random = new Random();
        generateLevel();

        this.state = WORLD_STATE_RUNNING;
        this.score = 0;
    }

    private void generateLevel(){
        float x = 5 * Plane.Plane_Width;
        Star star;
        while(x < 1.2f * World_Width){
            switch(Math.abs(random.nextInt() % 3)){
                case 0:
                    star = new Star(x, World_Height * 1/ 5.0f);
                    stars.add(star);

                    x += Plane.Plane_Width;
                    star = new Star(x, World_Height * 1 / 5.0f);
                    stars.add(star);

                    x += Plane.Plane_Width;
                    star = new Star(x, World_Height * 1 / 5.0f);
                    stars.add(star);
                    break;
                case 1:
                    star = new Star(x, World_Height * .5f);
                    stars.add(star);

                    x += Plane.Plane_Width;
                    star = new Star(x, World_Height * .5f);
                    stars.add(star);

                    x += Plane.Plane_Width;
                    star = new Star(x, World_Height * .5f);
                    stars.add(star);
                    break;
                case 2:
                    star = new Star(x, World_Height * 4 / 5.0f);
                    stars.add(star);

                    x += Plane.Plane_Width;
                    star = new Star(x, World_Height * 4 / 5.0f);
                    stars.add(star);

                    x += Plane.Plane_Width;
                    star = new Star(x, World_Height * 4 / 5.0f);
                    stars.add(star);
                    break;
            }

            x += World_Height * 2.0f / 3;
        }
    }

    /*public void update(float deltaTime, Vector2 touch){
        if(touch.y != World_Height) {
            if (touch.y > World_Height * 3 / 5.0f) {
                plane.state = Plane.PLANE_STATE_UP;
            } else if (touch.y < World_Height * 2 / 5.0f) {
                plane.state = Plane.PLANE_STATE_DOWN;
            } else plane.state = Plane.PLANE_STATE_CENTER;
        }

        updatePlane(deltaTime);
        updateStars(deltaTime);
        checkCollisions();
    }*/

    public void update(float deltaTime){
        updatePlane(deltaTime);
        updateStars(deltaTime);
        checkCollisions();
    }

    public void update(float deltaTime, List<BluetoothInput.BluetoothEvent> events){
        int len = events.size();

        for(int i = 0; i < len; i++){
            if(events.get(i).roll > 25) {
                plane.state = Plane.PLANE_STATE_UP;
            }
            else {
                if(events.get(i).roll < -25) {
                    plane.state = Plane.PLANE_STATE_DOWN;
                }
                else {
                    plane.state = Plane.PLANE_STATE_CENTER;
                }
            }

            updatePlane(deltaTime);
            updateStars(deltaTime);
            checkCollisions();
            checkGameOver();
        }

    }

    private void updatePlane(float deltaTime){
        if(plane.position.x < World_Width + World_Height / 2 + Screen_Width / 3){
            plane.update(deltaTime);
        }
    }

    private void updateStars(float deltaTime){
        int len = stars.size();
        for(int i = 0; i < len; i++){
            Star star = stars.get(i);
            star.update(deltaTime);
            if(plane.position.x < World_Width - .5f * Screen_Width) {
                if (star.position.x + Star.Star_Width < plane.position.x - 3) {
                    stars.remove(star);
                    len = stars.size();
                }
            }
            else if(star.position.x + Star.Star_Width < World_Width - Screen_Width){
                stars.remove(star);
                len = stars.size();
            }
        }
    }

    private void checkCollisions(){
        checkStarCollision();
    }

    private void checkStarCollision(){
        int len = stars.size();
        for(int i = 0; i < len; i++){
            Star star = stars.get(i);
            if(OverlapTester.overlapRectangles(plane.bounds, star.bounds)){
                stars.remove(star);
                worldListener.star();
                score += Star.Star_Score;
                len = stars.size();
            }
        }
    }

    private void checkGameOver(){
        if(plane.position.x > World_Width - Screen_Width * .5f){
            state = WORLD_STATE_GAME_OVER;
        }
    }
}
