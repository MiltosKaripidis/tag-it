package com.example.karhades_pc.tag_it;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.karhades_pc.utils.AudioPlayer;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class MyTags {

    private static final String TAG = "MyTags";

    private static final String FILENAME = "tags.json";

    private static MyTags myTags;
    private Context context;

    private ArrayList<NfcTag> nfcTags;
    private AudioPlayer audioPlayer;
    private TagJSONSerializer serializer;

    /**
     * Private constructor that gets called only
     * once by it's get(context) method.
     *
     * @param context The Context needed for Android.
     */
    private MyTags(Context context) {
        this.context = context;
        serializer = new TagJSONSerializer(this.context, FILENAME);
        setupAudioPlayer();

        try {
            nfcTags = serializer.loadTags();
        } catch (Exception e) {
            nfcTags = new ArrayList<>();
            Log.e(TAG, "Error loading tags: ", e);
        }

//        // Dummy Riddles
//        NfcTag nfcTag_1 = new NfcTag("Black", "Cras aliquet blandit vehicula. Maecenas auctor egestas eros...", "Hard", "04D11AD2C03480");
//        NfcTag nfcTag_2 = new NfcTag("Red", "Nulla et lacus quis erat luctus elementum. Mauris...", "Easy", "04BCE16AC82980");
//        NfcTag nfcTag_3 = new NfcTag("White", "Suspendisse rhoncus facilisis mi, in suscipit est fermentum...", "Medium", "04DC1BD2C03480");
//        NfcTag tag_4 = new NfcTag("Blue", "Class aptent taciti sociosqu ad litora torquent per...", "Hard", false, "04BCE16AC82982");
//        NfcTag tag_5 = new NfcTag("Green", "Sed convallis diam eu mi consequat, at varius...", "Easy", false, "04BCE16AC82983");
//        NfcTag tag_6 = new NfcTag("Orange", "Quisque sed nisi dignissim, ornare urna sed, tempor...", "Medium", false, "04BCE16AC82984");
//        NfcTag tag_7 = new NfcTag("Gray", "Donec efficitur vitae ante a egestas. Quisque sed...", "Easy", false, "04BCE16AC82985");
    }

    /**
     * Save the tags and return true if it succeeded or
     * false if it failed.
     *
     * @return The boolean indicating the result.
     */
    public boolean saveTags() {
        try {
            serializer.saveTags(nfcTags);
            Log.d(TAG, "Tags saved to file!");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving tags: ", e);
            return false;
        }
    }

    private void setupAudioPlayer() {
        // Initialize the audio player.
        audioPlayer = new AudioPlayer();
    }

    /**
     * Create a Single Object for this class (Singleton).
     *
     * @param context The Context needed for android.
     * @return The MyTags Object reference.
     */
    public static MyTags get(Context context) {
        if (myTags == null) {
            myTags = new MyTags(context.getApplicationContext());
        }
        return myTags;
    }

    /**
     * Get all the nfcTags.
     *
     * @return The ArrayList containing the tags.
     */
    public ArrayList<NfcTag> getNfcTags() {
        return nfcTags;
    }

    /**
     * Get a NfcTag through it's tagId.
     *
     * @param tagId The tag id needed for search.
     * @return The NfcTag with this tag id.
     */
    public NfcTag getNfcTag(String tagId) {
        for (NfcTag nfcTag : nfcTags) {
            if (nfcTag.getTagId().equals(tagId)) {
                return nfcTag;
            }
        }
        return null;
    }

    /**
     * Solve the NfcTag with the given tag id.
     *
     * @param tagId The id of the NfcTag to solve.
     */
    public void solveNfcTag(String tagId) {
        NfcTag nfcTag = getNfcTag(tagId);
        nfcTag.setSolved(true);
        Toast.makeText(context, "NfcTag " + nfcTag.getTitle() + " was successfully solved!", Toast.LENGTH_SHORT).show();
        // Play a winning sound.
        // TODO: Uncomment the cheering sound.
        //audioPlayer.play(context, R.raw.cheering);
    }
}
