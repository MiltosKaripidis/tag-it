package com.example.karhades_pc.tag_it.activity;

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
import com.example.karhades_pc.tag_it.model.MyTags;
import com.example.karhades_pc.tag_it.model.NfcTag;
import com.example.karhades_pc.tag_it.R;
import com.example.karhades_pc.tag_it.fragment.TrackingTagFragment;
import com.example.karhades_pc.utils.TransitionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TrackingTagPagerActivity extends AppCompatActivity implements ViewPager.PageTransformer {

    public static final String EXTRA_CURRENT_TAG_POSITION = "com.example.karhades_pc.tag_it.current_tag_position";
    public static final String EXTRA_OLD_TAG_POSITION = "com.example.karhades_pc.tag_it.old_tag_position";

    private static final float MIN_SCALE = 0.75f;

    private ViewPager viewPager;
    private FragmentAdapter fragmentAdapter;

    private String tagId;
    private boolean isTagDiscovered;
    private ArrayList<NfcTag> nfcTags;
    private NfcHandler nfcHandler;

    private int currentTagPosition;
    private int originalTagPosition;
    private boolean isReturning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_tag_pager);

        // Make content appear behind status bar.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Get the nfcTags from MyTags.
        nfcTags = MyTags.get(this).getNfcTags();

        getIntentExtras(getIntent());
        setupNFC();
        setupViewPager();
        solveNfcTagAfterViewPagerLoad();

        if (TransitionHelper.itSupportsTransitions()) {
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

        nfcHandler.enableNfcReadTag(intent);

        getIntentExtras(intent);
        setCurrentTagPage();
        solveNfcTag();
    }

    private void solveNfcTag() {
        if (isTagDiscovered) {
            TrackingTagFragment fragment = fragmentAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.solveNfcTag(tagId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcHandler.enableForegroundDispatch();
    }

    private void getIntentExtras(Intent intent) {
        // Gets the NfcTag ID either from the onListClick() of TrackingGameFragment
        // or the startActivityFromNfc() of NfcHandler.
        tagId = intent.getStringExtra(TrackingTagFragment.EXTRA_TAG_ID);

        isTagDiscovered = intent.getBooleanExtra(TrackingTagFragment.EXTRA_TAG_DISCOVERED, false);

        currentTagPosition = intent.getIntExtra(EXTRA_CURRENT_TAG_POSITION, -1);
        originalTagPosition = currentTagPosition;
    }

    private void setupNFC() {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(this);
    }

    private void setCurrentTagPage() {
        NfcTag nfcTag = MyTags.get(this).getNfcTag(tagId);
        int position = MyTags.get(this).getNfcTags().indexOf(nfcTag);
        if (position != -1) {
            viewPager.setCurrentItem(position);
        }
    }

    /**
     * Wait for the ViewPager to finish loading it's content
     * and then start the transition for the shared element.
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
            public void onPageScrolled(int i, float v, int i2) {
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

        // Search for the same tag id and set it as current.
        for (int i = 0; i < nfcTags.size(); i++) {
            NfcTag nfcTag = nfcTags.get(i);

            if (nfcTag.getTagId().equals(tagId)) {
                viewPager.setCurrentItem(i);
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
