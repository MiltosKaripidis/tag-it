package com.example.karhades_pc.tag_it;

import android.annotation.TargetApi;
import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.karhades_pc.contextual_action_bar.MaterialCab;
import com.example.karhades_pc.nfc.NfcHandler;

/**
 * Created by Karhades on 20-Aug-15.
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private MaterialCab materialCab;

    private NfcHandler nfcHandler;
    private int pagePosition = 0;
    private Bundle bundle;

    private static OnContextActivityListener onContextActivityListener;

    public static void setOnContextActivityListener(OnContextActivityListener newOnContextActivityListener) {
        onContextActivityListener = newOnContextActivityListener;
    }

    public interface OnContextActivityListener {
        void onMenuItemPressed(int id);

        void onContextExited();
    }

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

        nfcHandler.disableForegroundDispatch();

        // Save the tags to a file.
        MyTags.get(this).saveTags();

        // If Contextual Action Bar is enabled, close it.
        if (materialCab != null && materialCab.isActive()) {
            disableContextBar();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        nfcHandler.handleAndroidBeamReceivedFiles(intent);

        if (pagePosition == 0) {
            nfcHandler.enableNfcReadTag(intent);
        } else if (pagePosition == 1) {
            Snackbar.make(findViewById(R.id.navigation_drawer_layout), "Approach the devices to share game.", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(findViewById(R.id.coordinator_layout), "Click the + button to create a new one.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcHandler.enableForegroundDispatch();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        Log.d("MainActivity", "onActivityReenter called!");
        super.onActivityReenter(resultCode, data);

        bundle = new Bundle(data.getExtras());
        int oldPosition = bundle.getInt(TrackingTagPagerActivity.EXTRA_OLD_ITEM_POSITION);
        int currentPosition = bundle.getInt(TrackingTagPagerActivity.EXTRA_OLD_ITEM_POSITION);

        if (oldPosition != currentPosition) {

        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else if (materialCab != null && materialCab.isActive()) {
            disableContextBar();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(navigationView);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void disableContextBar() {
        materialCab.finish();
        onContextActivityListener.onContextExited();
    }

    private void setupNFC() {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(this);
        nfcHandler.enableNfcReadTag(getIntent());
        nfcHandler.enableAndroidBeamShareFiles();
        nfcHandler.handleAndroidBeamReceivedFiles(getIntent());
    }

    private void setupNavigationDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
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
                pagePosition = tab.getPosition();
                if (materialCab != null && materialCab.isActive() && tab.getPosition() != 2) {
                    disableContextBar();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // DO NOTHING.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // DO NOTHING.
            }
        });
    }

    private void setupViewPager() {
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
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    private void setupContextualActionBar() {
        materialCab = new MaterialCab(this, R.id.view_stub);
        materialCab.setMenu(R.menu.fragment_create_game_context);
        materialCab.setBackgroundColor(getResources().getColor(R.color.accent));

        // Listen for CreateGameFragment's events.
        CreateGameFragment.setOnContextFragmentListener(new CreateGameFragment.OnContextFragmentListener() {
            @Override
            public void onItemLongClicked() {
                changeBarThemeColor(false);

                materialCab.start(new MaterialCab.Callback() {
                    @Override
                    public boolean onCabCreated(MaterialCab cab, Menu menu) {
                        return true;
                    }

                    @Override
                    public boolean onCabItemClicked(MenuItem item) {
                        MenuItem selectAllItem = materialCab.getMenu().findItem(R.id.context_bar_select_all_item);
                        MenuItem clearSelectionItem = materialCab.getMenu().findItem(R.id.context_bar_clear_selection_item);

                        switch (item.getItemId()) {
                            case R.id.context_bar_delete_item:
                                onContextActivityListener.onMenuItemPressed(R.id.context_bar_delete_item);
                                disableContextBar();
                                return true;
                            case R.id.context_bar_select_all_item:
                                clearSelectionItem.setVisible(true);
                                selectAllItem.setVisible(false);
                                onContextActivityListener.onMenuItemPressed(R.id.context_bar_select_all_item);
                                return true;
                            case R.id.context_bar_clear_selection_item:
                                clearSelectionItem.setVisible(false);
                                selectAllItem.setVisible(true);
                                onContextActivityListener.onMenuItemPressed(R.id.context_bar_clear_selection_item);
                                return true;
                            default:
                                return false;
                        }
                    }

                    @Override
                    public boolean onCabFinished(MaterialCab cab) {
                        changeBarThemeColor(true);
                        onContextActivityListener.onContextExited();
                        return true;
                    }
                });
            }

            @Override
            public void onItemClicked(int tagsSelected) {
                materialCab.setTitle(tagsSelected + " selected");

                MenuItem deleteItem = materialCab.getMenu().findItem(R.id.context_bar_delete_item);
                // If there are no selected tags.
                if (tagsSelected == 0) {
                    deleteItem.setVisible(false);
                } else {
                    deleteItem.setVisible(true);
                }
            }
        });
    }

    @TargetApi(21)
    private void changeBarThemeColor(boolean isVisible) {
        if (isVisible) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));

            tabLayout.setTabTextColors(getResources().getColorStateList(R.color.tab_selector));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.primary));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.accent_dark));

            tabLayout.setTabTextColors(getResources().getColorStateList(R.color.tab_selector_context));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.accent));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primary));
        }
    }
}
