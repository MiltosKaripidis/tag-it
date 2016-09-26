package com.karhades.tag_it.main.controller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcHandler;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.stepper.AbstractStep;

import java.io.File;

/**
 * Controller fragment that works as the third step of the creation process.
 */

public class WriteStepFragment extends AbstractStep {

    /**
     * EXTRA constant.
     */
    private static final String EXTRA_NAME = "com.karhades.tag_it.name";

    /**
     * NFC adapter.
     */
    private NfcHandler mNfcHandler;

    /**
     * Instance variables.
     */
    private NfcTag mCurrentNfcTag;
    private String mTemporaryPictureFilename;
    private String mTemporaryTitle;
    private String mTemporaryDifficulty;


    public static WriteStepFragment newInstance(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_NAME, title);

        WriteStepFragment fragment = new WriteStepFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            return;
        }

        mNfcHandler.handleNfcTagWrite(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_write_step, container, false);
    }

    @Override
    public String name() {
        return getArguments().getString(EXTRA_NAME);
    }

    @Override
    public void onPrevious() {
        mNfcHandler.disableForegroundDispatch();
    }

    @Override
    public void onStepVisible() {
        // Reads the bundle from the previous steps.
        mTemporaryPictureFilename = PictureStepFragment.getFileName(getStepDataFor(0));
        mTemporaryTitle = DetailsStepFragment.getTagTitle(getStepDataFor(1));
        mTemporaryDifficulty = DetailsStepFragment.getDifficulty(getStepDataFor(1));

        mNfcHandler.enableForegroundDispatch();
        setupNfcWriteCallback();
    }

    @Override
    public boolean nextIf() {
        return mCurrentNfcTag != null;
    }

    @Override
    public String error() {
        return "You must tap an NFC tag";
    }

    private void setupNfcWriteCallback() {

        // Enters write mode.
        NfcHandler.setWriteMode(true);
        NfcHandler.setMode(NfcHandler.Mode.CREATE);

        // Wires a listener for an onTagWritten event.
        mNfcHandler.setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
            @Override
            public void onTagWritten(int status, String tagId) {

                // If NFC write operation was successful.
                if (status == NfcHandler.OnTagWriteListener.STATUS_OK) {
                    // Creates the new tag with the given tag ID.
                    createNewNfcTag(tagId);

                    // Renames temp_tag.jpg to new file path.
                    renameTempFile();

                    saveTag();

                    // Informs user.
                    makeSnackBar();
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

        // Creates new tag with the following fields.
        mCurrentNfcTag = new NfcTag(mTemporaryTitle, mTemporaryDifficulty, tagId);
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

    /**
     * Saves the tag to the external storage.
     */
    private void saveTag() {
        MyTags.get(getActivity()).addNfcTag(mCurrentNfcTag);
    }

    private void makeSnackBar() {
        View view = getView();

        if (view == null) {
            return;
        }

        Snackbar.make(view, "NFC tag written", Snackbar.LENGTH_LONG).show();
    }
}
