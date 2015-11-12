package com.example.karhades_pc.tag_it.fragment;

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

import com.example.karhades_pc.nfc.NfcHandler;
import com.example.karhades_pc.tag_it.model.MyTags;
import com.example.karhades_pc.tag_it.model.NfcTag;
import com.example.karhades_pc.tag_it.R;
import com.example.karhades_pc.tag_it.activity.FullScreenActivity;
import com.example.karhades_pc.utils.PictureLoader;
import com.example.karhades_pc.utils.TransitionHelper;

import java.io.File;

/**
 * Created by Karhades on 11-Sep-15.
 */
public class CreateTagFragment extends Fragment {

    public static final String EXTRA_TAG_ID = "com.example.karhades_pc.tag_it.tag_id";
    public static final String EXTRA_FILE_PATH = "com.example.karhades_pc.tag_it.file_path";

    private static final int REQUEST_IMAGE = 0;

    private ImageView imageView;
    private ViewGroup imageViewOverlay;
    private Button cancelButton;
    private Button tagItButton;
    private Spinner difficultySpinner;
    private FloatingActionButton cameraActionButton;
    private ViewGroup revealContent;
    private TagItDialogFragment dialogFragment;
    private Toolbar toolbar;

    private NfcTag currentNfcTag;
    private String temporaryDifficulty;
    private String temporaryPictureFilename;

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

        // Hide the reveal content view.
        if (revealContent.getVisibility() == View.VISIBLE) {
            TransitionHelper.circularHide(cameraActionButton, revealContent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Discard the unsaved photo.
        if (temporaryPictureFilename != null) {
            File deleteFile = new File(temporaryPictureFilename);

            if (deleteFile.delete()) {
                Log.d("CreateTagFragment", "Temporary picture deleted.");
            } else {
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
            PictureLoader.loadBitmapWithPicasso(getActivity(), currentNfcTag.getPictureFilePath(), imageView, null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("CreateTagFragment", "RESULT_OK!");

                File tempFile = createExternalStoragePrivateFile();
                temporaryPictureFilename = tempFile.getAbsolutePath();

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("CreateTagFragment", "RESULT_CANCELED!");
            } else {
                Log.d("CreateTagFragment", "RESULT_EXITED!");
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
                if (TransitionHelper.itSupportsTransitions()) {
                    TransitionHelper.circularShow(cameraActionButton, revealContent, new Runnable() {
                        @Override
                        public void run() {
                            takePicture();
                        }
                    });
                }
                // No transitions.
                else {
                    takePicture();
                }
            }
        });
    }

    private File createExternalStoragePrivateFile() {
        File file = new File(getActivity().getExternalFilesDir(null), "temp_tag.jpg");

        return file;
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
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.difficulty, android.R.layout.simple_spinner_item);
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
            Snackbar.make(getView(), "Tap the camera button first.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Enter write mode.
        NfcHandler.toggleTagWriteMode(true);

        // If it's a new tag.
        if (currentNfcTag == null) {
            NfcHandler.setMode(NfcHandler.Mode.CREATE_NEW);
        }
        // If it already exists.
        else {
            NfcHandler.setMode(NfcHandler.Mode.OVERWRITE);
        }

        // Create and show the dialog.
        dialogFragment = new TagItDialogFragment();
        dialogFragment.show(getActivity().getSupportFragmentManager(), "write");

        // Wire a listener for a on tag write event.
        NfcHandler.setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
            @Override
            public void onTagWritten(int status, String tagId) {

                // Close dialog.
                dialogFragment.dismiss();

                // If NFC write was successful.
                if (status == NfcHandler.OnTagWriteListener.STATUS_OK) {
                    if (currentNfcTag != null) {
                        overwriteNfcTag(tagId);
                    } else {
                        createNewNfcTag(tagId);
                    }

                    // Rename temp_tag.jpg to new file path.
                    renameTempFile();

                    // Inform user.
                    Toast.makeText(getActivity(), "Nfc Tag was successfully written!", Toast.LENGTH_SHORT).show();

                    // Close activity.
                    getActivity().finish();
                }
            }
        });
    }

    private void overwriteNfcTag(String tagId) {
        // Overwrite current nfc tag fields.
        currentNfcTag.setDifficulty(temporaryDifficulty);
        currentNfcTag.setTagId(tagId);
        currentNfcTag.setDateSolved(null);

        // Clear memory cache for previous image to refresh ImageView.
        PictureLoader.invalidateWithPicasso(getActivity(), currentNfcTag.getPictureFilePath());

        // Get Nfc Tag's position to inform RecyclerView.Adapter.
        int position = MyTags.get(getActivity()).getNfcTags().indexOf(currentNfcTag);
        // Set result for REQUEST_EDIT and set position as an intent extra.
        Intent intent = new Intent();
        intent.putExtra(CreateGameFragment.EXTRA_POSITION, position);

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
                Log.d("CreateTagFragment", "Renamed file to: " + renamedFile.getAbsolutePath());

                temporaryPictureFilename = null;
                currentNfcTag.setPictureFilePath(renamedFile.getAbsolutePath());
            } else {
                Log.e("CreateTagFragment", "Error while renaming: " + renamedFile.getAbsolutePath());
            }
        }
    }

    public static class TagItDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Nfc Write Mode")
                    .setMessage("Touch the nfc tag to write the information inserted.")
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NfcHandler.toggleTagWriteMode(false);
                        }
                    }).create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);

            NfcHandler.toggleTagWriteMode(false);
        }
    }

    public void makeSnackbar() {
        Snackbar.make(getView(), "Press the \"TAG IT!\" button to write! ", Snackbar.LENGTH_LONG).show();
    }
}
