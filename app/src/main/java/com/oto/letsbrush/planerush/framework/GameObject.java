package com.oto.letsbrush.planerush.framework;


import com.oto.letsbrush.planerush.math.Rectangle;
import com.oto.letsbrush.planerush.math.Vector2;

public class GameObject {
    public Vector2 position;
    public Rectangle bounds;

    public GameObject(float x, float y, float width, float height){
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x - width/2, y - height/2, width, height);
    }

}
