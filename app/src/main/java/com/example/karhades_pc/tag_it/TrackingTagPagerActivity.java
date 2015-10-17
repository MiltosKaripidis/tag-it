package com.example.karhades_pc.tag_it;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.karhades_pc.nfc.NfcHandler;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TrackingTagPagerActivity extends AppCompatActivity implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.75f;

    private ViewPager viewPager;

    private String tagId;
    private ArrayList<NfcTag> nfcTags;
    private NfcHandler nfcHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_tag_pager);

        // Get the nfcTags from MyTags.
        nfcTags = MyTags.get(this).getNfcTags();

        getIntentExtras(getIntent());
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

        nfcHandler.enableNfcReadTag(intent);
        setCurrentTagPage(intent);
    }

    private void setCurrentTagPage(Intent intent) {
        getIntentExtras(intent);
        NfcTag nfcTag = MyTags.get(this).getNfcTag(tagId);
        int position = MyTags.get(this).getNfcTags().indexOf(nfcTag);
        if (position != -1) {
            // Solve the Nfc Tag.
            MyTags.get(this).solveNfcTag(tagId);

            viewPager.setCurrentItem(position);
        }
    }

    private void getIntentExtras(Intent intent) {
        // Gets the NfcTag ID either from the onListClick() of TrackingGameFragment
        // or the startActivityFromNfc() of NfcHandler.
        tagId = intent.getStringExtra(TrackingTagFragment.EXTRA_TAG_ID);
    }

    @SuppressWarnings("deprecation")
    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.tracking_tag_pager_view_pager);
        viewPager.setPageTransformer(true, this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int i) {
                NfcTag nfcTag = nfcTags.get(i);

                return TrackingTagFragment.newInstance(nfcTag.getTagId());
            }

            @Override
            public int getCount() {
                return nfcTags.size();
            }
        });

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
}
