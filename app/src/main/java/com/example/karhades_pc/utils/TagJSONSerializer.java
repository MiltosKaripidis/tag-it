package com.example.karhades_pc.utils;

import android.content.Context;
import android.util.Log;

import com.example.karhades_pc.tag_it.model.NfcTag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

    /**
     * Save the tags collection to the device's internal storage.
     *
     * @param nfcTags The ArrayList to save.
     * @throws JSONException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void saveTagsInternal(ArrayList<NfcTag> nfcTags) throws JSONException, IOException {
        // Build an array in JSON.
        JSONArray jsonArray = new JSONArray();

        // Put each NfcTag to the JSONArray.
        for (NfcTag nfcTag : nfcTags) {
            jsonArray.put(nfcTag.toJSON());
        }

        // Write the file to disk.
        Writer writer = null;
        try {
            OutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(outputStream);
            writer.write(jsonArray.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    /**
     * Load the tags collection from the file and return it.
     *
     * @return The ArrayList that contains the tags collection.
     * @throws IOException
     * @throws JSONException
     */
    @SuppressWarnings("unused")
    public ArrayList<NfcTag> loadTagsInternal() throws IOException, JSONException {
        ArrayList<NfcTag> tags = new ArrayList<>();

        BufferedReader bufferedReader = null;

        try {
            // Open the file and create an input stream.
            InputStream inputStream = context.openFileInput(filename);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Create a StringBuilder, read each line of the file
            // and append it.
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Parse the JSON String and return a JSONArray.
            JSONArray jsonArray = (JSONArray) new JSONTokener(stringBuilder.toString()).nextValue();

            // Build the array of tags from JSONObjects.
            for (int i = 0; i < jsonArray.length(); i++) {
                tags.add(new NfcTag(jsonArray.getJSONObject(i)));
            }

        } catch (FileNotFoundException e) {
            Log.e("TagJSONSerializer", "File not found!", e);
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }

        return tags;
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

    public ArrayList<NfcTag> loadTagsExternal() throws IOException, JSONException {

        ArrayList<NfcTag> loadedTags = new ArrayList<>();

        BufferedReader bufferedReader = null;

        try {
            File file = new File(context.getExternalFilesDir(null) + File.separator + filename);

            // If it's a first run.
            if (!file.exists()) {
                return loadedTags;
            }

            FileInputStream fileInputStream = new FileInputStream(file);

            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            // Create a StringBuilder, read each line of the file
            // and append it.
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Parse the JSON String and return a JSONArray.
            JSONArray jsonArray = (JSONArray) new JSONTokener(stringBuilder.toString()).nextValue();

            // Build the array of tags from JSONObjects.
            for (int i = 0; i < jsonArray.length(); i++) {
                loadedTags.add(new NfcTag(jsonArray.getJSONObject(i)));
            }

        } catch (FileNotFoundException e) {
            Log.e("TagJSONSerializer", "File not found!", e);
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }

        return loadedTags;
    }
}
