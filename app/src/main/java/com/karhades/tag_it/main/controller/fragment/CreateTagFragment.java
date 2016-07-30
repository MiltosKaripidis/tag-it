/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import com.karhades.tag_it.main.controller.activity.FullScreenActivity;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcHandler;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TransitionHelper;

import java.io.File;

/**
 * Controller Fragment class that binds the creation of the tag with the data set.
 */
public class CreateTagFragment extends Fragment {

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
     * Widget references.
     */
    private ImageView imageView;
    private ViewGroup imageViewOverlay;
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
    private TagItDialogFragment dialogFragment;
    private Callbacks callbacks;

    public interface Callbacks {
        void setOnTagWriteListener(NfcHandler.OnTagWriteListener onTagWriteListener);
    }

    /**
     * Transition variable.
     */
    private ViewGroup revealContent;

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

        // Tell the FragmentManager that this fragment should receive
        // a call to onCreateOptionsMenu.
        setHasOptionsMenu(true);

        getFragmentArguments();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CreateTagFragment.Callbacks interface.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        callbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        hideCircularReveal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Discard the unsaved photo.
        if (temporaryPictureFilename != null) {
            File deleteFile = new File(temporaryPictureFilename);

            if (!deleteFile.delete()) {
                Log.e("CreateTagFragment", "Error deleting temporary file.");
            }
        }
    }

    private void getFragmentArguments() {
        // Get the tag ID from the CreateGameFragment (onListClick).
        String tagId = getArguments().getString(EXTRA_TAG_ID);

        // Get the nfcTag through it's tag id from the arguments.
        currentNfcTag = MyTags.get(getActivity()).getNfcTag(tagId);
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
        // Load saved picture.
        else if (currentNfcTag != null) {
            PictureLoader.loadBitmapWithPicasso(getActivity(), currentNfcTag.getPictureFilePath(), imageView);
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
            if (NavUtils.getParentActivityName(getActivity()) != null)
                actionBar.setDisplayHomeAsUpEnabled(true);
            if (currentNfcTag != null)
                actionBar.setTitle(currentNfcTag.getTitle());
        }
    }

    private void setupFloatingActionButton(View view) {
        cameraActionButton = (FloatingActionButton) view.findViewById(R.id.camera_action_button);
        cameraActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
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

        imageView = (ImageView) view.findViewById(R.id.create_image_view);

        imageViewOverlay = (ViewGroup) view.findViewById(R.id.create_image_view_overlay);
        imageViewOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentNfcTag != null) {
                    Intent intent = new Intent(getActivity(), FullScreenActivity.class);
                    String filePath = currentNfcTag.getPictureFilePath();
                    intent.putExtra(EXTRA_FILE_PATH, filePath);
                    startActivity(intent);
                }
            }
        });

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

        revealContent = (ViewGroup) view.findViewById(R.id.create_reveal_content);
    }

    private void setupSpinner(View view) {
        difficultySpinner = (Spinner) view.findViewById(R.id.spinner);
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

        if (currentNfcTag != null) {
            int position = 0;
            switch (currentNfcTag.getDifficulty()) {
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

        // If it's a new tag and there's no picture taken.
        if (currentNfcTag == null && temporaryPictureFilename == null) {
            View parentView = getView();
            if (parentView != null) {
                Snackbar.make(parentView, "Take a picture first.", Snackbar.LENGTH_LONG).show();
            }
            return;
        }

        // Enter write mode.
        NfcHandler.setWriteMode(true);

        // If it's a new tag.
        if (currentNfcTag == null) {
            NfcHandler.setMode(NfcHandler.Mode.CREATE_NEW);
        }
        // If it already exists.
        else {
            NfcHandler.setMode(NfcHandler.Mode.OVERWRITE);
        }

        // Create and show the dialog.
        dialogFragment = TagItDialogFragment.newInstance();
        dialogFragment.show(getActivity().getSupportFragmentManager(), "write");

        // Wire a listener for a on tag write event.
        callbacks.setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
            @Override
            public void onTagWritten(int status, String tagId) {

                // Close dialog.
                dialogFragment.dismiss();

                // If NFC write operation was successful.
                if (status == NfcHandler.OnTagWriteListener.STATUS_OK) {
                    if (currentNfcTag != null) {
                        overwriteNfcTag(tagId);
                    } else {
                        createNewNfcTag(tagId);
                    }

                    // Rename temp_tag.jpg to new file path.
                    renameTempFile();

                    // Inform user.
                    Toast.makeText(getActivity(), "NFC tag written!", Toast.LENGTH_SHORT).show();

                    // Close activity.
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

    private void overwriteNfcTag(String tagId) {
        // Overwrite current nfc tag fields.
        currentNfcTag.setDifficulty(temporaryDifficulty);
        currentNfcTag.setTagId(tagId);
        currentNfcTag.setDiscovered(false);
        currentNfcTag.setDateDiscovered(null);

        // Clear memory cache for previous image to refresh ImageView.
        PictureLoader.invalidateWithPicasso(getActivity(), currentNfcTag.getPictureFilePath());

        // Get Nfc Tag's position to inform RecyclerView.Adapter.
        int position = MyTags.get(getActivity()).getNfcTags().indexOf(currentNfcTag);
        // Set result for REQUEST_EDIT and set position as an intent extra.
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POSITION, position);

        // Set result for REQUEST_EDIT.
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    private void createNewNfcTag(String tagId) {
        int number = MyTags.get(getActivity()).getNfcTags().size() + 1;

        NfcTag newNfcTag = new NfcTag("Tag " + number, temporaryDifficulty, tagId);

        // Add new tag to NfcTags list.
        MyTags.get(getActivity()).addNfcTag(newNfcTag);

        // Set new tag as current instance member.
        currentNfcTag = newNfcTag;

        // Set result for REQUEST_NEW.
        getActivity().setResult(Activity.RESULT_OK);
    }

    private void renameTempFile() {
        if (temporaryPictureFilename != null) {
            File tempFile = new File(temporaryPictureFilename);
            String renamedPath = tempFile.getParent() + File.separator + "Tag" + currentNfcTag.getTagId() + ".jpg";
            File renamedFile = new File(renamedPath);

            if (tempFile.renameTo(renamedFile)) {
                temporaryPictureFilename = null;
                currentNfcTag.setPictureFilePath(renamedFile.getAbsolutePath());
            } else {
                Log.e("CreateTagFragment", "Error while renaming: " + renamedFile.getAbsolutePath());
            }
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

    public void makeSnackBar() {
        View parentView = getView();
        if (parentView != null) {
            Snackbar.make(parentView, "Click \"TAG IT!\" to write.", Snackbar.LENGTH_LONG).show();
        }
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
