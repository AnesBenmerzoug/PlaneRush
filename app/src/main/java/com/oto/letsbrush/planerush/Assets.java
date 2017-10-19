package com.oto.letsbrush.planerush;


import com.oto.letsbrush.planerush.framework.Music;
import com.oto.letsbrush.planerush.framework.Sound;
import com.oto.letsbrush.planerush.gl.Animation;
import com.oto.letsbrush.planerush.gl.Texture;
import com.oto.letsbrush.planerush.gl.TextureRegion;
import com.oto.letsbrush.planerush.implementation.BluetoothGame;

import java.util.ArrayList;
import java.util.List;

public class Assets {
    public static Texture Atlas;

    public static TextureRegion backgroundRegion;
    public static TextureRegion groundGrass;
    public static TextureRegion rockGrass;
    public static TextureRegion puffSmall;
    public static TextureRegion puffLarge;

    public static TextureRegion play;
    public static TextureRegion gameOver;
    public static TextureRegion getReady;

    public static TextureRegion buttonUp;
    public static TextureRegion buttonDown;

    public static Animation star;
    public static Animation planeFly;

    public static Music mainTheme;
    public static Music gameTheme;

    public static List<TextureRegion> numbers;

    public static Sound clickSound;
    public static Sound starSound;

    public static void load(BluetoothGame game){
        Atlas = new Texture(game, "Atlas2.png");

        backgroundRegion = new TextureRegion(Atlas, 0, 0, 512, 307);

        groundGrass = new TextureRegion(Atlas, 0, 380, 512, 45);

        rockGrass = new TextureRegion(Atlas, 512, 0, 55, 121);

        play = new TextureRegion(Atlas, 580, 0, 135, 42);

        gameOver = new TextureRegion(Atlas, 512, 160, 412, 78);

        getReady = new TextureRegion(Atlas, 512, 240, 400, 73);

        buttonUp = new TextureRegion(Atlas, 579, 42, 200, 49);
        buttonDown = new TextureRegion(Atlas, 584, 91, 202, 46);

        planeFly = new Animation(.05f,
                new TextureRegion(Atlas, 0, 308, 88, 73),
                new TextureRegion(Atlas, 88, 308, 88, 73),
                new TextureRegion(Atlas, 176, 308, 88, 73),
                new TextureRegion(Atlas, 88, 308, 88, 73));

        star = new Animation(.15f,
                new TextureRegion(Atlas, 310, 307, 39, 37),
                new TextureRegion(Atlas, 349, 307, 39, 37),
                new TextureRegion(Atlas, 310, 307, 39, 37),
                new TextureRegion(Atlas, 388, 307, 39, 37));

        puffSmall = new TextureRegion(Atlas,264, 307, 25, 21);

        puffLarge = new TextureRegion(Atlas,264, 328, 42, 37);

        numbers = new ArrayList<>();

        numbers.add(new TextureRegion(Atlas, 0, 425, 56, 84));
        numbers.add(new TextureRegion(Atlas, 56, 425, 56, 84));
        numbers.add(new TextureRegion(Atlas, 112, 425, 60, 84));
        numbers.add(new TextureRegion(Atlas, 172, 425, 57, 84));
        numbers.add(new TextureRegion(Atlas, 229, 425, 58, 84));
        numbers.add(new TextureRegion(Atlas, 282, 425, 58, 84));
        numbers.add(new TextureRegion(Atlas, 341, 425, 59, 84));
        numbers.add(new TextureRegion(Atlas, 398, 425, 59, 84));
        numbers.add(new TextureRegion(Atlas, 454, 425, 59, 84));
        numbers.add(new TextureRegion(Atlas, 511, 425, 57, 84));

        mainTheme = game.getAudio().newMusic("mainTheme.ogg");
        mainTheme.setLooping(true);
        mainTheme.setVolume(.5f);

        gameTheme = game.getAudio().newMusic("RiversideRide.wav");
        gameTheme.setLooping(true);
        gameTheme.setVolume(.5f);

        clickSound = game.getAudio().newSound("click2.ogg");

        starSound = game.getAudio().newSound("coin4.wav");
    }

    public static void reload(){
        Atlas.reload();
    }

    public static void playSound(Sound sound) {
        if(Settings.soundEnabled)
            sound.play(1);
    }
}

