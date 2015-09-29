package com.example.karhades_pc.tag_it;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.karhades_pc.nfc.NfcHandler;

import java.util.ArrayList;

/**
 * Created by Karhades on 15-Sep-15.
 */
public class CreateTagPagerActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private ArrayList<NfcTag> nfcTags;
    private String tagId;

    NfcHandler nfcHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tag_pager);

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

        nfcHandler.enableNfcWriteTag(intent);
    }

    private void getIntentExtras() {
        // Get the NfcTag ID from the onListClick() of CreateGameFragment.
        tagId = getIntent().getStringExtra(CreateTagFragment.EXTRA_TAG_ID);
    }

    @SuppressWarnings("deprecation")
    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.create_tag_pager_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                NfcTag nfcTag = nfcTags.get(position);

                return CreateTagFragment.newInstance(nfcTag.getTagId());
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
}
