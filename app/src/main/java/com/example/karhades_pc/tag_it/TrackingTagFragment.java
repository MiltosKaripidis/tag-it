package com.example.karhades_pc.tag_it;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.utils.FontCache;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class TrackingTagFragment extends Fragment {

    public static final String EXTRA_TAG_ID = "com.example.karhades_pc.tracking_tag_fragment.tag_id";

    private NfcTag nfcTag;
    private boolean nfcTagIsDiscovered;

    private TextView riddleDifficultyTextView;
    private TextView riddleTextView;
    private CheckBox riddleSolvedCheckBox;
    private TextView riddleDateSolvedTextView;

    private Toolbar toolbar;
    private ActionButton actionButton;

    /**
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
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
        startupAnimation();
    }

    private void updateUI() {
        riddleDifficultyTextView.setText(nfcTag.getDifficulty());
        riddleSolvedCheckBox.setChecked(nfcTag.isSolved());
//        riddleTextView.setText(nfcTag.getText());
//        if (nfcTag.getDateSolved() != null) {
//            // Format the Date into human-readable text
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy (HH:mm:ss)");
//            Date date = nfcTag.getDateSolved();
//            String formattedDate = simpleDateFormat.format(date);
//            riddleDateSolvedTextView.setText(formattedDate);
    }

    private void startupAnimation() {
        // Floating Action Button animation on show after a period of time.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (actionButton.isHidden()) {
                    actionButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
                    actionButton.show();
                    actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
                }
            }
        }, 750);
    }

    private void getFragmentArguments() {
        // Get the tag ID either from the TrackingGameFragment (onListClick) or
        // the NFC tag Discovery.
        String tagId = getArguments().getString(EXTRA_TAG_ID);

        // Get the nfcTag through it's tag id from the arguments.
        nfcTag = MyTags.get(getActivity()).getTag(tagId);
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
     * @param view A view needed for the findViewById() method
     */
    private void setupToolbar(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.tracking_tag_tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            // Display the caret for an ancestral navigation.
            if (NavUtils.getParentActivityName(getActivity()) != null)
                actionBar.setDisplayHomeAsUpEnabled(true);
            if (nfcTag != null)
                actionBar.setTitle(nfcTag.getTitle());
        }
    }

    private void setupFloatingActionButton(View view) {
        actionButton = (ActionButton) view.findViewById(R.id.full_screen_floating_action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Full Screen!", Toast.LENGTH_SHORT).show();
            }
        });
        actionButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
        actionButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
    }

    /**
     * Initialize the Widgets and wire custom fonts to them.
     *
     * @param view A view needed for the findViewById() method
     */
    private void initializeWidgets(View view) {
        // Custom Fonts.
        Typeface typefaceTitle = FontCache.get("fonts/Capture_it.ttf", getActivity());
        Typeface typefaceBold = FontCache.get("fonts/amatic_bold.ttf", getActivity());
        Typeface typefaceNormal = FontCache.get("fonts/amatic_normal.ttf", getActivity());

        // NfcTag Title TextView
        //TextView riddleTitleTextView = (TextView) view.findViewById(R.id.riddle_title_text_view);
        //riddleTitleTextView.setTypeface(typefaceTitle);

        // NfcTag Details Title TextView
        TextView riddleDetailsTitleTextView = (TextView) view.findViewById(R.id.riddle_details_title_text_view);
        riddleDetailsTitleTextView.setTypeface(typefaceTitle);

        // NfcTag TextView
//        riddleTextView = (TextView) view.findViewById(R.id.riddle_text_view);
//        riddleTextView.setTypeface(typefaceBold);

        // NfcTag Difficulty Label TextView
        TextView riddleDifficultyLabel = (TextView) view.findViewById(R.id.riddle_difficulty_label_text_view);
        riddleDifficultyLabel.setTypeface(typefaceNormal);

        // NfcTag Difficulty TextView
        riddleDifficultyTextView = (TextView) view.findViewById(R.id.riddle_difficulty_text_view);
        riddleDifficultyTextView.setTypeface(typefaceNormal);

        // NfcTag Solved Label TextView
        TextView riddleSolvedLabel = (TextView) view.findViewById(R.id.riddle_solved_label_text_view);
        riddleSolvedLabel.setTypeface(typefaceNormal);

        // NfcTag Solved CheckBox
        riddleSolvedCheckBox = (CheckBox) view.findViewById(R.id.riddle_solved_check_box);

        // NfcTag Date Label TextView
//        TextView riddleDateSolvedLabelTextView = (TextView) view.findViewById(R.id.riddle_date_solved_label_text_view);
//        riddleDateSolvedLabelTextView.setTypeface(typefaceNormal);
//
//        // NfcTag Date Solved CheckBox
//        riddleDateSolvedTextView = (TextView) view.findViewById(R.id.date_solved_text_view);
//        riddleDateSolvedTextView.setTypeface(typefaceNormal);
    }
}
