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
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.adapter.MainAdapter;
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
    private Bundle mBundle;

    /**
     * Widget references.
     */
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FloatingActionButton mFloatingActionButton;
    private ProgressBar mProgressBar;

    /**
     * Instance variables.
     */
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;
    private boolean mIsSelectAllItemVisible;
    private TrackGameFragment mTrackGameFragment;
    private CreateGameFragment mCreateGameFragment;
    private AsyncTaskLoader mAsyncTaskLoader;

    /**
     * NFC adapter.
     */
    private NfcHandler mNfcHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
     * Loads the tags asynchronously from the external storage.
     */
    private void loadTags() {
        mAsyncTaskLoader = new AsyncTaskLoader();
        mAsyncTaskLoader.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        disableContextualActionBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mCreateGameFragment == null) {
            return;
        }

        mCreateGameFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mNfcHandler.handleAndroidBeamReceivedFiles(intent);
    }

    @Override
    public void onBackPressed() {
        // Close contextual action bar.
        disableContextualActionBar();

        // Close NavigationView.
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
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
                mDrawerLayout.openDrawer(mNavigationView);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNFC() {
        mNfcHandler = new NfcHandler();
        mNfcHandler.setupNfcHandler(this);
        mNfcHandler.registerAndroidBeamShareFiles();
        mNfcHandler.handleAndroidBeamReceivedFiles(getIntent());
    }

    private void setupProgressBar() {
        mProgressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
    }

    private void setupFloatingActionButton() {
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.main_floating_action_button);
    }

    private void setupCoordinatorLayout() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);
    }

    @SuppressWarnings("ConstantConditions")
    private void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.main_navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                mDrawerLayout.closeDrawer(mNavigationView);

                if (item.getGroupId() == R.id.group_primary) {
                    mNavigationView.setCheckedItem(item.getItemId());
                }

                switch (item.getItemId()) {
                    case R.id.navigation_tracking:
                        mTabLayout.getTabAt(0).select();
                        return true;

                    case R.id.navigation_share:
                        mTabLayout.getTabAt(1).select();
                        return true;

                    case R.id.navigation_create:
                        mTabLayout.getTabAt(2).select();
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
        mToolbar = (Toolbar) findViewById(R.id.main_tool_bar);
        // Substitute the action bar for this toolbar.
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), R.drawable.vector_menu, null);
            actionBar.setHomeAsUpIndicator(vectorDrawableCompat);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupAppBarLayout() {
        mAppBarLayout = (AppBarLayout) findViewById(R.id.main_app_bar_layout);
    }

    @SuppressWarnings("deprecation, ConstantConditions")
    private void setupTabLayout() {
        mTabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        mTabLayout.setTabTextColors(getResources().getColorStateList(R.color.selector_tab_normal));
        mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));
        mTabLayout.setupWithViewPager(mViewPager);
        // Set tab names.
        String[] tabNames = getResources().getStringArray(R.array.tab_names);
        for (int i = 0; i < tabNames.length; i++) {
            mTabLayout.getTabAt(i).setText(tabNames[i]);
        }
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());

                Menu menu = mNavigationView.getMenu();
                mNavigationView.setCheckedItem(menu.getItem(tab.getPosition()).getItemId());

                mAppBarLayout.setExpanded(true, true);
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
        // Creates an ArrayList with the fragments.
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(TrackGameFragment.newInstance());
        fragments.add(ShareGameFragment.newInstance());
        fragments.add(CreateGameFragment.newInstance());

        MainAdapter adapter = new MainAdapter(getSupportFragmentManager(), fragments);

        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // If Tab 1.
                if (position == 0) {
                    mFloatingActionButton.hide();
                }
                // If Tab 2.
                else if (position == 1) {
                    mFloatingActionButton.hide();
                }
                // If Tab 3.
                else if (position == 2) {
                    // If idle.
                    if (positionOffset == 0) {
                        mFloatingActionButton.show();
                    }
                    // If dragging.
                    else if (positionOffset > 0) {
                        mFloatingActionButton.hide();
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
        mActionModeCallback = new ActionMode.Callback() {
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

                if (mIsSelectAllItemVisible) {
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
                if (mCreateGameFragment != null) {
                    switch (item.getItemId()) {
                        case R.id.context_bar_delete_item:
                            int size = mCreateGameFragment.contextGetSelectionSize();
                            DeleteDialogFragment.newInstance(size).show(getSupportFragmentManager(), "delete");
                            return true;
                        case R.id.context_bar_select_all_item:
                            mCreateGameFragment.contextSelectAll();

                            // Invalidate menu item.
                            mIsSelectAllItemVisible = true;
                            mode.invalidate();
                            return true;
                        case R.id.context_bar_clear_selection_item:
                            mCreateGameFragment.contextClearSelection();

                            // Invalidate menu item.
                            mIsSelectAllItemVisible = false;

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
                if (mCreateGameFragment == null) {
                    return;
                }

                changeBarsColor(true);

                mCreateGameFragment.contextFinish();

                // Invalidate menu item.
                mIsSelectAllItemVisible = false;
                mode.invalidate();

                mActionMode = null;
            }
        };
    }

    private void doPositiveClick() {
        int size = mCreateGameFragment.contextGetSelectionSize();

        mCreateGameFragment.contextDeleteSelectedItems();
        disableContextualActionBar();

        String deletionSize = getResources().getQuantityString(R.plurals.snackbar_deleted_plural, size);
        // Inform user.
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, deletionSize, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void disableContextualActionBar() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    /*
     * CreateGameFragment callbacks.
     */
    @Override
    public void onItemLongClicked() {
        if (mActionMode == null) {
            // Start contextual action mode.
            mActionMode = mToolbar.startActionMode(mActionModeCallback);
        }
    }

    @Override
    public void onItemClicked(int tagsSelected) {
        mActionMode.setTitle(tagsSelected + " selected");

        MenuItem deleteItem = mActionMode.getMenu().findItem(R.id.context_bar_delete_item);
        // If there are no selected tags.
        if (tagsSelected == 0) {
            deleteItem.setVisible(false);
        } else {
            deleteItem.setVisible(true);
        }
    }

    @Override
    public void onFragmentAttached(CreateGameFragment fragment) {
        mCreateGameFragment = fragment;
        mCreateGameFragment.setupFloatingActionButton(mFloatingActionButton);
    }

    @Override
    public void onItemDeleted(String title) {
        // Inform user.
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, title + " deleted", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /*
     * TrackGameFragment callbacks.
     */
    @Override
    public void onFragmentResumed(TrackGameFragment fragment) {
        mTrackGameFragment = fragment;

        // If fragment is created before the AsyncTask finishes, the UI update should be done on the
        // onPostExecute method to avoid duplicate updates.
        if (mAsyncTaskLoader.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }

        mProgressBar.setVisibility(View.GONE);
        mTrackGameFragment.updateUi();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(21)
    private void changeBarsColor(boolean isContextualActionBarVisible) {
        if (isContextualActionBarVisible) {
            mTabLayout.setTabTextColors(getResources().getColorStateList(R.color.selector_tab_normal));
            mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));

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
            mTabLayout.setTabTextColors(getResources().getColorStateList(R.color.selector_tab_activated));
            mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primary));

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
        ObjectAnimator objectAnimator = ObjectAnimator.ofArgb(mTabLayout, "backgroundColor", colorFrom, colorTo);
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

        if (data == null) {
            return;
        }

        // If Tab 1.
        if (mTrackGameFragment != null) {
            final RecyclerView recyclerView = mTrackGameFragment.getRecyclerView();

            mBundle = new Bundle(data.getExtras());

            int oldTagPosition = data.getIntExtra(EXTRA_OLD_TAG_POSITION, -1);
            int currentTagPosition = data.getIntExtra(EXTRA_CURRENT_TAG_POSITION, -1);

            // If user swiped to another tag.
            if (oldTagPosition != currentTagPosition) {
                recyclerView.scrollToPosition(currentTagPosition);
            }

            // Wait for RecyclerView to load it's layout.
            supportPostponeEnterTransition();
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    supportStartPostponedEnterTransition();
                    return true;
                }
            });
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
                final RecyclerView recyclerView = mTrackGameFragment.getRecyclerView();

                // If TrackTagPagerActivity returns to MainActivity.
                if (mBundle != null) {
                    int oldTagPosition = mBundle.getInt(EXTRA_OLD_TAG_POSITION);
                    int currentTagPosition = mBundle.getInt(EXTRA_CURRENT_TAG_POSITION);

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
                    mBundle = null;
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
     * AsyncTask class that loads the json file in a background thread.
     */
    private class AsyncTaskLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
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
            if (mTrackGameFragment == null) {
                return;
            }

            mProgressBar.setVisibility(View.GONE);
            mTrackGameFragment.updateUi();
        }
    }
}
