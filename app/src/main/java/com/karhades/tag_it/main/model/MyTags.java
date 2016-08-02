/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.karhades.tag_it.main.controller.fragment.SettingsFragment;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TagJsonSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class that manages all the tags for the application.
 */
public class MyTags {

    /**
     * Debug constant.
     */
    private static final String TAG = "MyTags";

    /**
     * Toggle application transitions.
     */
    private static boolean transitionDisabled = false;

    /**
     * JSON file name.
     */
    private static final String FILENAME = "tags.txt";

    /**
     * Singleton instance.
     */
    private static MyTags myTags;

    /**
     * Context for starting activities, using private storage, etc.
     */
    private Context context;

    /**
     * List of NfcTag objects.
     */
    private List<NfcTag> nfcTags;

    /**
     * Helper class for saving and loading NfcTag objects in JSON format.
     */
    private TagJsonSerializer serializer;

    /**
     * Private constructor that gets called only
     * once by it's get(context) method.
     *
     * @param context The Context needed for Android.
     */
    private MyTags(Context context) {
        this.context = context.getApplicationContext();

        serializer = new TagJsonSerializer(this.context, FILENAME);

        // Gets the saved transition variable from disk.
        transitionDisabled = SettingsFragment.getStoredTransition(this.context);
    }

    public static void setTransitionDisabled(boolean transitionDisabled) {
        MyTags.transitionDisabled = transitionDisabled;
    }

    public static boolean isTransitionDisabled() {
        return transitionDisabled;
    }

    /**
     * Saves the tags to external storage.
     */
    public void saveTags() {
        try {
            serializer.saveTagsExternal(nfcTags);
        } catch (Exception e) {
            Log.e(TAG, "Error saving tags: " + e.getMessage());
        }
    }

    /**
     * Loads the tags from the external storage.
     */
    public void loadTags() {
        try {
            nfcTags = serializer.loadTagsExternal();
        } catch (FileNotFoundException e) {
            nfcTags = new ArrayList<>();
            Log.e(TAG, "File tags.txt not found.", e);
        } catch (Exception e) {
            nfcTags = new ArrayList<>();
            Log.e(TAG, "Error loading tags: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a Single Object for this class (Singleton).
     *
     * @param context The Context needed for android.
     * @return The MyTags Object reference.
     */
    public static MyTags get(Context context) {
        if (myTags == null) {
            myTags = new MyTags(context);
        }
        return myTags;
    }

    /**
     * Gets all the nfcTags.
     *
     * @return The List containing the tags.
     */
    public List<NfcTag> getNfcTags() {
        return nfcTags;
    }

    /**
     * Gets a NfcTag through it's tagId.
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
     * Adds a new Nfc Tag to the list.
     *
     * @param nfcTag The NfcTag to add.
     */
    public void addNfcTag(NfcTag nfcTag) {
        // Add to list.
        nfcTags.add(nfcTag);
    }

    /**
     * Removes the specified NfcTag from the list.
     *
     * @param nfcTag The NfcTag to remove.
     */
    public void deleteNfcTag(NfcTag nfcTag) {
        // Clear memory cache for previous image to refresh ImageView.
        PictureLoader.invalidateWithPicasso(context, nfcTag.getPictureFilePath());

        if (nfcTag.getPictureFilePath() != null) {
            // Delete file from disk.
            File deleteFile = new File(nfcTag.getPictureFilePath());
            if (!deleteFile.delete()) {
                Log.e(TAG, "Error deleting " + nfcTag.getTitle() + " picture.");
            }
        }

        // Remove from list.
        nfcTags.remove(nfcTag);
    }

    /**
     * Reorders the whole list starting from 1 to length list.
     */
    public void reorderNfcTags() {
        int title = 1;

        for (NfcTag nfcTag : nfcTags) {
            nfcTag.setTitle("Tag " + title);
            title++;
        }
    }

    /**
     * Creates a URI array with the URIs of the tags.txt and the pictures
     * associated with every NfcTag object.
     *
     * @return Returns the URI array containing the URIs of tags.txt and
     * the images.
     */
    public Uri[] createFileUrisArray() {
        // First, save any changes of the tags into the model.
        saveTags();

        // Create a URI array with a size of [NFC tag picture URIs + tags.txt file URI].
        Uri[] fileUris = new Uri[nfcTags.size() + 1];

        // Create the tags.txt file URI.
        File tagsFile = new File(context.getExternalFilesDir(null) + File.separator + "tags.txt");
        setWorldReadable(tagsFile);
        fileUris[0] = Uri.fromFile(tagsFile);

        // Create the NFC tag picture file URIs.
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
}
