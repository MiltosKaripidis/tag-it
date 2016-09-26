package com.karhades.tag_it.main.controller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.karhades.tag_it.R;
import com.karhades.tag_it.utils.stepper.AbstractStep;

/**
 * Controller fragment that works as the second step of the creation process.
 */

public class DetailsStepFragment extends AbstractStep {

    /**
     * Extra constants.
     */
    private static final String EXTRA_NAME = "com.karhades.tag_it.name";
    private static final String EXTRA_TITLE = "com.karhades.tag_it.title";
    private static final String EXTRA_DIFFICULTY = "com.karhades.tag_it.difficulty";

    /**
     * Instance variable.
     */
    private String mTemporaryDifficulty;

    /**
     * Widget references.
     */
    private TextInputEditText mTitleEditText;
    private Spinner mDifficultySpinner;

    public static DetailsStepFragment newInstance(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_NAME, title);

        DetailsStepFragment fragment = new DetailsStepFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public static String getTagTitle(Bundle bundle) {
        return bundle.getString(EXTRA_TITLE);
    }

    public static String getDifficulty(Bundle bundle) {
        return bundle.getString(EXTRA_DIFFICULTY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details_step, container, false);

        setupSpinner(view);

        mTitleEditText = (TextInputEditText) view.findViewById(R.id.details_step_edit_text);

        return view;
    }

    private void setupSpinner(View view) {
        mDifficultySpinner = (Spinner) view.findViewById(R.id.details_step_spinner);
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_difficulty, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDifficultySpinner.setAdapter(arrayAdapter);
        mDifficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTemporaryDifficulty = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // NOT IMPLEMENTED
            }
        });
    }

    @Override
    public String name() {
        return getArguments().getString(EXTRA_NAME);
    }

    @Override
    public void onNext() {
        // Passes the bundle to the next step.
        getStepDataFor(1).putString(EXTRA_TITLE, mTitleEditText.getText().toString());
        getStepDataFor(1).putString(EXTRA_DIFFICULTY, mTemporaryDifficulty);
    }

    @Override
    public boolean nextIf() {
        return !mTitleEditText.getText().toString().equals("");
    }

    @Override
    public String error() {
        return "You must fill out the details";
    }
}
