package com.oto.letsbrush.planerush.implementation;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;


import com.oto.letsbrush.planerush.framework.Audio;
import com.oto.letsbrush.planerush.framework.Music;
import com.oto.letsbrush.planerush.framework.Sound;

import java.io.IOException;

public class AndroidAudio implements Audio {
    AssetManager assets;
    SoundPool soundPool;

    public AndroidAudio(Activity activity){
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.assets = activity.getAssets();
        soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
    }

    public Music newMusic(String fileName){
        try{
            AssetFileDescriptor assetDescriptor = assets.openFd(fileName);
            return new AndroidMusic(assetDescriptor);
        }
        catch(IOException e){
            throw new RuntimeException("Couldn't load music '"+fileName+"'");
        }
    }

    public Sound newSound(String fileName){
        try{
            AssetFileDescriptor assetDescriptor = assets.openFd(fileName);
            int soundId = soundPool.load(assetDescriptor, 0);
            return new AndroidSound(soundPool, soundId);
        }
        catch(IOException e){
            throw new RuntimeException("Couldn't load sound '"+fileName+"'");
        }
    }
}
