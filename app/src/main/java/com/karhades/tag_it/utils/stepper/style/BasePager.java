package com.karhades.tag_it.utils.stepper.style;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;


import com.karhades.tag_it.R;
import com.karhades.tag_it.utils.stepper.AbstractStep;
import com.karhades.tag_it.utils.stepper.adapter.PageAdapter;
import com.karhades.tag_it.utils.stepper.adapter.PageChangeAdapter;
import com.karhades.tag_it.utils.stepper.adapter.PageStateAdapter;
import com.karhades.tag_it.utils.stepper.interfaces.Pageable;

import java.util.List;

/**
 * @author Francesco Cannizzaro (fcannizzaro).
 */
public class BasePager extends BaseStyle {

    // view
    protected ViewPager mPager;

    // adapters
    protected Pageable mPagerAdapter;

    protected void init() {
        super.init();
        mPager = (ViewPager) findViewById(R.id.stepper_stepPager);
        assert mPager != null;
        mPager.setAdapter((PagerAdapter) mPagerAdapter);
        mSteps.get(0).onStepVisible();
        mPager.addOnPageChangeListener(new PageChangeAdapter() {
            @Override
            public void onPageSelected(int position) {
                mSteps.get(position).onStepVisible();
            }
        });
    }

    private void initAdapter() {
        if (mPagerAdapter == null)
            mPagerAdapter = getStateAdapter() ? new PageStateAdapter(getSupportFragmentManager()) : new PageAdapter(getSupportFragmentManager());
    }

    @Override
    public void addStep(AbstractStep step) {
        super.addStep(step);
        initAdapter();
        mPagerAdapter.add(step);
    }

    @Override
    public void addSteps(List<AbstractStep> steps) {
        super.addSteps(steps);
        initAdapter();
        mPagerAdapter.set(mSteps.getSteps());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        mPager.setCurrentItem(mSteps.current());
    }

}
