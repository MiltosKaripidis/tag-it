package com.example.karhades_pc.riddlehunting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class RiddlePagerActivity extends ActionBarActivity {

    private ViewPager viewPager;

    private ArrayList<Riddle> riddles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewPager = new ViewPager(this);
        viewPager.setId(R.id.view_pager);
        setContentView(viewPager);

        riddles = MyRiddles.get(this).getRiddles();

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int i) {
                Riddle riddle = riddles.get(i);

                return RiddleFragment.newInstance(riddle.getTagId(), false);
            }

            @Override
            public int getCount() {
                return riddles.size();
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //EMPTY
            }

            @Override
            public void onPageSelected(int i) {
                Riddle riddle = riddles.get(i);
                setTitle(riddle.getTitle());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //EMPTY
            }
        });

        String tagId = getIntent().getStringExtra(RiddleFragment.EXTRA_TAG_ID);

        for (int i = 0; i < riddles.size(); i++) {
            if (riddles.get(i).getTagId().equals(tagId)) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
    }
}
