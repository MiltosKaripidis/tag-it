package com.example.karhades_pc.riddlehunting;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.karhades_pc.utils.AudioPlayer;
import com.example.karhades_pc.utils.FontCache;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class RiddleFragment extends Fragment {

    public static final String EXTRA_TAG_ID = "com.example.karhades_pc.nfctester.tag_id";
    public static final String EXTRA_NFC_TAG_DISCOVERED = "com.example.karhades_pc.nfctester.nfc_tag_discovered";

    private Riddle riddle;
    private boolean nfcTagIsDiscovered;
    private AudioPlayer audioPlayer = new AudioPlayer();

    private TextView riddleDifficultyTextView;
    private TextView riddleTextView;
    private CheckBox riddleSolvedCheckBox;
    private TextView riddleDateSolvedTextView;

    private Toolbar toolbar;

    /**
     * It must be called after the fragment is created and before it is added to the RiddleActivity.
     *
     * @param tagId A String containing the Tag ID
     * @param nfcTagDiscovered A boolean indicating whether the RiddleActivity was started from NFC Tag discovery
     * @return A Fragment with the above arguments
     */
    public static RiddleFragment newInstance(String tagId, boolean nfcTagDiscovered) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TAG_ID, tagId);
        bundle.putBoolean(EXTRA_NFC_TAG_DISCOVERED, nfcTagDiscovered);

        RiddleFragment fragment = new RiddleFragment();
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

        // Get the Tag ID either from the RiddleListFragment (onListClick) or
        // the NFC Tag Discovery.
        String tagId = getArguments().getString(EXTRA_TAG_ID);
        // Check whether a NFC Tag was discovered and solve the
        // appropriate Riddle.
        nfcTagIsDiscovered = getArguments().getBoolean(EXTRA_NFC_TAG_DISCOVERED);

        // Get the riddle through it's tag id from the arguments.
        riddle = MyRiddles.get(getActivity()).getRiddle(tagId);

        if (nfcTagIsDiscovered) {
            solveRiddle();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                // If there is a parent activity, navigate to it.
                if(NavUtils.getParentActivityName(getActivity()) != null)
                {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void solveRiddle()
    {
        riddle.setSolved(true);
        riddle.setDateSolved(new Date());

        // Play a winning sound.
        audioPlayer.play(getActivity(), R.raw.cheering);

        Toast.makeText(getActivity(), "Riddle " + riddle.getTitle() + " was successfully solved!", Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_riddle, container, false);

        setUpToolbar(view);

        initializeWidgets(view);

        return view;
    }

    /**
     * Helper method for setting up the tool bar.
     *
     * @param view A view needed for the findViewById() method
     */
    private void setUpToolbar(View view)
    {
        toolbar = (Toolbar) view.findViewById(R.id.tool_bar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        // Display the caret for an ancestral navigation.
        if(NavUtils.getParentActivityName(getActivity()) != null)
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(riddle.getTitle());
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

        // Riddle Title TextView
        TextView riddleTitleTextView = (TextView) view.findViewById(R.id.riddle_title_text_view);
        riddleTitleTextView.setTypeface(typefaceTitle);

        // Riddle Details Title TextView
        TextView riddleDetailsTitleTextView = (TextView) view.findViewById(R.id.riddle_details_title_text_view);
        riddleDetailsTitleTextView.setTypeface(typefaceTitle);

        // Riddle TextView
        riddleTextView = (TextView) view.findViewById(R.id.riddle_text_view);
        riddleTextView.setText(riddle.getText());
        riddleTextView.setTypeface(typefaceBold);

        // Riddle Difficulty Label TextView
        TextView riddleDifficultyLabel = (TextView) view.findViewById(R.id.riddle_difficulty_label_text_view);
        riddleDifficultyLabel.setTypeface(typefaceNormal);

        // Riddle Difficulty TextView
        riddleDifficultyTextView = (TextView) view.findViewById(R.id.riddle_difficulty_text_view);
        riddleDifficultyTextView.setText(riddle.getDifficulty());
        riddleDifficultyTextView.setTypeface(typefaceNormal);

        // Riddle Solved Label TextView
        TextView riddleSolvedLabel = (TextView) view.findViewById(R.id.riddle_solved_label_text_view);
        riddleSolvedLabel.setTypeface(typefaceNormal);

        // Riddle Solved CheckBox
        riddleSolvedCheckBox = (CheckBox) view.findViewById(R.id.riddle_solved_check_box);
        riddleSolvedCheckBox.setChecked(riddle.isSolved());

        // Riddle Date Label TextView
        TextView riddleDateSolvedLabelTextView = (TextView) view.findViewById(R.id.riddle_date_solved_label_text_view);
        riddleDateSolvedLabelTextView.setTypeface(typefaceNormal);

        // Riddle Date Solved CheckBox
        riddleDateSolvedTextView = (TextView) view.findViewById(R.id.date_solved_text_view);
        riddleDateSolvedTextView.setTypeface(typefaceNormal);
        if (riddle.getDateSolved() != null) {
            // Format the Date into human-readable text
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy (HH:mm:ss)");
            Date date = riddle.getDateSolved();
            String formattedDate = simpleDateFormat.format(date);
            riddleDateSolvedTextView.setText(formattedDate);
        }
    }
}
