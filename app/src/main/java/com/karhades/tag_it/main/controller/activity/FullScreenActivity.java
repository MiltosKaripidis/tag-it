/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.utils.PictureLoader;

/**
 * Controller Activity class that loads a Fullscreen image.
 */
public class FullScreenActivity extends Activity {

    /**
     * Extra constant.
     */
    private static final String EXTRA_FILE_PATH = "com.karhades.tag_it.file_path";

    /**
     * Instance variable.
     */
    private ImageView mImageView;

    public static Intent newIntent(Context context, String filePath) {
        Intent intent = new Intent(context, FullScreenActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toggleSystemUI();
        setContentView(R.layout.activity_fullscreen);

        initializeWidgets();
        loadImage();
    }

    private void initializeWidgets() {
        mImageView = (ImageView) findViewById(R.id.fullscreen_image_view);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSystemUI();
            }
        });
    }

    private void loadImage() {
        // Get file path from intent extra.
        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);

        // Load image.
        PictureLoader.loadBitmapWithPicasso(this, filePath, mImageView);
    }

    private void toggleSystemUI() {
        // Full screen | Hide navigation bar.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
