package com.example.karhades_pc.riddlehunting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.example.karhades_pc.slidingtablayout.SlidingTabLayout;

/**
 * Created by Karhades on 20-Aug-15.
 */
public class MenuTabActivity extends NfcActivity
{
    private ViewPager viewPager;
    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu_tab);

        setupToolbar();
        setupTabMenu();
    }

    private void setupToolbar()
    {
        // Set up the Toolbar.
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupTabMenu()
    {
        // Tab names.
        final String[] tabNames = {"HOME", "MY RIDDLES", "TAB 3"};

        // The FragmentManager is needed for the view pager.
        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager = (ViewPager) findViewById(R.id.menu_view_pager);
        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new RiddleListFragment();
                    case 1:
                        return new RiddleFragment().newInstance("0421DC6AC82980", false);
                    case 2:
                        return new RiddleFragment().newInstance("04BCE16AC82980", false);
                }

                return null;
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabNames[position];
            }
        });
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
    }
}
