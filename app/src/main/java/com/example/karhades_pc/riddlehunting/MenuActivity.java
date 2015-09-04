package com.example.karhades_pc.riddlehunting;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.karhades_pc.nfc.NfcHandler;
import com.example.karhades_pc.sliding_tab_layout.SlidingTabLayout;

/**
 * Created by Karhades on 20-Aug-15.
 */
public class MenuActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT > 10) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);

        setupToolbar();
        setupTabMenu();
        setUpNavigationDrawer();
        setupNFC();
    }

    @Override
    protected void onPause() {
        super.onPause();

        NfcHandler.get().disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        NfcHandler.get().handleDiscoveredTag(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NfcHandler.get().enableForegroundDispatch();
    }

    private void setupNFC() {
        NfcHandler.get().setupNfcHandler(this, getIntent());
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        // Substitute the action bar for this toolbar.
        setSupportActionBar(toolbar);
    }

    @SuppressWarnings("deprecation")
    private void setupTabMenu() {
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
        // Slider color.
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });
    }

    private void setUpNavigationDrawer() {
        NavigationDrawerFragment fragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        fragment.setUp(drawerLayout, toolbar);
    }
}
