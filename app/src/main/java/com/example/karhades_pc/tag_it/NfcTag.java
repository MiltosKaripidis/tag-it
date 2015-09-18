package com.example.karhades_pc.tag_it;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Karhades - PC on 4/11/2015.
 */
public class NfcTag {

    private static final String JSON_DIFFICULTY = "difficulty";
    private static final String JSON_TAG_ID = "tag_id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_SOLVED = "solved";

    private String title;
    private UUID id;
    private String text;
    private String difficulty;
    private boolean solved;
    private String tagId;
    private Date dateSolved;
    private int picture;

    public NfcTag(String title, String text, String difficulty, String tagId) {
        this.title = title;
        this.text = text;
        this.difficulty = difficulty;
        this.tagId = tagId;
    }

    /**
     * Constructor for creating the NfcTag from the
     * given JSONObject.
     *
     * @param jsonObject The JSONObject to get the data.
     * @throws JSONException
     */
    public NfcTag(JSONObject jsonObject) throws JSONException {
        difficulty = jsonObject.getString(JSON_DIFFICULTY);
        tagId = jsonObject.getString(JSON_TAG_ID);
        title = jsonObject.getString(JSON_TITLE);
        solved = jsonObject.getBoolean(JSON_SOLVED);
    }

    /**
     * Return a JSONObject that contains all the
     * NFcTag fields.
     *
     * @return The JSONObject with the contained data.
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_DIFFICULTY, difficulty);
        jsonObject.put(JSON_TAG_ID, tagId);
        jsonObject.put(JSON_TITLE, title);
        jsonObject.put(JSON_SOLVED, solved);

        return jsonObject;
    }

    public int getPicture() {
        return picture;
    }

    public void setPicture(int picture) {
        this.picture = picture;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public Date getDateSolved() {
        return dateSolved;
    }

    public void setDateSolved(Date dateSolved) {
        this.dateSolved = dateSolved;
    }

    @Override
    public String toString() {
        return title;
    }
}
