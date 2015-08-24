package com.example.karhades_pc.riddlehunting;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.example.karhades_pc.sliding_tab_layout.SlidingTabLayout;

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
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        // Substitute the action bar for this toolbar.
        setSupportActionBar(toolbar);
        // Set the navigation icon for the toolbar.
        toolbar.setNavigationIcon(R.mipmap.ic_action_menu);
    }

    @TargetApi(23)
    @SuppressWarnings("deprecation")
    private void setupTabMenu()
    {
        // Tab names.
        final String[] tabNames = {"HOME", "MY RIDDLES", "RANKING"};

        // The FragmentManager is needed for the view pager.
        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new RiddleListFragment();
                    case 1:
                        return new RiddleListFragment();
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
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tab_layout);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
        slidingTabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryText));
    }
}
