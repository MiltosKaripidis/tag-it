package com.example.karhades_pc.tag_it;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.karhades_pc.nfc.NfcHandler;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TrackingTagPagerActivity extends AppCompatActivity implements ViewPager.PageTransformer {

    public static final String EXTRA_CURRENT_ITEM_POSITION = "com.example.karhades_pc.tag_it.current_item_position";
    public static final String EXTRA_OLD_ITEM_POSITION = "com.example.karhades_pc.tag_it.old_item_position";

    private static final float MIN_SCALE = 0.75f;

    private ViewPager viewPager;
    private FragmentAdapter fragmentAdapter;

    private String tagId;
    private boolean isTagDiscovered;
    private ArrayList<NfcTag> nfcTags;
    private NfcHandler nfcHandler;

    private int currentPosition;
    private int originalPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_tag_pager);

        // Make content appear behind status bar.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_ITEM_POSITION, -1);
        originalPosition = currentPosition;

        // Get the nfcTags from MyTags.
        nfcTags = MyTags.get(this).getNfcTags();

        getIntentExtras(getIntent());
        setupNFC();
        setupViewPager();
        setupTransitions();
        postponeTransition();

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                solveNfcTag();
            }
        });
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

    @Override
    public void finishAfterTransition() {
        Log.d("ASDF", "finishAfterTransition called!");
        Intent intent = new Intent();
        intent.putExtra(EXTRA_OLD_ITEM_POSITION, originalPosition);
        intent.putExtra(EXTRA_CURRENT_ITEM_POSITION, currentPosition);
        setResult(RESULT_OK, intent);

        super.finishAfterTransition();
    }

    private void setupTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            TransitionInflater transitionInflater = TransitionInflater.from(this);

            Transition enterTransition = transitionInflater.inflateTransition(R.transition.tracking_enter);
            Transition returnTransition = transitionInflater.inflateTransition(R.transition.tracking_return);

            Transition sharedEnterTransition = transitionInflater.inflateTransition(R.transition.tracking_shared_element);
            Transition sharedReturnTransition = transitionInflater.inflateTransition(R.transition.tracking_shared_element);

            // Set activity transitions.
            //getWindow().setEnterTransition(enterTransition);
            //getWindow().setReturnTransition(returnTransition);

            // Set shared element transitions.
            getWindow().setSharedElementEnterTransition(sharedEnterTransition);
            getWindow().setSharedElementReturnTransition(sharedReturnTransition);
        }
    }

    /**
     * Wait for the ViewPager to finish loading it's content
     * and then start the transition for the shared element.
     */
    private void postponeTransition() {
        supportPostponeEnterTransition();
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                supportStartPostponedEnterTransition();
            }
        });
    }

    private void getIntentExtras(Intent intent) {
        // Gets the NfcTag ID either from the onListClick() of TrackingGameFragment
        // or the startActivityFromNfc() of NfcHandler.
        tagId = intent.getStringExtra(TrackingTagFragment.EXTRA_TAG_ID);

        isTagDiscovered = intent.getBooleanExtra(TrackingTagFragment.EXTRA_TAG_DISCOVERED, false);
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
                currentPosition = position;

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
}
