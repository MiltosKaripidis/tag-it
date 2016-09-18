/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.karhades.tag_it.main.controller.fragment.SettingsFragment;
import com.karhades.tag_it.main.database.TagItDataBaseHelper;
import com.karhades.tag_it.main.database.NfcTagCursorWrapper;
import com.karhades.tag_it.main.database.TagItDatabaseSchema;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TagJsonSerializer;

import java.io.File;
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
    private static boolean sTransitionDisabled = false;

    /**
     * JSON file name.
     */
    private static final String FILENAME = "tags.txt";

    /**
     * Singleton instance.
     */
    private static MyTags sMyTags;

    /**
     * Context for starting activities, using private storage, etc.
     */
    private Context mContext;

    /**
     * Application database.
     */
    private SQLiteDatabase mDatabase;

    /**
     * Helper class for saving and loading NfcTag objects in JSON format.
     */
    private TagJsonSerializer mSerializer;

    /**
     * Private constructor that gets called only
     * once by it's get(context) method.
     *
     * @param context The Context needed for Android.
     */
    private MyTags(Context context) {
        mContext = context.getApplicationContext();

        mSerializer = new TagJsonSerializer(mContext, FILENAME);

        // Gets the saved transition variable from disk.
        sTransitionDisabled = SettingsFragment.getStoredTransition(mContext);
    }

    public static void setTransitionDisabled(boolean transitionDisabled) {
        MyTags.sTransitionDisabled = transitionDisabled;
    }

    public static boolean isTransitionDisabled() {
        return sTransitionDisabled;
    }

    /**
     * Loads the tags from the external storage.
     */
    public void loadTags() {
        mDatabase = new TagItDataBaseHelper(mContext).getWritableDatabase();
    }

    /**
     * Creates a Single Object for this class (Singleton).
     *
     * @param context The Context needed for android.
     * @return The MyTags Object reference.
     */
    public static MyTags get(Context context) {
        if (sMyTags == null) {
            sMyTags = new MyTags(context);
        }
        return sMyTags;
    }

    /**
     * Gets all the nfcTags from the database.
     *
     * @return The List containing the tags.
     */
    public List<NfcTag> getNfcTags() {
        List<NfcTag> nfcTags = new ArrayList<>();

        NfcTagCursorWrapper cursor = queryTags(null, null);
        cursor.moveToFirst();

        do {
            nfcTags.add(cursor.getNfcTag());
        } while (cursor.moveToNext());
        cursor.close();

        return nfcTags;
    }

    /**
     * Gets an NfcTag through it's tagId.
     *
     * @param tagId The tag id needed for search.
     * @return The NfcTag with this tag id.
     */
    public NfcTag getNfcTag(String tagId) {
        NfcTagCursorWrapper cursor = queryTags(
                TagItDatabaseSchema.TagTable.Columns.TAG_ID + " = ?",
                new String[]{tagId});

        if (cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();
        return cursor.getNfcTag();
    }

    /**
     * Gets the NfcTag position in the list from the given tag ID.
     *
     * @param tagId The tag ID needed for the search.
     * @return An int representing the position in the list or -1 if it doesn't exist.
     */
    public int getNfcTagPosition(String tagId) {
        List<NfcTag> nfcTags = getNfcTags();

        for (NfcTag nfcTag : nfcTags) {
            if (nfcTag.getTagId().equals(tagId)) {
                return nfcTags.indexOf(nfcTag);
            }
        }

        return -1;
    }

    /**
     * Returns a table row that contains all the given NfcTag fields inside each
     * corresponding table column.
     *
     * @param nfcTag The NfcTag object to get the values.
     * @return A ContentValues object that contains all the NfcTag fields.
     */
    private static ContentValues getContentValues(NfcTag nfcTag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TagItDatabaseSchema.TagTable.Columns.TAG_ID, nfcTag.getTagId());
        contentValues.put(TagItDatabaseSchema.TagTable.Columns.TITLE, nfcTag.getTitle());
        contentValues.put(TagItDatabaseSchema.TagTable.Columns.FILE_PATH, nfcTag.getPictureFilePath());
        contentValues.put(TagItDatabaseSchema.TagTable.Columns.DIFFICULTY, nfcTag.getDifficulty());
        contentValues.put(TagItDatabaseSchema.TagTable.Columns.DISCOVERED, nfcTag.isDiscovered() ? 1 : 0);
        contentValues.put(TagItDatabaseSchema.TagTable.Columns.DATE_DISCOVERED, nfcTag.getDateDiscovered());

        return contentValues;
    }

    /**
     * SQL select statement that returns data for the given WHERE clause.
     *
     * @param whereClause A String representing the WHERE clause. Can be null (SELECT *).
     * @param whereArgs   A String representing the arguments for the WHERE clause. Can be null.
     * @return An NfcTagCursorWrapper object that contains the row(s).
     */
    private NfcTagCursorWrapper queryTags(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                TagItDatabaseSchema.TagTable.NAME,
                null, // select all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );

        return new NfcTagCursorWrapper(cursor);
    }

    /**
     * Adds a new NFC tag to the database.
     *
     * @param nfcTag The NfcTag to add.
     */
    public void addNfcTag(NfcTag nfcTag) {
        ContentValues contentValues = getContentValues(nfcTag);

        mDatabase.insert(
                TagItDatabaseSchema.TagTable.NAME,
                null,
                contentValues);
    }

    /**
     * Updates the database for the given NFC tag.
     *
     * @param nfcTag The NfcTag to update.
     */
    public void updateNfcTag(NfcTag nfcTag) {
        String tagId = nfcTag.getTagId();
        ContentValues contentValues = getContentValues(nfcTag);

        mDatabase.update(
                TagItDatabaseSchema.TagTable.NAME,
                contentValues,
                TagItDatabaseSchema.TagTable.Columns.TAG_ID + " = ?",
                new String[]{tagId});
    }

    /**
     * Removes the specified NFC tag from the database along with it's image.
     *
     * @param nfcTag The NfcTag to remove.
     */
    public void deleteNfcTag(NfcTag nfcTag) {
        // Clear memory cache for previous image to refresh ImageView.
        PictureLoader.invalidateWithPicasso(mContext, nfcTag.getPictureFilePath());

        if (nfcTag.getPictureFilePath() != null) {
            // Delete file from disk.
            File deleteFile = new File(nfcTag.getPictureFilePath());
            if (!deleteFile.delete()) {
                Log.e(TAG, "Error deleting " + nfcTag.getTitle() + " picture.");
            }
        }

        String tagId = nfcTag.getTagId();

        mDatabase.delete(
                TagItDatabaseSchema.TagTable.NAME,
                TagItDatabaseSchema.TagTable.Columns.TAG_ID + " = ?",
                new String[]{tagId});
    }

    // TODO: Test the Android Beam operation.

    /**
     * Creates a URI array with the URIs of the tags.txt and the pictures
     * associated with every NfcTag object.
     *
     * @return Returns the URI array containing the URIs of tags.txt and
     * the images.
     */
    public Uri[] createFileUrisArray() {
        List<NfcTag> nfcTags = getNfcTags();

        prepareJsonFile(nfcTags);

        // Creates a URI array with a size of [NFC tag picture URIs + tags.txt file URI].
        Uri[] fileUris = new Uri[nfcTags.size() + 1];

        // Creates the tags.txt file URI.
        File tagsFile = new File(mContext.getExternalFilesDir(null) + File.separator + "tags.txt");
        setWorldReadable(tagsFile);
        fileUris[0] = Uri.fromFile(tagsFile);

        // Creates the NFC tag picture file URIs.
        for (int i = 0; i < nfcTags.size(); i++) {
            File file = new File(nfcTags.get(i).getPictureFilePath());
            setWorldReadable(file);
            Uri uri = Uri.fromFile(file);
            fileUris[i + 1] = uri;
        }

        return fileUris;
    }

    private void prepareJsonFile(List<NfcTag> nfcTags) {
        try {
            mSerializer.saveTagsExternal(nfcTags);
        } catch (Exception e) {
            Log.e(TAG, "Error saving tags: " + e.getMessage());
        }
    }

    @SuppressLint("SetWorldReadable")
    private void setWorldReadable(File file) {
        if (!file.setReadable(true, false)) {
            Log.e(TAG, "Unable to set " + file.getAbsolutePath() + " to world readable.");
        }
    }
}
