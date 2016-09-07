/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.karhades.tag_it.main.model.MyTags;

/**
 * Helper class for managing transitions.
 */
public class TransitionHelper {

    public static boolean isTransitionSupportedAndEnabled() {
        return Build.VERSION.SDK_INT >= 21 && !MyTags.isTransitionDisabled();
    }

    @TargetApi(21)
    public static void circularShow(View startView, View animatedView, final Runnable runnable) {
        int centerX = (startView.getLeft() + startView.getRight()) / 2;
        int centerY = (startView.getTop() + startView.getBottom()) / 2;
        float startRadius = 0;
        float endRadius = (float) Math.hypot(animatedView.getWidth(), animatedView.getHeight());

        Animator animator = ViewAnimationUtils.createCircularReveal(animatedView, centerX, centerY, startRadius, endRadius);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        animatedView.setVisibility(View.VISIBLE);
        animator.start();
    }

    @TargetApi(21)
    public static void circularHide(View endView, final View animatedView, final Runnable runnable) {
        int centerX = (endView.getLeft() + endView.getRight()) / 2;
        int centerY = (endView.getTop() + endView.getBottom()) / 2;
        float startRadius = animatedView.getWidth();
        float endRadius = 0;

        Animator animator = ViewAnimationUtils.createCircularReveal(animatedView, centerX, centerY, startRadius, endRadius);
        animator.setDuration(500);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatedView.setVisibility(View.INVISIBLE);

                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        animator.start();
    }
}
