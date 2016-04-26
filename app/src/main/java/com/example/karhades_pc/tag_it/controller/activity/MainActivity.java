package com.example.karhades_pc.tag_it.controller.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.karhades_pc.nfc.NfcHandler;
import com.example.karhades_pc.tag_it.R;
import com.example.karhades_pc.tag_it.controller.fragment.CreateGameFragment;
import com.example.karhades_pc.tag_it.controller.fragment.ShareGameFragment;
import com.example.karhades_pc.tag_it.controller.fragment.TrackingGameFragment;
import com.example.karhades_pc.tag_it.controller.fragment.TrackingTagFragment;
import com.example.karhades_pc.tag_it.model.MyTags;
import com.example.karhades_pc.utils.TransitionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karhades on 20-Aug-15.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Widget references.
     */
    private DrawerLayout drawerLayout;
    private CoordinatorLayout coordinatorLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton floatingActionButton;

    /**
     * Instance variables.
     */
    private TabsAdapter adapter;
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    /**
     * NFC adapter.
     */
    private NfcHandler nfcHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNFC();
        setupFloatingActionButton();
        setupCoordinatorLayout();
        setupNavigationDrawer();
        setupToolbar();
        setupViewPager();
        setupTabLayout();
        setupContextualActionBar();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disable the interception of any intent.
        nfcHandler.disableForegroundDispatch();

        // Save the tags to a file before leaving.
        MyTags.get(this).saveTags();

        disableContextualActionBar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        nfcHandler.handleAndroidBeamReceivedFiles(intent);

        // Tab 1.
        if (tabLayout.getSelectedTabPosition() == 0) {
            // Get the ID of the discovered tag.
            String tagId = nfcHandler.handleNfcReadTag(intent);
            // If there was no error.
            if (tagId != null) {
                startTrackingTagPagerActivity(tagId);
            }
        }
        // Tab 2.
        else if (tabLayout.getSelectedTabPosition() == 1) {
            Snackbar.make(coordinatorLayout, "Approach devices to share game.", Snackbar.LENGTH_LONG).show();
        }
        // Tab 3.
        else if (tabLayout.getSelectedTabPosition() == 2) {
            Snackbar.make(coordinatorLayout, "Click + to create a new one.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void startTrackingTagPagerActivity(String tagId) {
        // Create an Intent and send the extra discovered NfcTag ID and
        // another extra to indicate that it's from the NFC discovery.
        Intent tagIntent = new Intent(this, TrackingTagPagerActivity.class);
        tagIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        tagIntent.putExtra(TrackingTagFragment.EXTRA_TAG_ID, tagId);
        tagIntent.putExtra(TrackingTagFragment.EXTRA_TAG_DISCOVERED, true);
        this.startActivity(tagIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Intercept any intent that is associated with a Tag discovery.
        nfcHandler.enableForegroundDispatch();
    }

    @Override
    public void onBackPressed() {
        // Close contextual action bar.
        disableContextualActionBar();

        // Close NavigationView.
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
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

        // Get the ID of the discovered tag.
        String tagId = nfcHandler.handleNfcReadTag(getIntent());
        // If there was no error.
        if (tagId != null) {
            startTrackingTagPagerActivity(tagId);
        }

        nfcHandler.registerAndroidBeamShareFiles();
        nfcHandler.handleAndroidBeamReceivedFiles(getIntent());
    }

    private void setupFloatingActionButton() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
    }

    private void setupCoordinatorLayout() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);
    }

    @SuppressWarnings("ConstantConditions")
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

    @SuppressWarnings("deprecation")
    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.main_tool_bar);
        // Substitute the action bar for this toolbar.
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.icon_hamburger_menu));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressWarnings("deprecation, ConstantConditions")
    private void setupTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        tabLayout.setTabTextColors(getResources().getColorStateList(R.color.selector_tab_normal));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));
        tabLayout.setupWithViewPager(viewPager);
        // Set tab names.
        String[] tabNames = getResources().getStringArray(R.array.tab_names);
        for (int i = 0; i < tabNames.length; i++) {
            tabLayout.getTabAt(i).setText(tabNames[i]);
        }
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                Menu menu = navigationView.getMenu();
                navigationView.setCheckedItem(menu.getItem(tab.getPosition()).getItemId());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                disableContextualActionBar();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // DO NOTHING.
            }
        });
    }

    private void setupViewPager() {
        adapter = new TabsAdapter(getSupportFragmentManager());
        adapter.addFragment(new TrackingGameFragment());
        adapter.addFragment(new ShareGameFragment());
        adapter.addFragment(new CreateGameFragment());

        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // If Tab 1.
                if (position == 0) {
                    floatingActionButton.hide();
                }
                // If Tab 2.
                else if (position == 1) {
                    floatingActionButton.hide();
                }
                // If Tab 3.
                else if (position == 2) {
                    // If idle.
                    if (positionOffset == 0) {
                        floatingActionButton.show();
                    }
                    // If dragging.
                    else if (positionOffset > 0) {
                        floatingActionButton.hide();
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                // DO NOTHING.
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // DO NOTHING.
            }
        });
    }

    private void setupContextualActionBar() {
        actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextual_action_bar, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // If Tab 2 is selected.
                if (tabLayout.getSelectedTabPosition() == 2) {
                    CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(tabLayout.getSelectedTabPosition());

                    MenuItem selectAllItem = mode.getMenu().findItem(R.id.context_bar_select_all_item);
                    MenuItem clearSelectionItem = mode.getMenu().findItem(R.id.context_bar_clear_selection_item);

                    switch (item.getItemId()) {
                        case R.id.context_bar_delete_item:
                            new DeleteDialogFragment().show(getSupportFragmentManager(), "delete");
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
            public void onDestroyActionMode(ActionMode mode) {
                // If Tab 2 is selected.
                if (tabLayout.getSelectedTabPosition() == 2) {
                    changeBarsColor(true);

                    CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(tabLayout.getSelectedTabPosition());
                    fragment.contextFinish();
                    actionMode = null;
                }
            }
        };
    }

    private void doPositiveClick() {
        CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(tabLayout.getSelectedTabPosition());

        fragment.contextDeleteSelectedItems();
        disableContextualActionBar();

        // Inform user.
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Tags deleted", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void disableContextualActionBar() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void setupAddActionButton() {
        CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(2);
        fragment.setupFloatingActionButton(floatingActionButton);

        if (TransitionHelper.isTransitionSupported()) {
            ViewGroup sceneRoot = drawerLayout;
            ViewGroup revealContent = (ViewGroup) findViewById(R.id.main_reveal_content);
            fragment.setupTransitionViews(sceneRoot, revealContent);
        }
    }

    private void registerCreateGameFragmentListener() {
        // Listen for CreateGameFragment's events.
        CreateGameFragment fragment = (CreateGameFragment) adapter.getFragment(2);
        fragment.setOnContextualActionBarEnterListener(new CreateGameFragment.OnContextualActionBarEnterListener() {
            @Override
            public void onItemLongClicked() {
                if (actionMode == null) {
                    changeBarsColor(false);

                    // Start contextual action mode.
                    actionMode = toolbar.startActionMode(actionModeCallback);
                }
            }

            @Override
            public void onItemClicked(int tagsSelected) {
                actionMode.setTitle(tagsSelected + " selected");

                MenuItem deleteItem = actionMode.getMenu().findItem(R.id.context_bar_delete_item);
                // If there are no selected tags.
                if (tagsSelected == 0) {
                    deleteItem.setVisible(false);
                } else {
                    deleteItem.setVisible(true);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void changeBarsColor(boolean isContextualActionBarVisible) {
        if (isContextualActionBarVisible) {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
                getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
            }

            tabLayout.setTabTextColors(getResources().getColorStateList(R.color.selector_tab_normal));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.primary));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.accent_dark));
            }
            tabLayout.setTabTextColors(getResources().getColorStateList(R.color.selector_tab_activated));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.accent));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primary));
        }
    }

    // Used for transitions.
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (!TransitionHelper.isTransitionEnabled)
            return;

        // If Tab 1.
        if (tabLayout.getSelectedTabPosition() == 0) {
            TrackingGameFragment fragment = (TrackingGameFragment) adapter.getFragment(0);
            fragment.prepareReenterTransition(data);
        }
    }

    private class TabsAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();

        public TabsAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);

            // Register listeners for each fragment.
            if (fragment instanceof CreateGameFragment) {
                registerCreateGameFragmentListener();
                setupAddActionButton();
            }

            return fragment;
        }

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }

        public Fragment getFragment(int position) {
            return fragments.get(position);
        }
    }

    public static class DeleteDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Delete tags?")
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // DO NOTHING.
                        }
                    })
                    .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MainActivity) getActivity()).doPositiveClick();
                        }
                    })
                    .create();
        }
    }
}
