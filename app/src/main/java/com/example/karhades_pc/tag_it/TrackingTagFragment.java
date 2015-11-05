package com.example.karhades_pc.tag_it;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.karhades_pc.utils.AudioPlayer;
import com.example.karhades_pc.utils.FontCache;
import com.example.karhades_pc.utils.PictureLoader;
import com.example.karhades_pc.utils.Utils;
import com.squareup.picasso.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class TrackingTagFragment extends Fragment {

    public static final String EXTRA_TAG_ID = "com.example.karhades_pc.tag_it.tag_id";
    public static final String EXTRA_TAG_DISCOVERED = "com.example.karhades_pc.tag_it.tag_discovered";
    public static final String EXTRA_FILE_PATH = "com.example.karhades_pc.tag_it.file_path";

    private NfcTag nfcTag;
    private AudioPlayer audioPlayer;

    private ImageView imageView;
    private TextView difficultyTextView;
    private CheckBox solvedCheckBox;
    private TextView dateSolvedTextView;
    private Toolbar toolbar;
    private FloatingActionButton fullscreenActionButton;
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
        setupAudioPlayer();

        if (Utils.itSupportsTransitions()) {
            enableTransitions();
        }
    }

    @TargetApi(21)
    @Override
    public void onStart() {
        super.onStart();

        // Register a callback to be invoked when the image has been loaded
        // to inform the activity to start the shared element transition.
        Callback picassoCallback = new Callback() {
            @Override
            public void onSuccess() {
                if (Utils.itSupportsTransitions()) {
                    imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                            getActivity().startPostponedEnterTransition();
                            return true;
                        }
                    });
                }
            }

            @Override
            public void onError() {
                Log.e("TrackingTagFragment", "There was an error at loading image with Picasso");
            }
        };
        PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), imageView, picassoCallback);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
        showActionButton();

        if (revealContent.getVisibility() == View.VISIBLE) {
            circularHide();
        }
    }

    private void updateUI() {
        difficultyTextView.setText(nfcTag.getDifficulty());
        solvedCheckBox.setChecked(nfcTag.isSolved());
        if (nfcTag.getDateSolved() != null) {
            dateSolvedTextView.setText(nfcTag.getDateSolved());
        }
    }

    private void setupAudioPlayer() {
        audioPlayer = new AudioPlayer();
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM (HH:mm:ss)");
        String formattedDate = simpleDateFormat.format(date);
        nfcTag.setDateSolved(formattedDate);

        // Play winning sound.
        // TODO: Uncomment the cheering sound.
        //audioPlayer.play(context, R.raw.cheering);

        // Inform user.
        Snackbar snackbar = Snackbar.make(getView(), nfcTag.getTitle() + " was successfully solved!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.accent));
        snackbar.setAction("DISMISS", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DO NOTHING.
            }
        });
        snackbar.show();
        updateUI();
    }

    private void getFragmentArguments() {
        // Get the tag ID either from the TrackingGameFragment (onListClick) or
        // the NFC tag Discovery.
        String tagId = getArguments().getString(EXTRA_TAG_ID);

        // Get the nfcTag through it's tag id from the arguments.
        nfcTag = MyTags.get(getActivity()).getNfcTag(tagId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If there is a parent activity, navigate to it.
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking_tag, container, false);

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
            if (nfcTag != null) {
                actionBar.setTitle(nfcTag.getTitle());
            }
        }
    }

    private void setupFloatingActionButton(View view) {
        fullscreenActionButton = (FloatingActionButton) view.findViewById(R.id.full_screen_action_button);
        fullscreenActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullscreenActionButton.animate()
                        .scaleX(0)
                        .scaleY(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                if (Utils.itSupportsTransitions()) {
                                    circularShow();
                                } else {
                                    enterFullScreen();
                                }
                            }
                        });
            }
        });
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
        imageView = (ImageView) view.findViewById(R.id.tracking_image_view);
        if (Utils.itSupportsTransitions()) {
            imageView.setTransitionName("image" + nfcTag.getTagId());
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

        revealContent = (ViewGroup) view.findViewById(R.id.tracking_reveal_content);
    }

    private void enterFullScreen() {
        Intent intent = new Intent(getActivity(), FullScreenActivity.class);
        String filePath = nfcTag.getPictureFilePath();
        intent.putExtra(EXTRA_FILE_PATH, filePath);

        if (Utils.itSupportsTransitions()) {
            // Deactivate the default transitions for a better circular reveal experience.
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), null).toBundle();
            getActivity().startActivity(intent, bundle);
        }
        // No transitions.
        else {
            startActivity(intent);
        }
    }

    private void showActionButton() {
        // Floating Action Button animation on show after a period of time.
        if (fullscreenActionButton.getScaleX() == 0 && fullscreenActionButton.getScaleY() == 0) {
            fullscreenActionButton.animate()
                    .setStartDelay(700)
                    .scaleX(1)
                    .scaleY(1);
        }
    }

    /**
     * Called from TrackingTagPagerActivity to hide the action
     * button before finishing the fragment.
     *
     * @param runnable The runnable to run after the hide animation
     *                 of the action button.
     */
    public void hideActionButton(Runnable runnable) {
        fullscreenActionButton.animate()
                .scaleX(0)
                .scaleY(0)
                .withEndAction(runnable);
    }

    @TargetApi(21)
    private void enableTransitions() {
        getActivity().getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                fullscreenActionButton.setScaleX(0);
                fullscreenActionButton.setScaleY(0);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                getActivity().getWindow().getSharedElementEnterTransition().removeListener(this);

                fullscreenActionButton.animate()
                        .setInterpolator(new AccelerateInterpolator())
                        .setStartDelay(100)
                        .scaleX(1)
                        .scaleY(1);
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

    @TargetApi(21)
    private void circularShow() {
        int centerX = (fullscreenActionButton.getLeft() + fullscreenActionButton.getRight()) / 2;
        int centerY = (fullscreenActionButton.getTop() + fullscreenActionButton.getBottom()) / 2;
        float startRadius = 0;
        float finalRadius = (float) Math.hypot(revealContent.getWidth(), revealContent.getHeight());


        Animator animator = ViewAnimationUtils.createCircularReveal(revealContent, centerX, centerY, startRadius, finalRadius);
        animator.setDuration(700);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                enterFullScreen();
            }
        });
        revealContent.setVisibility(View.VISIBLE);
        animator.start();
    }

    @TargetApi(21)
    private void circularHide() {
        int centerX = (fullscreenActionButton.getLeft() + fullscreenActionButton.getRight()) / 2;
        int centerY = (fullscreenActionButton.getTop() + fullscreenActionButton.getBottom()) / 2;
        float initialRadius = revealContent.getWidth();
        float finalRadius = 0;

        Animator animator = ViewAnimationUtils.createCircularReveal(revealContent, centerX, centerY, initialRadius, finalRadius);
        animator.setDuration(700);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                revealContent.setVisibility(View.INVISIBLE);
            }
        });
        animator.start();
    }

}
