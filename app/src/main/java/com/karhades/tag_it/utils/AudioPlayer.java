/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.utils;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Helper Wrapper class for MediaPlayer.
 */
public class AudioPlayer {

    private MediaPlayer mediaPlayer;

    public void play(Context context, int sound) {
        mediaPlayer = MediaPlayer.create(context, sound);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
        mediaPlayer.start();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
