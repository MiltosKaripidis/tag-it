/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.utils;

import android.content.Context;

import com.karhades.tag_it.main.model.NfcTag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for saving and loading the json file from the filesystem.
 */
public class TagJsonSerializer {

    private Context mContext;
    private String mFilename;

    public TagJsonSerializer(Context context, String filename) {
        mContext = context;
        mFilename = filename;
    }

    /**
     * Saves the specified {@code nfcTags} to the external file system using the json format.
     *
     * @param nfcTags The List with the tags to save.
     * @throws JSONException
     * @throws IOException
     */
    public void saveTagsExternal(List<NfcTag> nfcTags) throws JSONException, IOException {
        // Builds an array in JSON.
        JSONArray jsonArray = new JSONArray();

        // Puts each NfcTag to the JSONArray.
        for (NfcTag nfcTag : nfcTags) {
            jsonArray.put(nfcTag.toJson());
        }

        File file = new File(mContext.getExternalFilesDir(null) + File.separator + mFilename);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(jsonArray.toString().getBytes());
        fileOutputStream.close();
    }

    /**
     * Loads the tags from the external file system and returns them.
     *
     * @return The saved List with tags.
     * @throws JSONException
     * @throws IOException
     */
    public List<NfcTag> loadTagsExternal() throws JSONException, IOException {
        // Create a new tag list that will be returned.
        List<NfcTag> loadedTags = new ArrayList<>();

        File file = new File(mContext.getExternalFilesDir(null) + File.separator + mFilename);
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        // Create a StringBuilder, read each line of the file
        // and append it.
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        // Create a JSONTokener from the StringBuilder.
        JSONTokener jsonTokener = new JSONTokener(stringBuilder.toString());
        // Parse the JSON String and return a JSONArray.
        JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();

        // Build the array of tags from JSONObjects.
        for (int i = 0; i < jsonArray.length(); i++) {
            loadedTags.add(new NfcTag(jsonArray.getJSONObject(i)));
        }

        return loadedTags;
    }
}
