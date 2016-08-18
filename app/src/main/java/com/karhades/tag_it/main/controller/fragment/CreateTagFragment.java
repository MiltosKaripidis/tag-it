/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcHandler;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TransitionHelper;

import java.io.File;

/**
 * Controller Fragment class that binds the creation of the tag with the data set. Manages the NFC
 * write operation.
 */
public class CreateTagFragment extends Fragment {

    /**
     * Request constant.
     */
    private static final int REQUEST_IMAGE = 0;

    /**
     * NFC adapter.
     */
    private NfcHandler nfcHandler;

    /**
     * Widget references.
     */
    private ImageView imageView;
    private Button cancelButton;
    private Button tagItButton;
    private Spinner difficultySpinner;
    private FloatingActionButton cameraActionButton;
    private Toolbar toolbar;

    /**
     * Instance variables.
     */
    private NfcTag currentNfcTag;
    private String temporaryDifficulty;
    private String temporaryPictureFilename;
    private TagItDialogFragment tagItDialogFragment;

    /**
     * Transition variable.
     */
    private ViewGroup revealContent;

    public static CreateTagFragment newInstance() {
        return new CreateTagFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Makes content appear behind status bar.
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Tells the FragmentManager that this fragment should receive
        // a call to onCreateOptionsMenu.
        setHasOptionsMenu(true);

        setupNfcHandler();
    }

    private void setupNfcHandler() {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(getActivity());
    }

