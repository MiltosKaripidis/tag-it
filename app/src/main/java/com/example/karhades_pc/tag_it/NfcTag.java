package com.example.karhades_pc.tag_it;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Karhades - PC on 4/11/2015.
 */
public class NfcTag {

    private static final String JSON_DIFFICULTY = "difficulty";
    private static final String JSON_TAG_ID = "tag_id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_PICTURE_FILE_PATH = "picture_file_path";
    private static final String JSON_SOLVED = "solved";
    private static final String JSON_DATE_SOLVED = "date_solved";

    private String tagId;
    private String title;
    private String pictureFilePath;
    private String difficulty;
    private boolean solved;
    private String dateSolved;

    public NfcTag(String title, String difficulty, String tagId) {
        this.title = title;
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
        pictureFilePath = jsonObject.getString(JSON_PICTURE_FILE_PATH);
        solved = jsonObject.getBoolean(JSON_SOLVED);
        if (jsonObject.has(JSON_DATE_SOLVED)) {
            dateSolved = jsonObject.getString(JSON_DATE_SOLVED);
        }
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
        jsonObject.put(JSON_PICTURE_FILE_PATH, pictureFilePath);
        jsonObject.put(JSON_SOLVED, solved);
        if (dateSolved != null) {
            jsonObject.put(JSON_DATE_SOLVED, dateSolved);
        }

        return jsonObject;
    }

    public String getPictureFilePath() {
        return pictureFilePath;
    }

    public void setPictureFilePath(String pictureFilePath) {
        this.pictureFilePath = pictureFilePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getDateSolved() {
        return dateSolved;
    }

    public void setDateSolved(String dateSolved) {
        this.dateSolved = dateSolved;
    }
}
