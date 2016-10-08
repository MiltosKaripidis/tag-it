package com.karhades.tag_it.main.controller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcHandler;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Controller Fragment that manages the discovery of an NFC tag. Manages the NFC read operation.
 */
public class DiscoverTagFragment extends Fragment {

    /**
     * NFC adapter.
     */
    private NfcHandler mNfcHandler;

    /**
     * Instance variable.
     */
    private NfcTag mNfcTag;
    private String mDiscoveredTagId;

    /**
     * Widget references.
     */
    private Toolbar mToolbar;
    private ImageView mPictureImageView;
    private TextView mDifficultyTextView;
    private CheckBox mDiscoveredCheckBox;
    private TextView mDateDiscoveredTextView;

    public static DiscoverTagFragment newInstance() {
        return new DiscoverTagFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        loadTags();
        setupNfcHandler();

        // Gets the corresponding NfcTag object from the discovered tag ID.
        mNfcTag = MyTags.get(getActivity()).getNfcTag(mDiscoveredTagId);
    }

    /**
     * Loads the tags from the external storage.
     */
    private void loadTags() {
        MyTags.get(getActivity()).loadTags();
    }

    private void setupNfcHandler() {
        mNfcHandler = new NfcHandler();
        mNfcHandler.setupNfcHandler(getActivity());
        mDiscoveredTagId = mNfcHandler.handleNfcTagRead(getActivity().getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(getActivity());
                if (getActivity().isTaskRoot()) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(getActivity())
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(getActivity(), upIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover_tag, container, false);

        setupToolbar(view);
        initializeWidgets(view);

        return view;
    }

    /**
     * Helper method for setting up the tool bar.
     *
     * @param view A view needed for the findViewById() method.
     */
    private void setupToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.discover_tool_bar);

        // Retrieves an AppCompatActivity hosting activity to get the supported actionbar.
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        // Sets the toolbar as the new actionbar.
        activity.setSupportActionBar(mToolbar);

        // Gets the action bar.
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            // Displays the caret for an ancestral navigation.
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void initializeWidgets(View view) {
        mPictureImageView = (ImageView) view.findViewById(R.id.discover_image_view);
        mDifficultyTextView = (TextView) view.findViewById(R.id.discover_difficulty_text_view);
        mDiscoveredCheckBox = (CheckBox) view.findViewById(R.id.discover_discovered_check_box);
        mDateDiscoveredTextView = (TextView) view.findViewById(R.id.discover_date_discovered_text_view);
    }

    @Override
    public void onStart() {
        super.onStart();

        loadImage();
    }

    private void loadImage() {
        if (mNfcTag == null) {
            return;
        }

        PictureLoader.loadBitmap(getActivity(), mNfcTag.getPictureFilePath(), mPictureImageView);
    }

    @Override
    public void onPause() {
        super.onPause();

        saveTag();
    }

    /**
     * Saves the tag to the external storage.
     */
    private void saveTag() {
        if (mNfcTag == null) {
            return;
        }

        MyTags.get(getActivity()).updateNfcTag(mNfcTag);
    }

    @Override
    public void onResume() {
        super.onResume();

        discoverTag();
        updateUi();
    }

    private void discoverTag() {
        if (mNfcTag == null) {
            return;
        }

        // Discovers the NFC tag.
        mNfcTag.setDiscovered(true);

        // Formats the Date into a custom string.
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM (HH:mm)", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(date);
        mNfcTag.setDateDiscovered(formattedDate);

        // Informs user.
        View parentView = getView();
        if (parentView != null) {
            Snackbar snackbar = Snackbar.make(parentView, mNfcTag.getTitle() + " discovered", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void updateUi() {
        if (mNfcTag == null) {
            return;
        }

        mToolbar.setTitle(mNfcTag.getTitle());
        mDifficultyTextView.setText(mNfcTag.getDifficulty());
        mDiscoveredCheckBox.setChecked(mNfcTag.isDiscovered());
        mDateDiscoveredTextView.setText(mNfcTag.getDateDiscovered());
    }
}