    public void onNewIntent(Intent intent) {
        // Indicates whether the write operation can start.
        boolean isReady = NfcHandler.getWriteMode();

        if (!isReady) {
            makeSnackBar();
            return;
        }

        nfcHandler.handleNfcWriteTag(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        hideCircularReveal();

        nfcHandler.enableForegroundDispatch();
    }

    @Override
    public void onPause() {
        super.onPause();

        nfcHandler.disableForegroundDispatch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        discardTag();
    }

    private void discardTag() {
        if (temporaryPictureFilename == null) {
            return;
        }

        // Discards the unsaved photo.
        File deleteFile = new File(temporaryPictureFilename);

        if (!deleteFile.delete()) {
            Log.e("CreateTagFragment", "Error deleting temporary file.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If there is a parent activity, navigate to it.
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    getActivity().onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Load newly picture taken.
        if (temporaryPictureFilename != null) {
            PictureLoader.loadBitmapWithPicassoNoCache(getActivity(), temporaryPictureFilename, imageView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {

                File tempFile = createExternalStoragePrivateFile();
                temporaryPictureFilename = tempFile.getAbsolutePath();
            }
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
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void setupFloatingActionButton(View view) {
        cameraActionButton = (FloatingActionButton) view.findViewById(R.id.create_tag_camera_floating_action_button);
        cameraActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TransitionHelper.isTransitionSupportedAndEnabled()) {
                    takePictureWithTransition();
                }
                // No transitions.
                else {
                    takePicture();
                }
            }
        });
    }

    private File createExternalStoragePrivateFile() {
        return new File(getActivity().getExternalFilesDir(null), "temp_tag.jpg");
    }

    private void takePictureWithTransition() {
        showCircularReveal();
    }

    private void takePicture() {
        File file = createExternalStoragePrivateFile();

        Uri fileUri = Uri.fromFile(file);

        // Camera Intent.
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void initializeWidgets(View view) {
        setupSpinner(view);

        imageView = (ImageView) view.findViewById(R.id.create_tag_image_view);

        tagItButton = (Button) view.findViewById(R.id.create_tag_tag_it_button);
        tagItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupNfcWriteCallback();
            }
        });

        cancelButton = (Button) view.findViewById(R.id.create_tag_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        revealContent = (ViewGroup) view.findViewById(R.id.create_tag_reveal_content);
    }

    private void setupSpinner(View view) {
        difficultySpinner = (Spinner) view.findViewById(R.id.create_tag_spinner);
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_difficulty, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(arrayAdapter);
        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                temporaryDifficulty = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // NOT IMPLEMENTED
            }
        });
    }

    private void setupNfcWriteCallback() {
        // If there's no picture taken.
        if (temporaryPictureFilename == null) {
            View parentView = getView();

            if (parentView == null) {
                return;
            }

            Snackbar.make(parentView, "Take a picture first.", Snackbar.LENGTH_LONG).show();

            return;
        }

        // Enters write mode.
        NfcHandler.setWriteMode(true);
        NfcHandler.setMode(NfcHandler.Mode.CREATE_NEW);

        // Creates and shows the dialog.
        tagItDialogFragment = TagItDialogFragment.newInstance();
        tagItDialogFragment.show(getActivity().getSupportFragmentManager(), "tagItDialog");

        // Wires a listener for an onTagWritten event.
        nfcHandler.setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
            @Override
            public void onTagWritten(int status, String tagId) {
                // Closes dialog.
                tagItDialogFragment.dismiss();

                // If NFC write operation was successful.
                if (status == NfcHandler.OnTagWriteListener.STATUS_OK) {
                    // Creates the new tag with the given tag ID.
                    createNewNfcTag(tagId);

                    // Renames temp_tag.jpg to new file path.
                    renameTempFile();

                    // Informs user.
                    Toast.makeText(getActivity(), "NFC tag written!", Toast.LENGTH_SHORT).show();

                    // Closes activity.
                    getActivity().finish();
                }
                // If NFC write operation was unsuccessful.
                else if (status == NfcHandler.OnTagWriteListener.STATUS_ERROR) {
                    // Inform user.
                    Toast.makeText(getActivity(), "Couldn't write to NFC tag!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createNewNfcTag(String tagId) {
        // Gets the next available number for the new tag.
        int number = MyTags.get(getActivity()).getNfcTags().size() + 1;

        // Creates new tag with the following fields.
        NfcTag newNfcTag = new NfcTag("Tag " + number, temporaryDifficulty, tagId);

        // Adds new tag to NfcTags list.
        MyTags.get(getActivity()).addNfcTag(newNfcTag);

        // Sets new tag as current instance member.
        currentNfcTag = newNfcTag;

        // Sets result for REQUEST_NEW.
        getActivity().setResult(Activity.RESULT_OK);
    }

    private void renameTempFile() {
        File tempFile = new File(temporaryPictureFilename);
        String renamedPath = tempFile.getParent() + File.separator + "Tag" + currentNfcTag.getTagId() + ".jpg";
        File renamedFile = new File(renamedPath);

        if (tempFile.renameTo(renamedFile)) {
            currentNfcTag.setPictureFilePath(renamedFile.getAbsolutePath());
            temporaryPictureFilename = null;
        } else {
            Log.e("CreateTagFragment", "Error while renaming: " + renamedFile.getAbsolutePath());
        }
    }

    public static class TagItDialogFragment extends DialogFragment {

        public static TagItDialogFragment newInstance() {
            return new TagItDialogFragment();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.icon_info)
                    .setTitle("NFC Write Mode")
                    .setMessage("Tap the NFC tag to write the inserted information.")
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NfcHandler.setWriteMode(false);
                        }
                    }).create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);

            NfcHandler.setWriteMode(false);
        }
    }

    private void makeSnackBar() {
        View parentView = getView();

        if (parentView == null) {
            return;
        }

        Snackbar.make(parentView, "Click \"TAG IT\" to write.", Snackbar.LENGTH_LONG).show();
    }

    private void hideCircularReveal() {
        if (revealContent.getVisibility() == View.INVISIBLE) {
            return;
        }

        TransitionHelper.circularHide(cameraActionButton, revealContent, new Runnable() {
            @Override
            public void run() {
                // DO NOTHING
            }
        });
    }

    private void showCircularReveal() {
        TransitionHelper.circularShow(cameraActionButton, revealContent, new Runnable() {
            @Override
            public void run() {
                takePicture();
            }
        });
    }
}
