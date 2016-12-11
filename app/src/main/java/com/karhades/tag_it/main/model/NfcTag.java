/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.model;

import android.support.annotation.VisibleForTesting;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class that represents an NFC tag.
 */
public class NfcTag {

    /**
     * Factory instance.
     */
    private Factory mFactory;

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
     * Default constructor of NfcTag.
     */
    public NfcTag() {
        // Empty constructor.
    }

    /**
     * Simple constructor of NfcTag.
     *
     * @param title      A String indicating the title of the tag.
     * @param difficulty A String indicating the difficulty of the tag.
     * @param tagId      A String indicating the ID of the tag.
     */
    public NfcTag(String title, String difficulty, String tagId) {
        mTitle = title;
        mDifficulty = difficulty;
        mTagId = tagId;
    }

    /**
     * Full constructor of NfcTag.
     *
     * @param tagId           A String indicating the ID of the tag.
     * @param title           A String indicating the title of the tag.
     * @param pictureFilePath A String indicating the picture file path.
     * @param difficulty      A String indicating the difficulty of the tag.
     * @param discovered      A boolean indicating whether the tag is discovered.
     * @param dateDiscovered  A String indicating the discovered date.
     */
    public NfcTag(String tagId, String title, String pictureFilePath, String difficulty, boolean discovered, String dateDiscovered) {
        mTagId = tagId;
        mTitle = title;
        mPictureFilePath = pictureFilePath;
        mDifficulty = difficulty;
        mDiscovered = discovered;
        mDateDiscovered = dateDiscovered;
    }

    /**
     * Constructor for creating the NfcTag from the
     * given JSONObject.
     *
     * @param jsonObject The JSONObject to get the data.
     * @throws JSONException
     */
    public NfcTag(JSONObject jsonObject) throws JSONException {
        mDifficulty = jsonObject.getString(JsonAttributes.DIFFICULTY);
        mTagId = jsonObject.getString(JsonAttributes.TAG_ID);
        mTitle = jsonObject.getString(JsonAttributes.TITLE);
        mPictureFilePath = jsonObject.getString(JsonAttributes.PICTURE_FILE_PATH);
        mDiscovered = jsonObject.getBoolean(JsonAttributes.DISCOVERED);
        if (jsonObject.has(JsonAttributes.DATE_DISCOVERED)) {
            mDateDiscovered = jsonObject.getString(JsonAttributes.DATE_DISCOVERED);
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
        JSONObject jsonObject = mFactory.getJsonObject();
        jsonObject.put(JsonAttributes.DIFFICULTY, mDifficulty);
        jsonObject.put(JsonAttributes.TAG_ID, mTagId);
        jsonObject.put(JsonAttributes.TITLE, mTitle);
        jsonObject.put(JsonAttributes.PICTURE_FILE_PATH, mPictureFilePath);
        jsonObject.put(JsonAttributes.DISCOVERED, mDiscovered);
        if (mDateDiscovered != null) {
            jsonObject.put(JsonAttributes.DATE_DISCOVERED, mDateDiscovered);
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

    /**
     * Factory class which facilitates the testing of the NfcTag#toJson() method.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public static class Factory {

        public JSONObject getJsonObject() {
            return new JSONObject();
        }
    }

    /**
     * Setter method for the factory class injection.
     *
     * @param factory The Factory object that will be injected.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public void setFactory(Factory factory) {
        mFactory = factory;
    }
}
