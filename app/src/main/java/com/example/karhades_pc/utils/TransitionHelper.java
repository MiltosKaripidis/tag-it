package com.example.karhades_pc.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by Karhades on 08-11-15.
 */
public class TransitionHelper {

    /**
     * Toggle application transitions.
     */
    public static boolean isTransitionEnabled = true;

    public static boolean isTransitionSupported() {
        return Build.VERSION.SDK_INT >= 21;
    }

    @TargetApi(21)
    public static void circularShow(View view, ViewGroup revealContent, final Runnable runnable) {
        int centerX = (view.getLeft() + view.getRight()) / 2;
        int centerY = (view.getTop() + view.getBottom()) / 2;
        float startRadius = 0;
        float finalRadius = (float) Math.hypot(revealContent.getWidth(), revealContent.getHeight());


        Animator animator = ViewAnimationUtils.createCircularReveal(revealContent, centerX, centerY, startRadius, finalRadius);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        revealContent.setVisibility(View.VISIBLE);
        animator.start();
    }

    @TargetApi(21)
    public static void circularHide(View view, final ViewGroup revealContent, final Runnable runnable) {
        int centerX = (view.getLeft() + view.getRight()) / 2;
        int centerY = (view.getTop() + view.getBottom()) / 2;
        float initialRadius = revealContent.getWidth();
        float finalRadius = 0;

        Animator animator = ViewAnimationUtils.createCircularReveal(revealContent, centerX, centerY, initialRadius, finalRadius);
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                revealContent.setVisibility(View.INVISIBLE);

                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        animator.start();
    }
}
