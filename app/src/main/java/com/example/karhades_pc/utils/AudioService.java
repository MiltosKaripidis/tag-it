package com.example.karhades_pc.utils;

import android.app.IntentService;
import android.content.Intent;

import com.example.karhades_pc.tag_it.R;

/**
 * Created by Karhades on 11-11-15.
 */
public class AudioService extends IntentService {

    private static final String TAG = "AudioService";

    public AudioService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AudioPlayer audioPlayer = new AudioPlayer();
        audioPlayer.play(this, R.raw.cheering);
    }
}
