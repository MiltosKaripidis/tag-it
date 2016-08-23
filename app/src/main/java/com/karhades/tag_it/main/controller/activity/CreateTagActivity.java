/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.fragment.CreateTagFragment;

/**
 * Controller Activity class that hosts a CreateTagFragment.
 */
public class CreateTagActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return CreateTagFragment.newInstance();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Gets FragmentManager.
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Gets CreateTagFragment through it's ID.
        CreateTagFragment createTagFragment = (CreateTagFragment) fragmentManager.findFragmentById(R.id.fragment_container);

        // Calls fragment's onNewIntent.
        createTagFragment.onNewIntent(intent);
    }
}
