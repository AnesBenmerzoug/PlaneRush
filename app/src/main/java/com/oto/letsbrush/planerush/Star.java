package com.oto.letsbrush.planerush;

import com.oto.letsbrush.planerush.framework.DynamicGameObject;
import com.oto.letsbrush.planerush.math.Rectangle;

public class Star extends DynamicGameObject {
    public static final float Star_Width = 1.0f;
    public static final float Star_Height = 1.0f;
    public static final float Star_Velocity = -1.0f;

    public static final int Star_Score = 100;

    public float stateTime = 0;
    public int state;

    public Star(float x, float y){
        super(x, y, Star_Width, Star_Height);
        velocity.set(Star_Velocity, 0);
        this.bounds = new Rectangle(x - Star_Width/2, y - Star_Height/2, Star_Width , Star_Height);
    }

    public void update(float deltaTime){
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        bounds.lowerLeft.set(position).sub(Star_Width / 2, Star_Height / 2);

        stateTime += deltaTime;
    }

}
