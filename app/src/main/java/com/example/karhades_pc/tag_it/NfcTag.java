package com.example.karhades_pc.tag_it;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Karhades - PC on 4/11/2015.
 */
public class NfcTag {
    private String title;
    private UUID id;
    private String text;
    private String difficulty;
    private boolean solved;
    private String tagId;
    private Date dateSolved;

    private int picture;

    public int getPicture() {
        return picture;
    }

    public void setPicture(int picture) {
        this.picture = picture;
    }

    public NfcTag(String title, String text, String difficulty, String tagId) {
        this.title = title;
        this.text = text;
        this.difficulty = difficulty;
        this.tagId = tagId;
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
