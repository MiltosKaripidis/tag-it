/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.SharedElementCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.fragment.CreateGameFragment;
import com.karhades.tag_it.main.controller.fragment.ShareGameFragment;
import com.karhades.tag_it.main.controller.fragment.TrackGameFragment;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcHandler;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.TransitionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main Controller Activity class that hosts 3 fragments (TrackGameFragment, ShareGameFragment,
 * CreateGameFragment) that each corresponds to a tab. Manages the Android Beam operation.
 */
public class MainActivity extends AppCompatActivity implements TrackGameFragment.Callbacks, CreateGameFragment.Callbacks {

    /**
     * Extras constants.
     */
    private static final String EXTRA_CURRENT_TAG_POSITION = "com.karhades.tag_it.current_tag_position";
    private static final String EXTRA_OLD_TAG_POSITION = "com.karhades.tag_it.old_tag_position";

    /**
     * Transition variables.
     */
    private Bundle bundle;

    /**
     * Widget references.
     */
    private DrawerLayout drawerLayout;
    private CoordinatorLayout coordinatorLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton floatingActionButton;
    private ProgressBar progressBar;

    /**
     * Instance variables.
     */
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;
    private boolean isSelectAllItemVisible;
    private TrackGameFragment trackGameFragment;
    private CreateGameFragment createGameFragment;
    private AsyncTaskLoader asyncTaskLoader;

