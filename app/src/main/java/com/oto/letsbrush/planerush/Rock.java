package com.oto.letsbrush.planerush;

import com.oto.letsbrush.planerush.framework.GameObject;

public class Rock extends GameObject {
    public static float Rock_Width = 3.0f;
    public static float Rock_Height = 5.0f;

    public Rock(float x, float y){
        super(x, y, Rock_Width, Rock_Height);
    }
}
