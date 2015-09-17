package com.example.karhades_pc.tag_it;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.nfc.NfcHandler;

/**
 * Created by Karhades on 11-Sep-15.
 */
public class CreateTagFragment extends Fragment {
    public static final String EXTRA_TAG_ID = "com.example.karhades_pc.tag_id";

    private Button cancelButton;
    private Button tagItButton;
    private Spinner difficultySpinner;
    private ActionButton actionButton;
    private Dialog alertDialog;
    private Toolbar toolbar;

    private NfcTag nfcTag;
    private String difficulty;

    /**
     * Return a CreateTagFragment with tagId as its argument.
     * It must be called after the fragment is created and before it is added to the hosting activity.
     *
     * @param tagId A String containing the NfcTag ID.
     * @return A Fragment with the above arguments.
     */
    public static CreateTagFragment newInstance(String tagId) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TAG_ID, tagId);

        CreateTagFragment createTagFragment = new CreateTagFragment();
        createTagFragment.setArguments(bundle);

        return createTagFragment;
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

        startupAnimation();
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
        // Get the tag ID from the CreateGameFragment (onListClick).
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_tag, container, false);

        setupToolbar(view);
        setupFloatingActionButton(view);
        initializeWidgets(view);

        return view;
    }

    private void setupToolbar(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.create_tag_tool_bar);

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
            if (nfcTag != null)
                actionBar.setTitle(nfcTag.getTitle());
        }
    }

    private void setupFloatingActionButton(View view) {
        actionButton = (ActionButton) view.findViewById(R.id.full_screen_floating_action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Camera!", Toast.LENGTH_SHORT).show();
            }
        });
        actionButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
        actionButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
    }

    private void initializeWidgets(View view) {
        setupSpinner(view);

        tagItButton = (Button) view.findViewById(R.id.tag_it_button);
        tagItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupNfcWriteCallback();
            }
        });

        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    private void setupSpinner(View view) {
        difficultySpinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.difficulty, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(arrayAdapter);
        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                difficulty = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // NOT IMPLEMENTED
            }
        });

        if (nfcTag != null) {
            int position = 0;
            switch (nfcTag.getDifficulty()) {
                case "Easy":
                    position = 0;
                    break;
                case "Medium":
                    position = 1;
                    break;
                case "Hard":
                    position = 2;
                    break;
            }

            difficultySpinner.setSelection(position);
        }
    }

    private void setupNfcWriteCallback() {
        // Enter write mode.
        NfcHandler.toggleTagWriteMode(true);

        // Wire a listener for a on tag written event.
        NfcHandler.setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
            @Override
            public void onTagWritten(int status, String tagId) {
                alertDialog.dismiss();

                if (status == NfcHandler.OnTagWriteListener.STATUS_OK) {
                    // Overwrite the existing tag.
                    if (nfcTag != null) {
                        NfcTag currentNfcTag = MyTags.get(getActivity()).getTag(nfcTag.getTagId());
                        currentNfcTag.setDifficulty(difficulty);
                        currentNfcTag.setTagId(tagId);
                    }
                    // Create a new tag.
                    else {
                        int number = MyTags.get(getActivity()).getNfcTags().size() + 1;
                        NfcTag newNfcTag = new NfcTag("Tag " + number, "Nulla et lacus quis erat luctus elementum. Mauris...", difficulty, tagId);
                        MyTags.get(getActivity()).getNfcTags().add(newNfcTag);
                    }

                    Toast.makeText(getActivity(), "Nfc Tag was successfully written!", Toast.LENGTH_SHORT).show();

                    // Close the activity.
                    getActivity().finish();

                } else if (status == NfcHandler.OnTagWriteListener.STATUS_ERROR) {
                    Toast.makeText(getActivity(), "Could not write to nfc tag!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Remove the event listener.
        NfcHandler.removeOnTagWriteListener();

        alertDialog = onCreateDialog();
        alertDialog.show();
    }

    private Dialog onCreateDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Nfc Write Mode")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        NfcHandler.toggleTagWriteMode(false);
                    }
                })
                .setMessage("Touch the nfc tag to write the information inserted.")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NfcHandler.toggleTagWriteMode(false);
                    }
                });

        return alertDialog.create();
    }
}
