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
import com.karhades.tag_it.main.controller.activity.FullScreenActivity;
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
public class EditTagFragment extends Fragment {

    /**
     * Extras constants.
     */
    private static final String EXTRA_POSITION = "com.karhades.tag_it.position";
    private static final String EXTRA_TAG_ID = "com.karhades.tag_it.tag_id";
    private static final String EXTRA_FILE_PATH = "com.karhades.tag_it.file_path";

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
    private Button mTagItButton;
    private TextInputEditText mTitleEditText;
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

    /**
     * Return an EditTagFragment with tagId as its argument.
     * It must be called after the fragment is created and before it is added to the hosting activity.
     *
     * @param tagId A String containing the NfcTag ID.
     * @return A Fragment with the above arguments.
     */
    public static EditTagFragment newInstance(String tagId) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TAG_ID, tagId);

        EditTagFragment fragment = new EditTagFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tells the FragmentManager that this fragment should receive
        // a call to onCreateOptionsMenu.
        setHasOptionsMenu(true);

        getFragmentArguments();
        setupNFC();
    }

    private void getFragmentArguments() {
        // Gets the tag ID from the CreateGameFragment (onListClick).
        String tagId = getArguments().getString(EXTRA_TAG_ID);

        // Gets the nfcTag through it's tag ID from the arguments.
        mCurrentNfcTag = MyTags.get(getActivity()).getNfcTag(tagId);
    }

    private void setupNFC() {
        mNfcHandler = new NfcHandler();
        mNfcHandler.setupNfcHandler(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_tag, container, false);

        setupToolbar(view);
        setupFloatingActionButton(view);
        initializeWidgets(view);

        return view;
    }

    private void setupToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.edit_tag_tool_bar);

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
            if (mCurrentNfcTag != null) {
                actionBar.setTitle(mCurrentNfcTag.getTitle());
            }
        }
    }

    private void setupFloatingActionButton(View view) {
        mCameraActionButton = (FloatingActionButton) view.findViewById(R.id.edit_tag_camera_floating_action_button);
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

    private void initializeWidgets(View view) {
        setupSpinner(view);

        mTitleEditText = (TextInputEditText) view.findViewById(R.id.edit_tag_title_edit_text);

        mPictureImageView = (ImageView) view.findViewById(R.id.edit_tag_image_view);
        mPictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentNfcTag != null) {
                    Intent intent = new Intent(getActivity(), FullScreenActivity.class);
                    String filePath = mCurrentNfcTag.getPictureFilePath();
                    intent.putExtra(EXTRA_FILE_PATH, filePath);
                    startActivity(intent);
                }
            }
        });

        mTagItButton = (Button) view.findViewById(R.id.edit_tag_tag_it_button);
        mTagItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupNfcWriteCallback();
            }
        });

        mRevealContent = (ViewGroup) view.findViewById(R.id.edit_tag_reveal_content);
    }

    private void setupSpinner(View view) {
        mDifficultySpinner = (Spinner) view.findViewById(R.id.edit_tag_spinner);
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

        if (mCurrentNfcTag != null) {
            int position = 0;
            switch (mCurrentNfcTag.getDifficulty()) {
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

            mDifficultySpinner.setSelection(position);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Loads newly picture taken.
        if (mTemporaryPictureFilename != null) {
            PictureLoader.loadBitmapWithPicassoNoCache(getActivity(), mTemporaryPictureFilename, mPictureImageView);
        }
        // Loads saved picture.
        else if (mCurrentNfcTag != null) {
            PictureLoader.loadBitmapWithPicasso(getActivity(), mCurrentNfcTag.getPictureFilePath(), mPictureImageView);
        }
    }

    public void onNewIntent(Intent intent) {
        // Indicates whether the write operation can start.
        boolean isReady = NfcHandler.getWriteMode();

        if (!isReady) {
            makeSnackBar();
        } else {
            mNfcHandler.handleNfcTagWrite(intent);
        }
    }

    private void makeSnackBar() {
        View parentView = getView();

        if (parentView == null) {
            return;
        }

        Snackbar.make(parentView, "Click \"TAG IT!\" to write.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();

        mNfcHandler.enableForegroundDispatch();

        hideCircularReveal();
    }

    @Override
    public void onPause() {
        super.onPause();

        mNfcHandler.disableForegroundDispatch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Discards the unsaved photo.
        if (mTemporaryPictureFilename != null) {
            File deleteFile = new File(mTemporaryPictureFilename);

            if (!deleteFile.delete()) {
                Log.e("EditTagFragment", "Error deleting temporary file.");
            }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {

                File tempFile = createExternalStoragePrivateFile();
                mTemporaryPictureFilename = tempFile.getAbsolutePath();
            }
        }
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

    private void setupNfcWriteCallback() {

        // If it's a new tag and there's no picture taken.
        if (mCurrentNfcTag == null && mTemporaryPictureFilename == null) {
            View parentView = getView();

            if (parentView == null) {
                return;
            }

            Snackbar.make(parentView, "Take a picture first.", Snackbar.LENGTH_LONG).show();

            return;
        }

        // Enters write mode.
        NfcHandler.setWriteMode(true);
        NfcHandler.setMode(NfcHandler.Mode.OVERWRITE);

        // Creates and show the dialog.
        mTagItDialogFragment = TagItDialogFragment.newInstance();
        mTagItDialogFragment.show(getActivity().getSupportFragmentManager(), "write");

        // Wires a listener for an onTagWrite event.
        mNfcHandler.setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
            @Override
            public void onTagWritten(int status, String tagId) {

                // Closes dialog.
                mTagItDialogFragment.dismiss();

                // If NFC write operation was successful.
                if (status == NfcHandler.OnTagWriteListener.STATUS_OK) {
                    // Overwrites the tag with the specified tag ID.
                    overwriteNfcTag(tagId);

                    // Renames temp_tag.jpg to new file path.
                    renameTempFile();

                    saveTag();

                    // Informs user.
                    Toast.makeText(getActivity(), "NFC tag written!", Toast.LENGTH_SHORT).show();

                    // Closes activity.
                    getActivity().finish();
                }
                // If NFC write operation was unsuccessful.
                else if (status == NfcHandler.OnTagWriteListener.STATUS_ERROR) {
                    // Informs user.
                    Toast.makeText(getActivity(), "Couldn't write to NFC tag", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void overwriteNfcTag(String tagId) {
        // If edit text is empty, it keeps the same title, else the inserted one.
        mTemporaryTitle = mTitleEditText.getText().toString().equals("") ? mCurrentNfcTag.getTitle()
                : mTitleEditText.getText().toString();

        // Overwrites current nfc tag fields.
        mCurrentNfcTag.setTitle(mTemporaryTitle);
        mCurrentNfcTag.setDifficulty(mTemporaryDifficulty);
        mCurrentNfcTag.setTagId(tagId);
        mCurrentNfcTag.setDiscovered(false);
        mCurrentNfcTag.setDateDiscovered(null);

        // Clear memory cache for previous image to refresh ImageView.
        PictureLoader.invalidateWithPicasso(getActivity(), mCurrentNfcTag.getPictureFilePath());

        // Get Nfc Tag's position to inform RecyclerView.Adapter.
        int position = MyTags.get(getActivity()).getNfcTagPosition(mCurrentNfcTag.getTagId());
        // Set result for REQUEST_EDIT and set position as an intent extra.
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POSITION, position);

        // Set result for REQUEST_EDIT.
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    private void renameTempFile() {
        if (mTemporaryPictureFilename != null) {
            File tempFile = new File(mTemporaryPictureFilename);
            String renamedPath = tempFile.getParent() + File.separator + "Tag" + mCurrentNfcTag.getTagId() + ".jpg";
            File renamedFile = new File(renamedPath);

            if (tempFile.renameTo(renamedFile)) {
                mTemporaryPictureFilename = null;
                mCurrentNfcTag.setPictureFilePath(renamedFile.getAbsolutePath());
            } else {
                Log.e("EditTagFragment", "Error while renaming: " + renamedFile.getAbsolutePath());
            }
        }
    }

    /**
     * Saves the tag to the external storage.
     */
    private void saveTag() {
        MyTags.get(getActivity()).updateNfcTag(mCurrentNfcTag);
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
