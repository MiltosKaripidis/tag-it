package com.karhades.tag_it.main.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.karhades.tag_it.main.model.NfcHandler;
import com.karhades.tag_it.main.R;
import com.karhades.tag_it.main.controller.fragment.CreateTagFragment;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;

import java.util.ArrayList;

/**
 * Created by Karhades on 15-Sep-15.
 */
public class CreateTagPagerActivity extends AppCompatActivity implements ViewPager.PageTransformer {

    /**
     * ViewPager.PageTransformer constant.
     */
    private static final float MIN_SCALE = 0.75f;

    /**
     * Widget references.
     */
    private ViewPager viewPager;

    /**
     * Instance variables.
     */
    private FragmentAdapter fragmentAdapter;
    private ArrayList<NfcTag> nfcTags;
    private String tagId;

    /**
     * NFC adapter.
     */
    private NfcHandler nfcHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tag_pager);

        // Make content appear behind status bar.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Get the nfcTags from MyTags.
        nfcTags = MyTags.get(this).getNfcTags();

        getIntentExtras();
        setupViewPager();
        setupNFC();
    }

    private void setupNFC() {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcHandler.disableForegroundDispatch();
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcHandler.enableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Indicates whether the write operation can start.
        boolean isReady = NfcHandler.getWriteMode();
        if (!isReady) {
            fragmentAdapter.getCurrentFragment().makeSnackBar();
        } else {
            nfcHandler.handleNfcWriteTag(intent);
        }
    }

    private void getIntentExtras() {
        // Get the NfcTag ID from the onListClick() of CreateGameFragment.
        tagId = getIntent().getStringExtra(CreateTagFragment.EXTRA_TAG_ID);
    }

    @SuppressWarnings("deprecation")
    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.create_tag_pager_view_pager);

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

        private CreateTagFragment currentFragment;

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            NfcTag nfcTag = nfcTags.get(position);

            return CreateTagFragment.newInstance(nfcTag.getTagId());
        }

        @Override
        public int getCount() {
            return nfcTags.size();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);

            currentFragment = (CreateTagFragment) object;
        }

        public CreateTagFragment getCurrentFragment() {
            return currentFragment;
        }
    }
}
