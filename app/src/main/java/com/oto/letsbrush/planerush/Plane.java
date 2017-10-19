package com.oto.letsbrush.planerush;

import android.util.Log;

import com.oto.letsbrush.planerush.framework.DynamicGameObject;
import com.oto.letsbrush.planerush.math.Vector2;

public class Plane extends DynamicGameObject {
    public static final int PLANE_STATE_UP = 1;
    public static final int PLANE_STATE_CENTER = 0;
    public static final int PLANE_STATE_DOWN = -1;

    public static final float PLANE_VELOCITY_Y = 13.0f;
    public static final float PLANE_VELOCITY_X = 2.0f;

    public static final float Plane_Width = 2.0f;
    public static final float Plane_Height = 1.5f;

    public final float NORM_FACTOR = 6.0f;

    int state;
    float stateTime;

    float error;

    public Plane(float x, float y){
        super(x, y, Plane_Width, Plane_Height);
        state = PLANE_STATE_CENTER;
        stateTime = 0;
        error = 0;
    }

    public void update(float deltaTime){
        if(state == PLANE_STATE_CENTER){
            error = 0.5f * World.World_Height - position.y;
            if(position.y < 0.5f * World.World_Height){
                position.add(PLANE_VELOCITY_X * deltaTime, PLANE_VELOCITY_Y * (error)/NORM_FACTOR * deltaTime);
            }
            else{
                position.add(PLANE_VELOCITY_X * deltaTime, PLANE_VELOCITY_Y * (error)/NORM_FACTOR * deltaTime);
            }
        }
        else if(state == PLANE_STATE_UP){
            error = World.World_Height * 4.0f / 5 - position.y;
            position.add(PLANE_VELOCITY_X * deltaTime, PLANE_VELOCITY_Y * (error)/NORM_FACTOR * deltaTime);
        }
        else{
            error = World.World_Height * 1.0f / 5 - position.y;
            position.add(PLANE_VELOCITY_X * deltaTime, PLANE_VELOCITY_Y * (error)/NORM_FACTOR * deltaTime);
        }

        bounds.lowerLeft.set(position).sub(Plane_Width / 2, Plane_Height / 2);

        stateTime += deltaTime;
    }

}
