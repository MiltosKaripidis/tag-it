package com.example.karhades_pc.tag_it;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.karhades_pc.contextual_action_bar.MaterialCab;
import com.example.karhades_pc.nfc.NfcHandler;

/**
 * Created by Karhades on 20-Aug-15.
 */
@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private FragmentAdapter adapter;
    private ViewPager viewPager;
    private MaterialCab contextualActionBar;
    private MaterialCab.Callback contextualActionBarCallback;

    private NfcHandler nfcHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNFC();
        setupNavigationDrawer();
        setupToolbar();
        setupTabLayout();
        setupViewPager();
        setupContextualActionBar();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disable the interception of any intent.
        nfcHandler.disableForegroundDispatch();

        // Save the tags to a file before leaving.
        MyTags.get(this).saveTags();

        // Close the contextual action bar before leaving.
        if (contextualActionBar != null && contextualActionBar.isActive()) {
            disableContextualActionBar();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        nfcHandler.handleAndroidBeamReceivedFiles(intent);

        // Tab 1.
        if (tabLayout.getSelectedTabPosition() == 0) {
            nfcHandler.enableNfcReadTag(intent);
        }
        // Tab 2.
        else if (tabLayout.getSelectedTabPosition() == 1) {
            Snackbar.make(findViewById(R.id.main_navigation_drawer_layout), "Approach the devices to share game.", Snackbar.LENGTH_LONG).show();
        }
        // Tab 3.
        else if (tabLayout.getSelectedTabPosition() == 2) {
            Snackbar.make(findViewById(R.id.create_coordinator_layout), "Click the + button to create a new one.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Intercept any intent that is associated with a Tag discovery.
        nfcHandler.enableForegroundDispatch();
    }

    @Override
    public void onBackPressed() {
        // Close NavigationView.
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        }
        // Close contextual action bar.
        else if (contextualActionBar != null && contextualActionBar.isActive()) {
            disableContextualActionBar();
        }
        // Close Activity.
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Navigation Icon.
            case android.R.id.home:
                drawerLayout.openDrawer(navigationView);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNFC() {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(this);
        nfcHandler.enableNfcReadTag(getIntent());
        nfcHandler.enableAndroidBeamShareFiles();

        nfcHandler.handleAndroidBeamReceivedFiles(getIntent());
    }

    private void setupNavigationDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.main_navigation_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                navigationView.setCheckedItem(item.getItemId());
                drawerLayout.closeDrawer(navigationView);

                switch (item.getItemId()) {
                    case R.id.navigation_tracking:
                        tabLayout.getTabAt(0).select();
                        return true;
                    case R.id.navigation_share:
                        tabLayout.getTabAt(1).select();
                        return true;

                    case R.id.navigation_create:
                        tabLayout.getTabAt(2).select();
                        return true;
                    case R.id.navigation_settings:
                        // DO NOTHING.
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.main_tool_bar);
        // Substitute the action bar for this toolbar.
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.mipmap.hamburger_menu));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("TRACKING"));
        tabLayout.addTab(tabLayout.newTab().setText("SHARE"));
        tabLayout.addTab(tabLayout.newTab().setText("CREATE"));
        tabLayout.setTabTextColors(getResources().getColorStateList(R.color.tab_selector));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                Menu menu = navigationView.getMenu();
                navigationView.setCheckedItem(menu.getItem(tab.getPosition()).getItemId());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (contextualActionBar != null && contextualActionBar.isActive() && tab.getPosition() == 2) {
                    disableContextualActionBar();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // DO NOTHING.
            }
        });
    }

    private void setupViewPager() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        adapter = new FragmentAdapter(fragmentManager);

        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    private void setupContextualActionBar() {
        contextualActionBar = new MaterialCab(this, R.id.view_stub);
        contextualActionBar.setMenu(R.menu.fragment_create_game_context);
        contextualActionBar.setBackgroundColor(getResources().getColor(R.color.accent));
        contextualActionBarCallback = new MaterialCab.Callback() {
            @Override
            public boolean onCabCreated(MaterialCab cab, Menu menu) {
                return true;
            }

            @Override
            public boolean onCabItemClicked(MenuItem item) {
                // If Tab 2 is selected.
                if (tabLayout.getSelectedTabPosition() == 2) {
                    CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(tabLayout.getSelectedTabPosition());

                    MenuItem selectAllItem = contextualActionBar.getMenu().findItem(R.id.context_bar_select_all_item);
                    MenuItem clearSelectionItem = contextualActionBar.getMenu().findItem(R.id.context_bar_clear_selection_item);

                    switch (item.getItemId()) {
                        case R.id.context_bar_delete_item:
                            fragment.contextDeleteSelectedItems();
                            disableContextualActionBar();
                            return true;
                        case R.id.context_bar_select_all_item:
                            clearSelectionItem.setVisible(true);
                            selectAllItem.setVisible(false);
                            fragment.contextSelectAll();
                            return true;
                        case R.id.context_bar_clear_selection_item:
                            clearSelectionItem.setVisible(false);
                            selectAllItem.setVisible(true);
                            fragment.contextClearSelection();
                            return true;
                        default:
                            return false;
                    }
                }
                return false;
            }

            @Override
            public boolean onCabFinished(MaterialCab cab) {
                changeBarsColor(true);

                if (tabLayout.getSelectedTabPosition() == 2) {
                    CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(tabLayout.getSelectedTabPosition());
                    fragment.contextFinish();
                }
                return true;
            }
        };
    }

    private void disableContextualActionBar() {
        CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(tabLayout.getSelectedTabPosition());
        fragment.contextFinish();

        contextualActionBar.finish();
    }

    private void registerCreateGameFragmentListener() {
        // Listen for CreateGameFragment's events.
        CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(2);
        fragment.setOnContextualActionBarEnterListener(new CreateGameFragment.OnContextualActionBarEnterListener() {
            @Override
            public void onItemLongClicked() {
                changeBarsColor(false);

                contextualActionBar.start(contextualActionBarCallback);
            }

            @Override
            public void onItemClicked(int tagsSelected) {
                contextualActionBar.setTitle(tagsSelected + " selected");

                MenuItem deleteItem = contextualActionBar.getMenu().findItem(R.id.context_bar_delete_item);
                // If there are no selected tags.
                if (tagsSelected == 0) {
                    deleteItem.setVisible(false);
                } else {
                    deleteItem.setVisible(true);
                }
            }
        });
    }

    private void changeBarsColor(boolean isVisible) {
        if (isVisible) {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
                getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
            }

            tabLayout.setTabTextColors(getResources().getColorStateList(R.color.tab_selector));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.primary));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.accent_dark));
            }
            tabLayout.setTabTextColors(getResources().getColorStateList(R.color.tab_selector_context));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.accent));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primary));
        }
    }

    private class FragmentAdapter extends FragmentPagerAdapter {

        private static final int ADAPTER_SIZE = 3;
        private SparseArray<Fragment> fragments = new SparseArray<>(ADAPTER_SIZE);

        public FragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TrackingGameFragment();
                case 1:
                    return new ShareGameFragment();
                case 2:
                    return new CreateGameFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return ADAPTER_SIZE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragments.put(position, fragment);

            // Register listeners for each fragment.
            if (fragment instanceof CreateGameFragment) {
                registerCreateGameFragmentListener();
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getFragment(int position) {
            return fragments.get(position);
        }
    }

    // Used for transitions.
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        // If Tab 1.
        if (tabLayout.getSelectedTabPosition() == 0) {
            TrackingGameFragment fragment = (TrackingGameFragment) adapter.getFragment(0);
            fragment.prepareReenterTransition(data);
        }
    }
}
