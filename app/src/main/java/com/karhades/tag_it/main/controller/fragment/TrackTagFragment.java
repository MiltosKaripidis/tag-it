/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.fragment;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.activity.FullScreenActivity;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TransitionHelper;
import com.squareup.picasso.Callback;

/**
 * Controller Fragment class that binds the tracking of the tags with the data set.
 */
public class TrackTagFragment extends Fragment {

    /**
     * Extras constants.
     */
    private static final String EXTRA_TAG_ID = "com.karhades.tag_it.tag_id";
    private static final String EXTRA_FILE_PATH = "com.karhades.tag_it.file_path";

    /**
     * Instance variable.
     */
    private NfcTag mNfcTag;

    /**
     * Widget references.
     */
    private ImageView mPictureImageView;
    private TextView mDifficultyTextView;
    private CheckBox mDiscoveredCheckBox;
    private TextView mDateDiscoveredTextView;
    private Toolbar mToolbar;
    private FloatingActionButton mFullscreenActionButton;

    /**
     * Transition variable.
     */
    private ViewGroup mRevealContent;

    /**
     * Returns a TrackTagFragment with tagId as its argument.
     * It must be called after the fragment is created and before it is added to the hosting activity.
     *
     * @param tagId A String containing the NfcTag ID.
     * @return A Fragment with the above arguments.
     */
    public static TrackTagFragment newInstance(String tagId) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TAG_ID, tagId);

        TrackTagFragment fragment = new TrackTagFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getFragmentArguments();

        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            enableTransitions();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        loadImage();
    }

    private void loadImage() {
        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            // Registers a callback to be invoked when the image has been loaded
            // to inform the activity to start the shared element transition.
            Callback picassoCallback = new Callback() {
                @Override
                public void onSuccess() {
                    mPictureImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            mPictureImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                            getActivity().startPostponedEnterTransition();

                            return true;
                        }
                    });
                }

                @Override
                public void onError() {
                    Log.e("TrackTagFragment", "There was an error loading image with Picasso");
                }
            };
            PictureLoader.loadBitmapWithPicasso(getActivity(), mNfcTag.getPictureFilePath(), mPictureImageView, picassoCallback);
        }
        // No transitions.
        else {
            PictureLoader.loadBitmapWithPicasso(getActivity(), mNfcTag.getPictureFilePath(), mPictureImageView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
        hideCircularReveal();
    }

    private void updateUI() {
        mToolbar.setTitle(mNfcTag.getTitle());
        mDifficultyTextView.setText(mNfcTag.getDifficulty());
        mDiscoveredCheckBox.setChecked(mNfcTag.isDiscovered());
        if (mNfcTag.getDateDiscovered() != null) {
            mDateDiscoveredTextView.setText(mNfcTag.getDateDiscovered());
        }
    }

    private void hideCircularReveal() {
        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            // Hide the reveal content view.
            if (mRevealContent.getVisibility() == View.VISIBLE) {
                TransitionHelper.circularHide(mFullscreenActionButton, mRevealContent, new Runnable() {
                    @Override
                    public void run() {
                        // DO NOTHING
                    }
                });
            }
        }
    }

    private void getFragmentArguments() {
        // Get the tag ID either from the TrackGameFragment or
        // the NFC tag Discovery.
        String tagId = getArguments().getString(EXTRA_TAG_ID);

        // Get the nfcTag through it's tag id from the arguments.
        mNfcTag = MyTags.get(getActivity()).getNfcTag(tagId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_tag, container, false);

        setupToolbar(view);
        setupFloatingActionButton(view);
        initializeWidgets(view);

        return view;
    }

    /**
     * Helper method for setting up the tool bar.
     *
     * @param view A view needed for the findViewById() method.
     */
    private void setupToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.track_tag_tool_bar);

        // Retrieve an AppCompatActivity hosting activity to get the supported actionbar.
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        // Set the toolbar as the new actionbar.
        activity.setSupportActionBar(mToolbar);

        // Get the action bar.
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            // Display the caret for an ancestral navigation.
            if (NavUtils.getParentActivityName(getActivity()) != null)
                actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupFloatingActionButton(View view) {
        mFullscreenActionButton = (FloatingActionButton) view.findViewById(R.id.track_tag_fullscreen__floating_action_button);
        mFullscreenActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TransitionHelper.isTransitionSupportedAndEnabled()) {
                    startFullScreenActivityWithTransition();
                }
                // No transitions.
                else {
                    startFullScreenActivity();
                }
            }
        });
    }

    @TargetApi(21)
    @SuppressWarnings("NullArgumentToVariableArgMethod")
    private void startFullScreenActivityWithTransition() {
        TransitionHelper.circularShow(mFullscreenActionButton, mRevealContent, new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), FullScreenActivity.class);
                String filePath = mNfcTag.getPictureFilePath();
                intent.putExtra(EXTRA_FILE_PATH, filePath);
                // Deactivate the default transitions for a better circular reveal experience.
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), null).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        });
    }

    private void startFullScreenActivity() {
        String filePath = mNfcTag.getPictureFilePath();

        Intent intent = FullScreenActivity.newIntent(getActivity(), filePath);
        startActivity(intent);
    }

    /**
     * Initialize the Widgets and wire custom fonts to them.
     *
     * @param view A view needed for the findViewById() method.
     */
    private void initializeWidgets(View view) {
        // NfcTag Picture ImageView.
        mPictureImageView = (ImageView) view.findViewById(R.id.track_tag_image_view);
        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            mPictureImageView.setTransitionName("image" + mNfcTag.getTagId());
        }

        // NfcTag Difficulty TextView.
        mDifficultyTextView = (TextView) view.findViewById(R.id.track_tag_difficulty_text_view);

        // NfcTag discovered CheckBox.
        mDiscoveredCheckBox = (CheckBox) view.findViewById(R.id.track_tag_discovered_check_box);

        // NfcTag Date discovered TextView.
        mDateDiscoveredTextView = (TextView) view.findViewById(R.id.track_tag_date_discovered_text_view);

        // A hidden FrameLayout that will cover the whole screen on transition start.
        mRevealContent = (ViewGroup) view.findViewById(R.id.tracking_reveal_content);
    }

    private void showActionButton() {
        mFullscreenActionButton.setVisibility(View.VISIBLE);
        mFullscreenActionButton.animate()
                .scaleX(1)
                .scaleY(1);
    }

    /**
     * Called from TrackTagPagerActivity to hide the action
     * button before finishing the fragment.
     *
     * @param runnable The runnable to run after the hide animation
     *                 of the action button.
     */
    public void hideActionButtonOnExit(Runnable runnable) {
        mFullscreenActionButton.animate()
                .scaleX(0)
                .scaleY(0)
                .withEndAction(runnable);
    }

    private void hideActionButton() {
        mFullscreenActionButton.setScaleX(0);
        mFullscreenActionButton.setScaleY(0);
        mFullscreenActionButton.setVisibility(View.INVISIBLE);
    }

    @TargetApi(21)
    private void enableTransitions() {
        getActivity().getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                hideActionButton();
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                showActionButton();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                // DO NOTHING.
            }

            @Override
            public void onTransitionPause(Transition transition) {
                // DO NOTHING.
            }

            @Override
            public void onTransitionResume(Transition transition) {
                // DO NOTHING.
            }
        });
    }

    /**
     * Returns the shared element that should be transitioned back to the previous Activity,
     * or null if the view is not visible on screen.
     */
    public View getSharedElement() {
        if (getView() != null) {
            return getView().findViewById(R.id.track_tag_image_view);
        }
        return null;
    }
}
