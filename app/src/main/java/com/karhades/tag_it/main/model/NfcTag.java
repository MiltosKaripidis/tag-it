/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class that represents an NFC tag.
 */
public class NfcTag {

    /**
     * JSON map keys constants.
     */
    private static final String JSON_DIFFICULTY = "difficulty";
    private static final String JSON_TAG_ID = "tag_id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_PICTURE_FILE_PATH = "picture_file_path";
    private static final String JSON_DISCOVERED = "discovered";
    private static final String DATE_DISCOVERED = "date_discovered";

    /**
     * Instance fields.
     */
    private String mTagId;
    private String mTitle;
    private String mPictureFilePath;
    private String mDifficulty;
    private boolean mDiscovered;
    private String mDateDiscovered;

    /**
     * Simple constructor of NfcTag.
     *
     * @param title A String indicating the title of the tag.
     * @param difficulty A String indicating the difficulty of the tag.
     * @param tagId A String indicating the id of the tag.
     */
    public NfcTag(String title, String difficulty, String tagId) {
        mTitle = title;
        mDifficulty = difficulty;
        mTagId = tagId;
    }

    /**
     * Constructor for creating the NfcTag from the
     * given JSONObject.
     *
     * @param jsonObject The JSONObject to get the data.
     * @throws JSONException
     */
    public NfcTag(JSONObject jsonObject) throws JSONException {
        mDifficulty = jsonObject.getString(JSON_DIFFICULTY);
        mTagId = jsonObject.getString(JSON_TAG_ID);
        mTitle = jsonObject.getString(JSON_TITLE);
        mPictureFilePath = jsonObject.getString(JSON_PICTURE_FILE_PATH);
        mDiscovered = jsonObject.getBoolean(JSON_DISCOVERED);
        if (jsonObject.has(DATE_DISCOVERED)) {
            mDateDiscovered = jsonObject.getString(DATE_DISCOVERED);
        }
    }

    /**
     * Returns a JSONObject that contains all the
     * NFcTag fields.
     *
     * @return The JSONObject with the contained data.
     * @throws JSONException
     */
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_DIFFICULTY, mDifficulty);
        jsonObject.put(JSON_TAG_ID, mTagId);
        jsonObject.put(JSON_TITLE, mTitle);
        jsonObject.put(JSON_PICTURE_FILE_PATH, mPictureFilePath);
        jsonObject.put(JSON_DISCOVERED, mDiscovered);
        if (mDateDiscovered != null) {
            jsonObject.put(DATE_DISCOVERED, mDateDiscovered);
        }

        return jsonObject;
    }

    public String getPictureFilePath() {
        return mPictureFilePath;
    }

    public void setPictureFilePath(String pictureFilePath) {
        mPictureFilePath = pictureFilePath;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDifficulty() {
        return mDifficulty;
    }

    public void setDifficulty(String difficulty) {
        mDifficulty = difficulty;
    }

    public boolean isDiscovered() {
        return mDiscovered;
    }

    public void setDiscovered(boolean discovered) {
        mDiscovered = discovered;
    }

    public String getTagId() {
        return mTagId;
    }

    public void setTagId(String tagId) {
        mTagId = tagId;
    }

    public String getDateDiscovered() {
        return mDateDiscovered;
    }

    public void setDateDiscovered(String dateDiscovered) {
        mDateDiscovered = dateDiscovered;
    }
}
