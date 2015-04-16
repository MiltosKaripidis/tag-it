package com.example.karhades_pc.riddlehunting;

import java.util.Date;

/**
 * Created by Karhades - PC on 4/11/2015.
 */
public class Riddle {
    private String title;
    private String text;
    private String difficulty;
    private boolean solved;
    private String tagId;
    private Date dateSolved;

    public Riddle(String title, String text, String difficulty, boolean solved, String tagId) {
        this.title = title;
        this.text = text;
        this.difficulty = difficulty;
        this.solved = solved;
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
