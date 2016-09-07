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
import android.support.design.widget.TextInputEditText;
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
    private NfcHandler mNfcHandler;

    /**
     * Widget references.
     */
    private ImageView mPictureImageView;
    private TextInputEditText mTitleEditText;
    private Button mTagItButton;
    private Spinner mDifficultySpinner;
    private FloatingActionButton mCameraActionButton;
    private Toolbar mToolbar;

    /**
     * Instance variables.
     */
    private NfcTag mCurrentNfcTag;
    private String mTemporaryTitle;
    private String mTemporaryDifficulty;
    private String mTemporaryPictureFilename;
    private TagItDialogFragment mTagItDialogFragment;

    /**
     * Transition variable.
     */
    private ViewGroup mRevealContent;

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
        mNfcHandler = new NfcHandler();
        mNfcHandler.setupNfcHandler(getActivity());
    }

    public void onNewIntent(Intent intent) {
        // Indicates whether the write operation can start.
        boolean isReady = NfcHandler.getWriteMode();

        if (!isReady) {
            makeSnackBar();
            return;
        }

        mNfcHandler.handleNfcTagWrite(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        hideCircularReveal();

        mNfcHandler.enableForegroundDispatch();
    }

    @Override
    public void onPause() {
        super.onPause();

        mNfcHandler.disableForegroundDispatch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        discardTag();
    }

    private void discardTag() {
        if (mTemporaryPictureFilename == null) {
            return;
        }

        // Discards the unsaved photo.
        File deleteFile = new File(mTemporaryPictureFilename);

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
        if (mTemporaryPictureFilename != null) {
            PictureLoader.loadBitmapWithPicassoNoCache(getActivity(), mTemporaryPictureFilename, mPictureImageView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {

                File tempFile = createExternalStoragePrivateFile();
                mTemporaryPictureFilename = tempFile.getAbsolutePath();
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
        mToolbar = (Toolbar) view.findViewById(R.id.create_tag_tool_bar);
        mToolbar.setTitle("Create");

        // Retrieve an AppCompatActivity hosting activity to get the supported actionbar.
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        // Set the toolbar as the new actionbar.
        activity.setSupportActionBar(mToolbar);

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
        mCameraActionButton = (FloatingActionButton) view.findViewById(R.id.create_tag_camera_floating_action_button);
        mCameraActionButton.setOnClickListener(new View.OnClickListener() {
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

        mPictureImageView = (ImageView) view.findViewById(R.id.create_tag_image_view);

        mTitleEditText = (TextInputEditText) view.findViewById(R.id.create_tag_title_edit_text);

        mTagItButton = (Button) view.findViewById(R.id.create_tag_tag_it_button);
        mTagItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupNfcWriteCallback();
            }
        });

        mRevealContent = (ViewGroup) view.findViewById(R.id.create_tag_reveal_content);
    }

    private void setupSpinner(View view) {
        mDifficultySpinner = (Spinner) view.findViewById(R.id.create_tag_spinner);
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

    private void setupNfcWriteCallback() {
        // If there's no picture taken.
        if (mTemporaryPictureFilename == null) {
            View parentView = getView();

            if (parentView == null) {
                return;
            }

            Snackbar.make(parentView, "Take a picture first.", Snackbar.LENGTH_LONG).show();

            return;
        }

        // Enters write mode.
        NfcHandler.setWriteMode(true);
        NfcHandler.setMode(NfcHandler.Mode.CREATE);

        // Creates and shows the dialog.
        mTagItDialogFragment = TagItDialogFragment.newInstance();
        mTagItDialogFragment.show(getActivity().getSupportFragmentManager(), "tagItDialog");

        // Wires a listener for an onTagWritten event.
        mNfcHandler.setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
            @Override
            public void onTagWritten(int status, String tagId) {
                // Closes dialog.
                mTagItDialogFragment.dismiss();

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
        // If edit text is empty, it keeps the same title, else the inserted one.
        mTemporaryTitle = mTitleEditText.getText().toString().equals("") ? mCurrentNfcTag.getTitle()
                : mTitleEditText.getText().toString();

        // Creates new tag with the following fields.
        NfcTag newNfcTag = new NfcTag(mTemporaryTitle, mTemporaryDifficulty, tagId);

        // Adds new tag to NfcTags list.
        MyTags.get(getActivity()).addNfcTag(newNfcTag);

        // Sets new tag as current instance member.
        mCurrentNfcTag = newNfcTag;

        // Sets result for REQUEST_INSERT.
        getActivity().setResult(Activity.RESULT_OK);
    }

    private void renameTempFile() {
        File tempFile = new File(mTemporaryPictureFilename);
        String renamedPath = tempFile.getParent() + File.separator + "Tag" + mCurrentNfcTag.getTagId() + ".jpg";
        File renamedFile = new File(renamedPath);

        if (tempFile.renameTo(renamedFile)) {
            mCurrentNfcTag.setPictureFilePath(renamedFile.getAbsolutePath());
            mTemporaryPictureFilename = null;
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
        if (mRevealContent.getVisibility() == View.INVISIBLE) {
            return;
        }

        TransitionHelper.circularHide(mCameraActionButton, mRevealContent, new Runnable() {
            @Override
            public void run() {
                // DO NOTHING
            }
        });
    }

    private void showCircularReveal() {
        TransitionHelper.circularShow(mCameraActionButton, mRevealContent, new Runnable() {
            @Override
            public void run() {
                takePicture();
            }
        });
    }
}
