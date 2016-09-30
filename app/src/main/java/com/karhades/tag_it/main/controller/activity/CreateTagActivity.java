/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.fragment.DetailsStepFragment;
import com.karhades.tag_it.main.controller.fragment.PictureStepFragment;
import com.karhades.tag_it.main.controller.fragment.WriteStepFragment;
import com.karhades.tag_it.utils.TransitionHelper;
import com.karhades.tag_it.utils.stepper.AbstractStep;
import com.karhades.tag_it.utils.stepper.adapter.PageAdapter;
import com.karhades.tag_it.utils.stepper.style.TabStepper;

/**
 * Controller Activity class that hosts a CreateTagFragment.
 */
public class CreateTagActivity extends TabStepper {

    /**
     * Widget references.
     */
    private FloatingActionButton floatingActionButton;
    private View revealRoot;

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

        initializeWidgets();

        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            revealShow();
        } else {
            revealRoot.setVisibility(View.VISIBLE);
        }

    }

    private void initializeWidgets() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.stepper_fab);
        revealRoot = findViewById(R.id.stepper_reveal_root);
    }

    @Override
    public void onBackPressed() {
        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            revealHide();
        } else {
            super.onBackPressed();
        }
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

        onBackPressed();
    }

    @TargetApi(21)
    private void revealShow() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.changebounds_with_arcmotion);
        getWindow().setSharedElementEnterTransition(transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);

                TransitionHelper.circularShow(floatingActionButton, revealRoot, null);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    private void revealHide() {
        TransitionHelper.circularHide(floatingActionButton, revealRoot, new Runnable() {
            @Override
            public void run() {
                CreateTagActivity.super.onBackPressed();
            }
        });
    }
}
