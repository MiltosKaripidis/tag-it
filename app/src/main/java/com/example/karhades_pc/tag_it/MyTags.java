package com.example.karhades_pc.tag_it;

import android.content.Context;
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

    private static final String FILENAME = "tags.json";

    private static MyTags myTags;
    private Context context;

    private ArrayList<NfcTag> nfcTags;
    private TagJSONSerializer serializer;

    private onListChangeListener onListChangeListener;

    /**
     * Register a callback to be invoked when the list changes.
     *
     * @param onListChangeListener The callback that will run.
     */
    public void setOnListChangeListener(onListChangeListener onListChangeListener) {
        this.onListChangeListener = onListChangeListener;
    }

    /**
     * Interface definition for a callback to be invoked
     * when the list changes.
     */
    public interface onListChangeListener {
        void onListChanged();
    }

    /**
     * Private constructor that gets called only
     * once by it's get(context) method.
     *
     * @param context The Context needed for Android.
     */
    private MyTags(Context context) {
        this.context = context;
        serializer = new TagJSONSerializer(this.context, FILENAME);

        try {
            nfcTags = serializer.loadTags();
            Log.d(TAG, "Nfc Tags were loaded!");
        } catch (Exception e) {
            nfcTags = new ArrayList<>();
            Log.e(TAG, "Error loading tags: ", e);
        }

//        // Dummy Tags
//        nfcTags = new ArrayList<>();
//        NfcTag nfcTag_1 = new NfcTag("Tag 1", "Hard", "04D11AD2C03480");
//        NfcTag nfcTag_2 = new NfcTag("Tag 2", "Easy", "04BCE16AC82980");
//        NfcTag nfcTag_3 = new NfcTag("Tag 3", "Medium", "04DC1BD2C03480");
//        nfcTags.add(nfcTag_1);
//        nfcTags.add(nfcTag_2);
//        nfcTags.add(nfcTag_3);
    }

    /**
     * Save the tags and return true if it succeeded or
     * false if it failed.
     *
     * @return The boolean indicating the result.
     */
    public boolean saveTags() {
        try {
            serializer.saveTags(nfcTags);
            Log.d(TAG, "Tags saved to file!");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving tags: ", e);
            return false;
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

        // Callback event method.
        onListChangeListener.onListChanged();
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

        // Delete file from disk.
        File deleteFile = new File(nfcTag.getPictureFilePath());
        if (deleteFile.delete()) {
            Log.d(TAG, "NFC " + nfcTag.getTitle() + " picture deleted.");
        } else {
            Log.e(TAG, "Error deleting " + nfcTag.getTitle() + " picture.");
        }

        // Remove from list.
        nfcTags.remove(nfcTag);

        // Callback event method.
        onListChangeListener.onListChanged();
    }

    public void reorderNfcTags() {
        int title = 1;

        for (NfcTag nfcTag : nfcTags) {
            nfcTag.setTitle("Tag " + title);
            title++;
        }
    }
}
