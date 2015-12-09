package com.example.karhades_pc.utils;

import android.content.Context;

import com.example.karhades_pc.tag_it.model.NfcTag;

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

/**
 * Created by Karhades on 18-Sep-15.
 */
public class TagJSONSerializer {

    private Context context;
    private String filename;

    public TagJSONSerializer(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    public void saveTagsExternal(ArrayList<NfcTag> nfcTags) throws JSONException, IOException {
        // Build an array in JSON.
        JSONArray jsonArray = new JSONArray();

        // Put each NfcTag to the JSONArray.
        for (NfcTag nfcTag : nfcTags) {
            jsonArray.put(nfcTag.toJSON());
        }

        File file = new File(context.getExternalFilesDir(null) + File.separator + filename);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(jsonArray.toString().getBytes());
        fileOutputStream.close();
    }

    public ArrayList<NfcTag> loadTagsExternal() throws JSONException, IOException {
        // Create a new tag list that will be returned.
        ArrayList<NfcTag> loadedTags = new ArrayList<>();

        File file = new File(context.getExternalFilesDir(null) + File.separator + filename);
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
