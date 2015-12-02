package com.example.karhades_pc.tag_it.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.karhades_pc.utils.PictureLoader;
import com.example.karhades_pc.utils.TagJSONSerializer;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class MyTags {

    private static final String TAG = "MyTags";
    private static final String FILENAME = "tags.txt";

    private static MyTags myTags;
    private Context context;

    private ArrayList<NfcTag> nfcTags;
    private TagJSONSerializer serializer;

    /**
     * Private constructor that gets called only
     * once by it's get(context) method.
     *
     * @param context The Context needed for Android.
     */
    private MyTags(Context context) {
        this.context = context;
        serializer = new TagJSONSerializer(this.context, FILENAME);

        loadTags();

//        // Dummy Tags
//        nfcTags = new ArrayList<>();
//        NfcTag nfcTag_1 = new NfcTag("Tag 1", "Hard", "04D11AD2C03480");
//        NfcTag nfcTag_2 = new NfcTag("Tag 2", "Easy", "04BCE16AC82980");
//        NfcTag nfcTag_3 = new NfcTag("Tag 3", "Medium", "04DC1BD2C03480");
//        NfcTag nfcTag_4 = new NfcTag("Tag 4", "Hard", "04D11AD2C03480");
//        NfcTag nfcTag_5 = new NfcTag("Tag 5", "Easy", "04BCE16AC82980");
//        NfcTag nfcTag_6 = new NfcTag("Tag 6", "Medium", "04DC1BD2C03480");
//        nfcTags.add(nfcTag_1);
//        nfcTags.add(nfcTag_2);
//        nfcTags.add(nfcTag_3);
//        nfcTags.add(nfcTag_4);
//        nfcTags.add(nfcTag_5);
//        nfcTags.add(nfcTag_6);
    }

    /**
     * Save the tags asynchronously to external storage.
     */
    public void saveTags() {
        new AsyncTaskSaver().execute();
    }

    /**
     * Load the tags from the external storage.
     */
    public void loadTags() {
        try {
            nfcTags = serializer.loadTagsExternal();
            Log.d(TAG, "Nfc Tags were loaded!");
        } catch (Exception e) {
            nfcTags = new ArrayList<>();
            Log.e(TAG, "Error loading tags: ", e);
        }
    }

    /**
     * Create a Single Object for this class (Singleton).
     *
     * @param context The Context needed for android.
     * @return The MyTags Object reference.
     */
    public static MyTags get(Context context) {
        if (myTags == null) {
            myTags = new MyTags(context.getApplicationContext());
        }
        return myTags;
    }

    /**
     * Get all the nfcTags.
     *
     * @return The ArrayList containing the tags.
     */
    public ArrayList<NfcTag> getNfcTags() {
        return nfcTags;
    }

    /**
     * Get a NfcTag through it's tagId.
     *
     * @param tagId The tag id needed for search.
     * @return The NfcTag with this tag id.
     */
    public NfcTag getNfcTag(String tagId) {
        for (NfcTag nfcTag : nfcTags) {
            if (nfcTag.getTagId().equals(tagId)) {
                return nfcTag;
            }
        }
        return null;
    }

    /**
     * Add a new Nfc Tag to the list.
     *
     * @param nfcTag The NfcTag to add.
     */
    public void addNfcTag(NfcTag nfcTag) {
        Log.d(TAG, "NfcTag inserted!");

        // Add to list.
        nfcTags.add(nfcTag);
    }

    /**
     * Remove the specified NfcTag from the list.
     *
     * @param nfcTag The NfcTag to remove.
     */
    public void deleteNfcTag(NfcTag nfcTag) {
        Log.d(TAG, "NFC " + nfcTag.getTitle() + " deleted!");

        // Clear memory cache for previous image to refresh ImageView.
        PictureLoader.invalidateWithPicasso(context, nfcTag.getPictureFilePath());

        if (nfcTag.getPictureFilePath() != null) {
            // Delete file from disk.
            File deleteFile = new File(nfcTag.getPictureFilePath());
            if (deleteFile.delete()) {
                Log.d(TAG, "NFC " + nfcTag.getTitle() + " picture deleted.");
            } else {
                Log.e(TAG, "Error deleting " + nfcTag.getTitle() + " picture.");
            }
        }

        // Remove from list.
        nfcTags.remove(nfcTag);
    }

    public void reorderNfcTags() {
        int title = 1;

        for (NfcTag nfcTag : nfcTags) {
            nfcTag.setTitle("Tag " + title);
            title++;
        }
    }

    /**
     * Create a Uri array with the uris of the tags.txt and the pictures
     * associated with every NfcTag object.
     *
     * @return Return the Uri array containing the uris of tags.json and
     * the images.
     */
    public Uri[] createFileUrisArray() {
        // First, save any changes of the tags into the model.
        saveTags();

        // Create a URI array containing NFC tags picture URIs + tags.txt file URI.
        Uri[] fileUris = new Uri[nfcTags.size() + 1];

        // Create the tags.txt file URI.
        File tagsFile = new File(context.getExternalFilesDir(null) + File.separator + "tags.txt");
        setWorldReadable(tagsFile);
        fileUris[0] = Uri.fromFile(tagsFile);

        // Create the NFC tags Picture file URIs.
        for (int i = 0; i < nfcTags.size(); i++) {
            File file = new File(nfcTags.get(i).getPictureFilePath());
            setWorldReadable(file);
            Uri uri = Uri.fromFile(file);
            fileUris[i + 1] = uri;
        }

        return fileUris;
    }

    @SuppressLint("SetWorldReadable")
    private void setWorldReadable(File file) {
        if (!file.setReadable(true, false)) {
            Log.e(TAG, "Unable to set " + file.getAbsolutePath() + " to world readable.");
        }
    }

    private class AsyncTaskSaver extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                serializer.saveTagsExternal(nfcTags);
                Log.d(TAG, "Tags saved to file!");
            } catch (Exception e) {
                Log.e(TAG, "Error saving tags: ", e);
            }

            return null;
        }
    }
}