    /**
     * NFC adapter.
     */
    private NfcHandler nfcHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNFC();
        setupProgressBar();
        setupFloatingActionButton();
        setupCoordinatorLayout();
        setupNavigationDrawer();
        setupAppBarLayout();
        setupToolbar();
        setupViewPager();
        setupTabLayout();
        setupContextualActionBar();

        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            enableTransitions();
        }

        loadTags();
    }

    /**
     * Saves the tags asynchronously to external storage.
     */
    private void saveTags() {
        new AsyncTaskSaver().execute();
    }

    /**
     * Loads the tags asynchronously from the external storage.
     */
    private void loadTags() {
        asyncTaskLoader = new AsyncTaskLoader();
        asyncTaskLoader.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveTags();
        disableContextualActionBar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        nfcHandler.handleAndroidBeamReceivedFiles(intent);
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
        nfcHandler.registerAndroidBeamShareFiles();
        nfcHandler.handleAndroidBeamReceivedFiles(getIntent());
    }

    private void setupProgressBar() {
        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
    }

    private void setupFloatingActionButton() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.main_floating_action_button);
    }

    private void setupCoordinatorLayout() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);
    }

    @SuppressWarnings("ConstantConditions")
    private void setupNavigationDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.main_navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawer(navigationView);

                if (item.getGroupId() == R.id.group_primary) {
                    navigationView.setCheckedItem(item.getItemId());
                }

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
                        startSettingsActivity();
                        return false;

                    default:
                        return false;
                }
            }
        });
    }

    private void startSettingsActivity() {
        Intent intent = SettingsActivity.newIntent(MainActivity.this);
        startActivity(intent);
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

    private void setupAppBarLayout() {
        appBarLayout = (AppBarLayout) findViewById(R.id.main_app_bar_layout);
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

                appBarLayout.setExpanded(true, true);
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
        TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager());
        adapter.addFragment(TrackGameFragment.newInstance());
        adapter.addFragment(ShareGameFragment.newInstance());
        adapter.addFragment(CreateGameFragment.newInstance());

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
                changeBarsColor(false);

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextual_action_bar, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem selectAllItem = menu.findItem(R.id.context_bar_select_all_item);
                MenuItem clearSelectionItem = menu.findItem(R.id.context_bar_clear_selection_item);

                if (isSelectAllItemVisible) {
                    selectAllItem.setVisible(false);
                    clearSelectionItem.setVisible(true);
                } else {
                    selectAllItem.setVisible(true);
                    clearSelectionItem.setVisible(false);
                }

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // If Tab 2 is selected.
                if (createGameFragment != null) {
                    switch (item.getItemId()) {
                        case R.id.context_bar_delete_item:
                            int size = createGameFragment.contextGetSelectionSize();
                            DeleteDialogFragment.newInstance(size).show(getSupportFragmentManager(), "delete");
                            return true;
                        case R.id.context_bar_select_all_item:
                            createGameFragment.contextSelectAll();

                            // Invalidate menu item.
                            isSelectAllItemVisible = true;
                            mode.invalidate();
                            return true;
                        case R.id.context_bar_clear_selection_item:
                            createGameFragment.contextClearSelection();

                            // Invalidate menu item.
                            isSelectAllItemVisible = false;

                            mode.invalidate();
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
                if (createGameFragment == null) {
                    return;
                }

                changeBarsColor(true);

                createGameFragment.contextFinish();

                // Invalidate menu item.
                isSelectAllItemVisible = false;
                mode.invalidate();

                actionMode = null;
            }
        };
    }

    private void doPositiveClick() {
        int size = createGameFragment.contextGetSelectionSize();

        createGameFragment.contextDeleteSelectedItems();
        disableContextualActionBar();

        String deletionSize = getResources().getQuantityString(R.plurals.snackbar_deleted_plural, size);
        // Inform user.
        Snackbar snackbar = Snackbar.make(coordinatorLayout, deletionSize, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void disableContextualActionBar() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    /*
     * CreateGameFragment callbacks.
     */
    @Override
    public void onItemLongClicked() {
        if (actionMode == null) {
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

    @Override
    public void onFragmentAttached(CreateGameFragment fragment) {
        createGameFragment = fragment;
        createGameFragment.setupFloatingActionButton(floatingActionButton);

        ViewGroup sceneRoot = drawerLayout;
        ViewGroup revealContent = (ViewGroup) findViewById(R.id.main_reveal_content);
        createGameFragment.setupTransitionViews(sceneRoot, revealContent);
    }

    @Override
    public void onItemDeleted(String title) {
        // Inform user.
        Snackbar snackbar = Snackbar.make(coordinatorLayout, title + " deleted", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /*
     * TrackGameFragment callbacks.
     */
    @Override
    public void onFragmentResumed(TrackGameFragment fragment) {
        trackGameFragment = fragment;

        // If fragment is created before the AsyncTask finishes, the UI update should be done on the
        // onPostExecute method to avoid duplicate updates.
        if (asyncTaskLoader.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }

        progressBar.setVisibility(View.GONE);
        trackGameFragment.updateUi();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(21)
    private void changeBarsColor(boolean isContextualActionBarVisible) {
        if (isContextualActionBarVisible) {
            tabLayout.setTabTextColors(getResources().getColorStateList(R.color.selector_tab_normal));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));

            animateStatusBar(getResources().getColor(R.color.accent_dark), getResources().getColor(R.color.primary_dark));
            animateTabLayout(getResources().getColor(R.color.accent), getResources().getColor(R.color.primary));

            // TODO: Temporary fix.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
                }
            }, 400);
        } else {
            tabLayout.setTabTextColors(getResources().getColorStateList(R.color.selector_tab_activated));
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primary));

            animateStatusBar(getResources().getColor(R.color.primary_dark), getResources().getColor(R.color.accent_dark));
            animateTabLayout(getResources().getColor(R.color.primary), getResources().getColor(R.color.accent));
        }
    }

    @TargetApi(21)
    private void animateStatusBar(int colorFrom, int colorTo) {
        final ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
    }

    @TargetApi(21)
    private void animateTabLayout(int colorFrom, int colorTo) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofArgb(tabLayout, "backgroundColor", colorFrom, colorTo);
        objectAnimator.setDuration(300);
        objectAnimator.setStartDelay(20);
        objectAnimator.start();
    }

    // Used for transitions.
    @Override
    @TargetApi(21)
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (!TransitionHelper.isTransitionSupportedAndEnabled())
            return;

        // If Tab 1.
        if (trackGameFragment != null) {
            final RecyclerView recyclerView = trackGameFragment.getRecyclerView();

            bundle = new Bundle(data.getExtras());

            int oldTagPosition = data.getIntExtra(EXTRA_OLD_TAG_POSITION, -1);
            int currentTagPosition = data.getIntExtra(EXTRA_CURRENT_TAG_POSITION, -1);

            // If user swiped to another tag.
            if (oldTagPosition != currentTagPosition) {
                recyclerView.scrollToPosition(currentTagPosition);

                // Wait for RecyclerView to load it's layout.
                postponeEnterTransition();
                recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
            }
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

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }
    }

    public static class DeleteDialogFragment extends DialogFragment {

        private static final String EXTRA_SIZE = "com.karhades.tag_it.selectionSize";
        private int selectionSize;

        public static DeleteDialogFragment newInstance(int size) {
            DeleteDialogFragment fragment = new DeleteDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_SIZE, size);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            selectionSize = getArguments().getInt(EXTRA_SIZE);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getResources().getQuantityString(R.plurals.dialog_deleted_plural, selectionSize))
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

    @TargetApi(21)
    private void enableTransitions() {
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                final RecyclerView recyclerView = trackGameFragment.getRecyclerView();

                // If TrackTagPagerActivity returns to MainActivity.
                if (bundle != null) {
                    int oldTagPosition = bundle.getInt(EXTRA_OLD_TAG_POSITION);
                    int currentTagPosition = bundle.getInt(EXTRA_CURRENT_TAG_POSITION);

                    // If currentPosition != oldPosition the user must have swiped to a different
                    // page in the ViewPager. We must update the shared element so that the
                    // correct one falls into place.
                    if (currentTagPosition != oldTagPosition) {

                        // Get the transition name of the current tag.
                        NfcTag nfcTag = MyTags.get(MainActivity.this).getNfcTags().get(currentTagPosition);
                        String currentTransitionName = "image" + nfcTag.getTagId();

                        // Get the ImageView from the RecyclerView.
                        View currentSharedImageView = recyclerView.findViewWithTag(currentTransitionName);
                        // If it exists.
                        if (currentSharedImageView != null) {
                            // Clear the previous (original) ImageView registrations.
                            names.clear();
                            sharedElements.clear();

                            // Add the current ImageView.
                            names.add(currentTransitionName);
                            sharedElements.put(currentTransitionName, currentSharedImageView);
                        }
                    }
                    // Delete the previous positions.
                    bundle = null;
                } else {
                    //TODO: When status bar is transparent, it is null and imageView overlaps it.
                    // If bundle is null, then the activity is exiting.
                    View statusBar = findViewById(android.R.id.statusBarBackground);
                    // Add the NavigationBar to shared elements to avoid blinking.
                    View navigationBar = findViewById(android.R.id.navigationBarBackground);

                    if (statusBar != null) {
                        names.add(statusBar.getTransitionName());
                        sharedElements.put(statusBar.getTransitionName(), statusBar);
                    }
                    if (navigationBar != null) {
                        names.add(navigationBar.getTransitionName());
                        sharedElements.put(navigationBar.getTransitionName(), navigationBar);
                    }
                }
            }
        });
    }

    /**
     * AsyncTask class that saves the json file in a background thread.
     */
    private class AsyncTaskSaver extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            MyTags.get(MainActivity.this).saveTags();

            return null;
        }
    }

    /**
     * AsyncTask class that loads the json file in a background thread.
     */
    private class AsyncTaskLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            MyTags.get(MainActivity.this).loadTags();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // If fragment isn't created yet, it cannot update it's UI and should be done on the
            // onResume method instead.
            if (trackGameFragment == null) {
                return;
            }

            progressBar.setVisibility(View.GONE);
            trackGameFragment.updateUi();
        }
    }
}
