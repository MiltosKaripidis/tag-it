/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.fragment.EditTagFragment;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;

import java.util.List;

/**
 * Controller Activity class that hosts EditTagFragment and enables paging.
 */
public class EditTagPagerActivity extends AppCompatActivity implements ViewPager.PageTransformer {

    /**
     * Extra constant.
     */
    private static final String EXTRA_TAG_ID = "com.karhades.tag_it.tag_id";

    /**
     * ViewPager.PageTransformer constant.
     */
    private static final float MIN_SCALE = 0.75f;

    /**
     * Instance variables.
     */
    private FragmentAdapter fragmentAdapter;
    private List<NfcTag> nfcTags;
    private String tagId;

    public static Intent newIntent(Context context, String tagId) {
        Intent intent = new Intent(context, EditTagPagerActivity.class);
        intent.putExtra(EXTRA_TAG_ID, tagId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag_pager);

        // Gets the nfcTags from MyTags.
        nfcTags = MyTags.get(this).getNfcTags();

        // Gets the NfcTag ID from CreateGameFragment.
        tagId = getIntent().getStringExtra(EXTRA_TAG_ID);

        setupViewPager();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        EditTagFragment editTagFragment = fragmentAdapter.getCurrentFragment();
        editTagFragment.onNewIntent(intent);
    }

    @SuppressWarnings("deprecation")
    private void setupViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.edit_tag_pager_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentAdapter = new FragmentAdapter(fragmentManager);

        viewPager.setAdapter(fragmentAdapter);
        viewPager.setPageTransformer(true, this);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                // DO NOTHING
            }

            @Override
            public void onPageSelected(int i) {
                NfcTag nfcTag = nfcTags.get(i);
                setTitle(nfcTag.getTitle());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // DO NOTHING
            }
        });

        // Change to the appropriate page when started.
        for (int i = 0; i < nfcTags.size(); i++) {
            if (nfcTags.get(i).getTagId().equals(tagId)) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();

        // [-Infinity,-1)
        if (position < -1) {
            // This page is way off-screen to the left.
            page.setAlpha(0);

        }
        // [-1,0]
        else if (position <= 0) {
            // Use the default slide transition when moving to the left page.
            page.setAlpha(1);
            page.setTranslationX(0);
            page.setScaleX(1);
            page.setScaleY(1);

        }
        // (0,1]
        else if (position <= 1) {
            // Fade the page out.
            page.setAlpha(1 - position);

            // Counteract the default slide transition.
            page.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1).
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

        }
        // (1,+Infinity]
        else {
            // This page is way off-screen to the right.
            page.setAlpha(0);
        }
    }

    private class FragmentAdapter extends FragmentStatePagerAdapter {

        private EditTagFragment currentFragment;

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            NfcTag nfcTag = nfcTags.get(position);

            return EditTagFragment.newInstance(nfcTag.getTagId());
        }

        @Override
        public int getCount() {
            return nfcTags.size();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);

            currentFragment = (EditTagFragment) object;
        }

        public EditTagFragment getCurrentFragment() {
            return currentFragment;
        }
    }
}
