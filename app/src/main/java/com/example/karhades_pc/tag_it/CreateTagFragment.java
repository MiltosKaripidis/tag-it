package com.example.karhades_pc.tag_it;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.nfc.NfcHandler;
import com.example.karhades_pc.picture_utils.PictureUtils;

import java.io.File;

/**
 * Created by Karhades on 11-Sep-15.
 */
public class CreateTagFragment extends Fragment {
    public static final String EXTRA_TAG_ID = "com.example.karhades_pc.tag_id";

    private static final int REQUEST_IMAGE = 0;

    private ImageView imageView;
    private Button cancelButton;
    private Button tagItButton;
    private Spinner difficultySpinner;
    private ActionButton actionButton;
    private TagItDialogFragment dialogFragment;
    private Toolbar toolbar;

    private NfcTag nfcTag;
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

        startupAnimation();
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
        nfcTag = MyTags.get(getActivity()).getNfcTag(tagId);
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
    public void onStart() {
        super.onStart();

        // Load the picture taken.
        if (temporaryPictureFilename != null) {
            //loadBitmap(temporaryPictureFilename, imageView);
            imageView.setImageBitmap(PictureUtils.decodeSampledBitmapFromResource(temporaryPictureFilename, imageView.getWidth(), imageView.getHeight()));
        } else if (nfcTag != null) {
            PictureUtils.loadBitmap(nfcTag.getPictureFilename(), imageView);
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
            if (nfcTag != null)
                actionBar.setTitle(nfcTag.getTitle());
        }
    }

    private void setupFloatingActionButton(View view) {
        actionButton = (ActionButton) view.findViewById(R.id.full_screen_floating_action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExternalStorageWritable()) {
                    File file = createExternalStoragePrivateFile();

                    Uri fileUri = Uri.fromFile(file);

                    // Camera Intent
                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(intent, REQUEST_IMAGE);
                } else {
                    Toast.makeText(getActivity(), "External Storage is unmounted!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        actionButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
        actionButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File createExternalStoragePrivateFile() {
        File file = new File(getActivity().getExternalFilesDir(null), "temp_tag.jpg");

        return file;
    }

    private void initializeWidgets(View view) {
        setupSpinner(view);

        imageView = (ImageView) view.findViewById(R.id.row_create_image_view);
        // TODO: Implement click listener.

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
                temporaryDifficulty = parent.getItemAtPosition(position).toString();
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
        if (nfcTag == null && temporaryPictureFilename == null) {
            Toast.makeText(getActivity(), "Tap the camera button first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enter write mode.
        NfcHandler.toggleTagWriteMode(true);

        // Wire a listener for a on tag written event.
        NfcHandler.setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
            @Override
            public void onTagWritten(int status, String tagId) {
                dialogFragment.dismiss();

                // TODO: Invalidate.

                // NFC write was successful.
                if (status == NfcHandler.OnTagWriteListener.STATUS_OK) {
                    if (nfcTag != null) {
                        overwriteNfcTag(tagId);
                    } else {
                        createNewNfcTag(tagId);
                    }

                    // Rename the and set the file.
                    if (temporaryPictureFilename != null) {
                        File tempFile = new File(temporaryPictureFilename);
                        String renamedPath = tempFile.getParent() + File.separator + "Tag" + nfcTag.getTagId() + ".jpg";
                        File renamedFile = new File(renamedPath);

                        if (tempFile.renameTo(renamedFile)) {
                            Log.d("CreateTagFragment", "Renamed file to: " + renamedFile.getAbsolutePath());
                            nfcTag.setPictureFilename(renamedFile.getAbsolutePath());
                            temporaryPictureFilename = null;
                        } else {
                            Log.e("CreateTagFragment", "Error while renaming: " + renamedFile.getAbsolutePath());
                        }
                    }

                    // Inform the user.
                    Toast.makeText(getActivity(), "Nfc Tag was successfully written!", Toast.LENGTH_SHORT).show();

                    // Close the activity.
                    getActivity().finish();

                }
                // NFC write failed.
                else if (status == NfcHandler.OnTagWriteListener.STATUS_ERROR) {
                    Toast.makeText(getActivity(), "Could not write to nfc tag!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogFragment = new TagItDialogFragment();
        dialogFragment.show(getActivity().getSupportFragmentManager(), "write");
    }

    private void overwriteNfcTag(String tagId) {
        NfcTag currentNfcTag = MyTags.get(getActivity()).getNfcTag(nfcTag.getTagId());
        currentNfcTag.setDifficulty(temporaryDifficulty);
        currentNfcTag.setTagId(tagId);
        // TODO: Remove this statement.
        currentNfcTag.setSolved(false);

        // Get the Nfc Tag's position to inform the RecyclerView.Adapter.
        int position = MyTags.get(getActivity()).getNfcTags().indexOf(currentNfcTag);
        // Set result for REQUEST_EDIT and set the position as an intent extra.
        Intent intent = new Intent();
        intent.putExtra(CreateGameFragment.EXTRA_POSITION, position);
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    private void createNewNfcTag(String tagId) {
        // TODO: Edit the creation of the tag.
        int number = MyTags.get(getActivity()).getNfcTags().size() + 1;
        NfcTag newNfcTag = new NfcTag("Tag " + number, temporaryDifficulty, tagId);

        MyTags.get(getActivity()).addNfcTag(newNfcTag);

        this.nfcTag = newNfcTag;

        // Set result for REQUEST_NEW.
        getActivity().setResult(Activity.RESULT_OK);
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
}
