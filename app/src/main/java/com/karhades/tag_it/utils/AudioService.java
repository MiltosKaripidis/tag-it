/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.utils;

import android.app.IntentService;
import android.content.Intent;

import com.karhades.tag_it.R;


/**
 * Background service that plays a sound.
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
