package com.example.karhades_pc.riddlehunting;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class RiddleFragment extends Fragment {

    public static final String EXTRA_TAG_ID = "com.example.karhades_pc.nfctester.tag_id";
    public static final String EXTRA_NFC_TAG_DISCOVERED = "com.example.karhades_pc.nfctester.nfc_tag_discovered";
    public static final String EXTRA_NFC_TAG_DISCOVERED_RESET = "com.example.karhades_pc.nfctester.nfc_tag_discovered_reset";

    private Riddle riddle;
    private boolean nfcTagDiscovered;

    private TextView riddleDifficultyTextView;
    private TextView riddleTextView;
    private CheckBox riddleSolvedCheckBox;
    private TextView riddleDateSolvedTextView;

    //Creates a bundle and sets the Tag ID and a boolean indicating whether the
    //RiddleActivity was started from NFC Tag discovery. It must be called after
    //the fragment is created and before it is added to the RiddleActivity.
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

        //Gets the Tag ID either from the RiddleListFragment (onListClick) or
        //the NFC Tag Discovery.
        String tagId = getArguments().getString(EXTRA_TAG_ID);
        riddle = MyRiddles.get(getActivity()).getRiddle(tagId);

        //Checks whether an NFC Tag was discovered and solves the
        //appropriate Riddle.
        if(savedInstanceState != null) {
            nfcTagDiscovered = savedInstanceState.getBoolean(EXTRA_NFC_TAG_DISCOVERED_RESET);
        }
        else {
            nfcTagDiscovered = getArguments().getBoolean(EXTRA_NFC_TAG_DISCOVERED);
        }

        if (nfcTagDiscovered) {
            riddle.setSolved(true);
            riddle.setDateSolved(new Date());

            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(riddle.getTitle());
            Toast.makeText(getActivity(), "Riddle " + riddle.getTitle() + " was successfully solved!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(EXTRA_NFC_TAG_DISCOVERED_RESET, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_riddle, container, false);

        initializeWidgets(view);

        return view;
    }

    //Initializes the Widgets
    private void initializeWidgets(View view) {
        //Riddle Title TextView
        TextView riddleTitleTextView = (TextView) view.findViewById(R.id.riddle_title_text_view);
        //Bold Font for the Riddle
        Typeface typefaceTitle = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Capture_it.ttf");
        riddleTitleTextView.setTypeface(typefaceTitle);

        //Riddle Details Title TextView
        TextView riddleDetailsTitleTextView = (TextView) view.findViewById(R.id.riddle_details_title_text_view);
        riddleDetailsTitleTextView.setTypeface(typefaceTitle);

        //Riddle TextView
        riddleTextView = (TextView) view.findViewById(R.id.riddle_text_view);
        riddleTextView.setText(riddle.getText());
        //Bold Font for the Riddle
        Typeface typefaceBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/amatic_bold.ttf");
        riddleTextView.setTypeface(typefaceBold);

        //Normal Font for the rest
        Typeface typefaceNormal = Typeface.createFromAsset(getActivity().getAssets(), "fonts/amatic_normal.ttf");

        //Riddle Difficulty Label TextView
        TextView riddleDifficultyLabel = (TextView) view.findViewById(R.id.riddle_difficulty_label_text_view);
        riddleDifficultyLabel.setTypeface(typefaceNormal);

        //Riddle Difficulty TextView
        riddleDifficultyTextView = (TextView) view.findViewById(R.id.riddle_difficulty_text_view);
        riddleDifficultyTextView.setText(riddle.getDifficulty());
        riddleDifficultyTextView.setTypeface(typefaceNormal);

        //Riddle Solved Label TextView
        TextView riddleSolvedLabel = (TextView) view.findViewById(R.id.riddle_solved_label_text_view);
        riddleSolvedLabel.setTypeface(typefaceNormal);

        //Riddle Solved CheckBox
        riddleSolvedCheckBox = (CheckBox) view.findViewById(R.id.riddle_solved_check_box);
        riddleSolvedCheckBox.setChecked(riddle.isSolved());

        //Riddle Date Label TextView
        TextView riddleDateSolvedLabelTextView = (TextView) view.findViewById(R.id.riddle_date_solved_label_text_view);
        riddleDateSolvedLabelTextView.setTypeface(typefaceNormal);

        //Riddle Date Solved CheckBox
        riddleDateSolvedTextView = (TextView) view.findViewById(R.id.date_solved_text_view);
        riddleDateSolvedTextView.setTypeface(typefaceNormal);
        if (riddle.getDateSolved() != null) {
            //Formats the Date into human-readable text
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy (HH:mm:ss)");
            Date date = riddle.getDateSolved();
            String formattedDate = simpleDateFormat.format(date);
            riddleDateSolvedTextView.setText(formattedDate);
        }
    }
}
