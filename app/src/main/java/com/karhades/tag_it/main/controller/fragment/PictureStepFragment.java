package com.karhades.tag_it.main.controller.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.stepper.AbstractStep;

import java.io.File;

/**
 * Controller fragment that works as the first step of the creation process.
 */
public class PictureStepFragment extends AbstractStep {

    /**
     * Request constant.
     */
    private static final int REQUEST_IMAGE = 0;

    /**
     * Extra constants.
     */
    private static final String EXTRA_NAME = "com.karhades.tag_it.name";
    private static final String EXTRA_FILE_NAME = "com.karhades.tag_it.file_name";

    /**
     * Instance variable.
     */
    private String mTemporaryPictureFilename;

    /**
     * Widget references.
     */
    private FloatingActionButton mCameraActionButton;
    private CardView mCardView;
    private ImageView mPictureImageView;

    public static PictureStepFragment newInstance(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_NAME, title);

        PictureStepFragment fragment = new PictureStepFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public static String getFileName(Bundle bundle) {
        return bundle.getString(PictureStepFragment.EXTRA_FILE_NAME);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Loads newly picture taken.
        if (mTemporaryPictureFilename != null) {
            PictureLoader.loadBitmapWithPicassoNoCache(getActivity(), mTemporaryPictureFilename, mPictureImageView);

            // Swaps views.
            mCardView.setVisibility(View.VISIBLE);
            mCameraActionButton.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picture_step, container, false);

        mCameraActionButton = (FloatingActionButton) view.findViewById(R.id.picture_step_camera_action_button);
        mCameraActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mCardView = (CardView) view.findViewById(R.id.picture_step_card_view);

        mPictureImageView = (ImageView) view.findViewById(R.id.picture_step_image_view);

        return view;
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
            Log.e("PictureStepFragment", "Error deleting temporary file.");
        }
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

    private File createExternalStoragePrivateFile() {
        return new File(getActivity().getExternalFilesDir(null), "temp_tag.jpg");
    }

    @Override
    public String name() {
        return getArguments().getString(EXTRA_NAME);
    }

    @Override
    public boolean nextIf() {
        return mTemporaryPictureFilename != null;
    }

    @Override
    public void onNext() {
        // Passes the bundle to the next step.
        getStepDataFor(0).putString(EXTRA_FILE_NAME, mTemporaryPictureFilename);
    }

    @Override
    public String error() {
        return "You must take a picture";
    }
}
