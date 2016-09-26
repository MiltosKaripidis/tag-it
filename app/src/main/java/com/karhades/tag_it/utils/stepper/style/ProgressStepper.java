package com.karhades.tag_it.utils.stepper.style;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

/**
 * @author Francesco Cannizzaro (fcannizzaro).
 */
public class ProgressStepper extends BaseNavigation {

    // views
    protected ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        applyTheme();

        setContentView(com.github.fcannizzaro.materialstepper.R.layout.style_progress);

        init();

        mProgress = (ProgressBar) findViewById(com.github.fcannizzaro.materialstepper.R.id.stepProgress);
        assert mProgress != null;
        mProgress.getProgressDrawable().setColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP);

        onUpdate();

    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        mProgress.setMax(mSteps.total());
        mProgress.setProgress(mSteps.current() + 1);
    }


    @Override
    public void onError() {
        super.onError();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwitch.setDisplayedChild(0);
            }
        }, 1500);
    }
}
