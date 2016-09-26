/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.fragment.DetailsStepFragment;
import com.karhades.tag_it.main.controller.fragment.PictureStepFragment;
import com.karhades.tag_it.main.controller.fragment.WriteStepFragment;
import com.karhades.tag_it.utils.stepper.AbstractStep;
import com.karhades.tag_it.utils.stepper.adapter.PageAdapter;
import com.karhades.tag_it.utils.stepper.style.TabStepper;

/**
 * Controller Activity class that hosts a CreateTagFragment.
 */
public class CreateTagActivity extends TabStepper {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTitle("Create Tag");
        setPrimaryColor(getResources().getColor(R.color.primary));
        setDisabledTouch();
        setPreviousVisible();

        addStep(PictureStepFragment.newInstance("Take a picture"));
        addStep(DetailsStepFragment.newInstance("Fill out the details"));
        addStep(WriteStepFragment.newInstance("Write to tag"));

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Delivers the NFC intent to the last step.
        AbstractStep abstractStep = ((PageAdapter) mPagerAdapter).getItem(2);
        if (abstractStep instanceof WriteStepFragment) {
            ((WriteStepFragment) abstractStep).onNewIntent(intent);
        }
    }

    @Override
    public void onComplete() {
        // Sets result for REQUEST_INSERT.
        setResult(Activity.RESULT_OK);

        finish();
    }
}
