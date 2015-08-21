package com.example.karhades_pc.utils;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by Karhades on 20-Aug-15.
 */
public class AudioPlayer
{
    private MediaPlayer mediaPlayer;

    public void play(Context context, int sound)
    {
        mediaPlayer = MediaPlayer.create(context, sound);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
        mediaPlayer.start();
    }

    public void stop()
    {
        if(mediaPlayer != null)
        {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
