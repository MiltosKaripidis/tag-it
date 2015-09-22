package com.example.karhades_pc.tag_it;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.karhades_pc.nfc.NfcHandler;
import com.example.karhades_pc.sliding_tab_layout.SlidingTabLayout;

/**
 * Created by Karhades on 20-Aug-15.
 */
public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;

    private NfcHandler nfcHandler;
    private int pagePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupToolbar();
        setupTabMenu();
        setUpNavigationDrawer();
        setupNFC();
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcHandler.disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (pagePosition == 0) {
            nfcHandler.enableNfcReadTag(intent);
        } else if (pagePosition == 1) {
            Toast.makeText(this, "Share game tab!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Click the + button or overwrite an existing NFC tag.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcHandler.enableForegroundDispatch();
    }

    private void setupNFC() {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(this);
        nfcHandler.enableNfcReadTag(getIntent());
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.create_tag_tool_bar);
        // Substitute the action bar for this toolbar.
        setSupportActionBar(toolbar);
    }

    @SuppressWarnings("deprecation")
    private void setupTabMenu() {
        // Tab names.
        final String[] tabNames = {"TRACKING", "SHARE", "CREATE"};

        // The FragmentManager is needed for the view pager.
        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new TrackingGameFragment();
                    case 1:
                        return new ShareGameFragment();
                    case 2:
                        return new CreateGameFragment();
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
                return getResources().getColor(R.color.accent);
            }
        });
        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pagePosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setUpNavigationDrawer() {
        NavigationDrawerFragment fragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        fragment.setUp(drawerLayout, toolbar);
    }
}
