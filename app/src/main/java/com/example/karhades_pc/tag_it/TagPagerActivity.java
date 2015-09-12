package com.example.karhades_pc.tag_it;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TagPagerActivity extends AppCompatActivity {

    private ViewPager viewPager;

    private String tagId;
    private boolean nfcDiscovered;
    private ArrayList<NfcTag> nfcTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the nfcTags from MyTags.
        nfcTags = MyTags.get(this).getNfcTags();

        getIntentExtras();

        setupViewPager();

        setContentView(viewPager);
    }

    private void getIntentExtras() {
        // Gets the NfcTag ID either from the onListClick() of TrackingGameFragment
        // or the startActivityFromNfc() of NfcHandler.
        tagId = getIntent().getStringExtra(TrackingTagFragment.EXTRA_TAG_ID);

        // Get a boolean value whether the intent was sent from NfcHandler.
        nfcDiscovered = getIntent().getBooleanExtra(TrackingTagFragment.EXTRA_NFC_TAG_DISCOVERED, false);
    }

    @SuppressWarnings("deprecation")
    private void setupViewPager() {

        viewPager = new ViewPager(this);
        viewPager.setId(R.id.list_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int i) {
                NfcTag nfcTag = nfcTags.get(i);

                Fragment fragment = TrackingTagFragment.newInstance(nfcTag.getTagId(), nfcDiscovered);
                nfcDiscovered = false;
                return fragment;
            }

            @Override
            public int getCount() {
                return nfcTags.size();
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //EMPTY
            }

            @Override
            public void onPageSelected(int i) {
                NfcTag nfcTag = nfcTags.get(i);
                setTitle(nfcTag.getTitle());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //EMPTY
            }
        });

        for (int i = 0; i < nfcTags.size(); i++) {
            if (nfcTags.get(i).getTagId().equals(tagId)) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
    }
}
