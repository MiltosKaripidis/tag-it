package com.karhades.tag_it.main.controller.fragment;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
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
import com.karhades.tag_it.utils.FontCache;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TransitionHelper;
import com.squareup.picasso.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class TrackingTagFragment extends Fragment {

    /**
     * Extras constants.
     */
    private static final String EXTRA_TAG_ID = "com.karhades.tag_it.tag_id";
    private static final String EXTRA_FILE_PATH = "com.karhades.tag_it.file_path";

    /**
     * Instance variable.
     */
    private NfcTag nfcTag;

    /**
     * Widget references.
     */
    private ImageView pictureImageView;
    private TextView difficultyTextView;
    private CheckBox solvedCheckBox;
    private TextView dateSolvedTextView;
    private Toolbar toolbar;
    private FloatingActionButton fullscreenActionButton;

    /**
     * Transition variable.
     */
    private ViewGroup revealContent;

    /**
     * Return a TrackingTagFragment with tagId as its argument.
     * It must be called after the fragment is created and before it is added to the hosting activity.
     *
     * @param tagId A String containing the NfcTag ID.
     * @return A Fragment with the above arguments.
     */
    public static TrackingTagFragment newInstance(String tagId) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TAG_ID, tagId);

        TrackingTagFragment fragment = new TrackingTagFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain the fragment through configuration change.
        setRetainInstance(true);

        // Tell the FragmentManager that this fragment should receive
        // a call to onCreateOptionsMenu.
        setHasOptionsMenu(true);

        getFragmentArguments();

        if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
            enableTransitions();
        }
    }

    @TargetApi(21)
    @Override
    public void onStart() {
        super.onStart();

        loadImage();
    }

    private void loadImage() {
        if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
            // Register a callback to be invoked when the image has been loaded
            // to inform the activity to start the shared element transition.
            Callback picassoCallback = new Callback() {
                @Override
                public void onSuccess() {
                    pictureImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            pictureImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                            getActivity().startPostponedEnterTransition();

                            return true;
                        }
                    });
                }

                @Override
                public void onError() {
                    Log.e("TrackingTagFragment", "There was an error loading image with Picasso");
                }
            };
            PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), pictureImageView, picassoCallback);
        }
        // No transitions.
        else {
            PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), pictureImageView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
        hideCircularReveal();
    }

    private void updateUI() {
        difficultyTextView.setText(nfcTag.getDifficulty());
        solvedCheckBox.setChecked(nfcTag.isSolved());
        if (nfcTag.getDateSolved() != null) {
            dateSolvedTextView.setText(nfcTag.getDateSolved());
        }
    }

    private void hideCircularReveal() {
        if (TransitionHelper.isTransitionEnabled) {
            // Hide the reveal content view.
            if (revealContent.getVisibility() == View.VISIBLE) {
                TransitionHelper.circularHide(fullscreenActionButton, revealContent, new Runnable() {
                    @Override
                    public void run() {
                        showActionButton();
                    }
                });
            }
        }
    }

    /**
     * Solve the NfcTag with the given tag id.
     *
     * @param tagId The id of the NfcTag to solve.
     */
    public void solveNfcTag(String tagId) {
        // Get tag from list and solve.
        NfcTag nfcTag = MyTags.get(getActivity()).getNfcTag(tagId);
        nfcTag.setSolved(true);

        // Format the Date into custom string.
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM (HH:mm)", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(date);
        nfcTag.setDateSolved(formattedDate);

//        // Play winning sound.
//        Intent audioService = new Intent(getActivity(), AudioService.class);
//        getActivity().startService(audioService);

        // Inform user.
        View parentView = getView();
        if (parentView != null) {
            Snackbar snackbar = Snackbar.make(parentView, nfcTag.getTitle() + " solved", Snackbar.LENGTH_INDEFINITE);
            snackbar.setActionTextColor(getResources().getColor(R.color.accent));
            snackbar.setAction("DISMISS", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // DO NOTHING.
                }
            });
            snackbar.show();
        }
        updateUI();
    }

    private void getFragmentArguments() {
        // Get the tag ID either from the TrackingGameFragment or
        // the NFC tag Discovery.
        String tagId = getArguments().getString(EXTRA_TAG_ID);

        // Get the nfcTag through it's tag id from the arguments.
        nfcTag = MyTags.get(getActivity()).getNfcTag(tagId);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking_tag2, container, false);

        setupToolbar(view);
        setupCollapsingToolbar(view);
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
        toolbar = (Toolbar) view.findViewById(R.id.tracking_tool_bar);

        // Retrieve an AppCompatActivity hosting activity to get the supported actionbar.
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        // Set the toolbar as the new actionbar.
        activity.setSupportActionBar(toolbar);

        // Get the action bar.
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            // Display the caret for an ancestral navigation.
            if (NavUtils.getParentActivityName(getActivity()) != null)
                actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupCollapsingToolbar(View view) {
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_tool_bar_layout);
        collapsingToolbarLayout.setTitle(nfcTag.getTitle());
    }

    private void setupFloatingActionButton(View view) {
        fullscreenActionButton = (FloatingActionButton) view.findViewById(R.id.full_screen_action_button);
        fullscreenActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TransitionHelper.isTransitionEnabled) {
                    hideActionButton();
                }

                if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
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
        TransitionHelper.circularShow(fullscreenActionButton, revealContent, new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), FullScreenActivity.class);
                String filePath = nfcTag.getPictureFilePath();
                intent.putExtra(EXTRA_FILE_PATH, filePath);
                // Deactivate the default transitions for a better circular reveal experience.
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), null).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        });
    }

    private void startFullScreenActivity() {
        Intent intent = new Intent(getActivity(), FullScreenActivity.class);
        String filePath = nfcTag.getPictureFilePath();
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);
    }

    /**
     * Initialize the Widgets and wire custom fonts to them.
     *
     * @param view A view needed for the findViewById() method.
     */
    private void initializeWidgets(View view) {
        // Custom Fonts.
        Typeface typefaceTitle = FontCache.get("fonts/capture_it.ttf", getActivity());
        Typeface typefaceNormal = FontCache.get("fonts/amatic_normal.ttf", getActivity());

        // NfcTag Picture ImageView.
        pictureImageView = (ImageView) view.findViewById(R.id.tracking_image_view);
        if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
            pictureImageView.setTransitionName("image" + nfcTag.getTagId());
        }

        // NfcTag Details Title TextView.
        TextView detailsTextView = (TextView) view.findViewById(R.id.tracking_details_text_view);
        detailsTextView.setTypeface(typefaceTitle);

        // NfcTag Difficulty Label TextView.
        TextView difficultyLabelTextView = (TextView) view.findViewById(R.id.tracking_difficulty_label_text_view);
        difficultyLabelTextView.setTypeface(typefaceNormal);

        // NfcTag Difficulty TextView.
        difficultyTextView = (TextView) view.findViewById(R.id.tracking_difficulty_text_view);
        difficultyTextView.setTypeface(typefaceNormal);

        // NfcTag Solved Label TextView.
        TextView solvedLabelTextView = (TextView) view.findViewById(R.id.tracking_solved_label_text_view);
        solvedLabelTextView.setTypeface(typefaceNormal);

        // NfcTag Solved CheckBox.
        solvedCheckBox = (CheckBox) view.findViewById(R.id.tracking_solved_check_box);

        // NfcTag Date Label TextView.
        TextView dateSolvedLabelTextView = (TextView) view.findViewById(R.id.tracking_date_solved_label_text_view);
        dateSolvedLabelTextView.setTypeface(typefaceNormal);

        // NfcTag Date Solved TextView.
        dateSolvedTextView = (TextView) view.findViewById(R.id.tracking_date_solved_text_view);
        dateSolvedTextView.setTypeface(typefaceNormal);

        // A hidden FrameLayout that will cover the whole screen on transition start.
        revealContent = (ViewGroup) view.findViewById(R.id.tracking_reveal_content);
    }

    private void showActionButton() {
        fullscreenActionButton.animate()
                .scaleX(1)
                .scaleY(1);
    }

    /**
     * Called from TrackingTagPagerActivity to hide the action
     * button before finishing the fragment.
     *
     * @param runnable The runnable to run after the hide animation
     *                 of the action button.
     */
    public void hideActionButtonOnExit(Runnable runnable) {
        fullscreenActionButton.animate()
                .scaleX(0)
                .scaleY(0)
                .withEndAction(runnable);
    }

    private void hideActionButton() {
        fullscreenActionButton.setScaleX(0);
        fullscreenActionButton.setScaleY(0);
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
            return getView().findViewById(R.id.tracking_image_view);
        }
        return null;
    }
}
