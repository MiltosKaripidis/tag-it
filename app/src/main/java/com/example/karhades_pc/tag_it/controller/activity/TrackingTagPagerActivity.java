package com.example.karhades_pc.tag_it.controller.activity;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.example.karhades_pc.nfc.NfcHandler;
import com.example.karhades_pc.tag_it.R;
import com.example.karhades_pc.tag_it.controller.fragment.TrackingTagFragment;
import com.example.karhades_pc.tag_it.model.MyTags;
import com.example.karhades_pc.tag_it.model.NfcTag;
import com.example.karhades_pc.utils.TransitionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TrackingTagPagerActivity extends AppCompatActivity implements ViewPager.PageTransformer {

    /**
     * Extras constants.
     */
    public static final String EXTRA_CURRENT_TAG_POSITION = "com.example.karhades_pc.tag_it.current_tag_position";
    public static final String EXTRA_OLD_TAG_POSITION = "com.example.karhades_pc.tag_it.old_tag_position";

    /**
     * ViewPager.PageTransformer constant.
     */
    private static final float MIN_SCALE = 0.75f;

    /**
     * Widget reference.
     */
    private ViewPager viewPager;

    /**
     * Instance variables.
     */
    private ArrayList<NfcTag> nfcTags;
    private FragmentAdapter fragmentAdapter;
    private String tagId;
    private boolean isTagDiscovered;

    /**
     * NFC adapter.
     */
    private NfcHandler nfcHandler;

    /**
     * Transition variables.
     */
    private int currentTagPosition;
    private int originalTagPosition;
    private boolean isReturning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_tag_pager);

        // Get the list of NFC tags.
        nfcTags = MyTags.get(this).getNfcTags();

        getIntentExtras(getIntent());
        setupNFC();
        setupViewPager();

        if (isTagDiscovered) {
            solveNfcTagAfterViewPagerLoad();
        }

        if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
            enableTransitions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcHandler.disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Get the ID of the discovered tag.
        tagId = nfcHandler.handleNfcReadTag(intent);
        // If there was no error.
        if (tagId != null) {
            setCurrentTagPage();
            solveNfcTag();
        }
    }

    private void solveNfcTag() {
        TrackingTagFragment fragment = fragmentAdapter.getCurrentFragment();
        if (fragment != null) {
            fragment.solveNfcTag(tagId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcHandler.enableForegroundDispatch();
    }

    private void getIntentExtras(Intent intent) {
        // Gets the NfcTag ID either from the onListClick() of TrackingGameFragment
        // or from NFC tag discovery.
        tagId = intent.getStringExtra(TrackingTagFragment.EXTRA_TAG_ID);

        isTagDiscovered = intent.getBooleanExtra(TrackingTagFragment.EXTRA_TAG_DISCOVERED, false);

        currentTagPosition = intent.getIntExtra(EXTRA_CURRENT_TAG_POSITION, -1);
        originalTagPosition = currentTagPosition;
    }

    private void setupNFC() {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(this);
    }

    /**
     * Wait for the ViewPager to finish loading it's content
     * before solving the NFC tag.
     */
    private void solveNfcTagAfterViewPagerLoad() {
        viewPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                viewPager.getViewTreeObserver().removeOnPreDrawListener(this);

                solveNfcTag();

                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.tracking_tag_pager_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentAdapter = new FragmentAdapter(fragmentManager);

        viewPager.setAdapter(fragmentAdapter);
        viewPager.setPageTransformer(true, this);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // DO NOTHING
            }

            @Override
            public void onPageSelected(int position) {
                currentTagPosition = position;

                NfcTag nfcTag = nfcTags.get(position);
                setTitle(nfcTag.getTitle());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // DO NOTHING
            }
        });

        setCurrentTagPage();
    }

    private void setCurrentTagPage() {
        NfcTag nfcTag = MyTags.get(this).getNfcTag(tagId);
        int position = MyTags.get(this).getNfcTags().indexOf(nfcTag);

        if (position != -1) {
            viewPager.setCurrentItem(position);
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

        private TrackingTagFragment currentFragment;

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            NfcTag nfcTag = nfcTags.get(position);

            return TrackingTagFragment.newInstance(nfcTag.getTagId());
        }

        @Override
        public int getCount() {
            return nfcTags.size();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);

            currentFragment = (TrackingTagFragment) object;
        }

        public TrackingTagFragment getCurrentFragment() {
            return currentFragment;
        }
    }

    // Used for transitions.
    @TargetApi(21)
    @Override
    public void finishAfterTransition() {
        if (TransitionHelper.isTransitionEnabled) {
            isReturning = true;

            // Hide the fragment's action button and pass a runnable to run after
            // the animation ends.
            fragmentAdapter.getCurrentFragment().hideActionButton(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_OLD_TAG_POSITION, originalTagPosition);
                    intent.putExtra(EXTRA_CURRENT_TAG_POSITION, currentTagPosition);
                    setResult(RESULT_OK, intent);

                    TrackingTagPagerActivity.super.finishAfterTransition();
                }
            });
        } else {
            TrackingTagPagerActivity.super.finishAfterTransition();
        }
    }

    @TargetApi(21)
    private void enableTransitions() {
        // Postpone the loading of Activity until
        // the shared element is ready to transition.
        postponeEnterTransition();

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                // If it's returning to MainActivity (TrackingGameFragment).
                if (isReturning) {
                    // Get shared view.
                    View sharedView = fragmentAdapter.getCurrentFragment().getSharedElement();

                    // If shared view was recycled.
                    if (sharedView == null) {
                        // If shared view is null, then it has likely been scrolled off screen and
                        // recycled. In this case we cancel the shared element transition by
                        // removing the shared elements from the shared elements map.
                        names.clear();
                        sharedElements.clear();
                    }
                    // If user swiped to another tag.
                    else if (currentTagPosition != originalTagPosition) {
                        // Clear all the previous registrations.
                        names.clear();
                        sharedElements.clear();

                        // Add the correct transition name for the current view.
                        names.add(sharedView.getTransitionName());
                        sharedElements.put(sharedView.getTransitionName(), sharedView);
                    }

                    // Add the NavigationBar to shared elements to avoid blinking.
                    View navigationBar = findViewById(android.R.id.navigationBarBackground);

                    if (navigationBar != null) {
                        names.add(navigationBar.getTransitionName());
                        sharedElements.put(navigationBar.getTransitionName(), navigationBar);
                    }
                }
            }
        });
    }
}
