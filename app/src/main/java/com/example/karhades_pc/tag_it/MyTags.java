package com.example.karhades_pc.tag_it;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class MyTags {

    private static MyTags myTags;
    private Context context;

    private final ArrayList<NfcTag> nfcTags;

    // Private constructor that gets called only once
    // by it's get(context) method.
    private MyTags(Context context) {
        this.context = context;
        nfcTags = new ArrayList<>();

        // Dummy Riddles
        NfcTag nfcTag_1 = new NfcTag("Black", "Cras aliquet blandit vehicula. Maecenas auctor egestas eros...", "Hard", false, "04D11AD2C03480");
        NfcTag nfcTag_2 = new NfcTag("Red", "Nulla et lacus quis erat luctus elementum. Mauris...", "Easy", false, "04BCE16AC82980");
        NfcTag nfcTag_3 = new NfcTag("White", "Suspendisse rhoncus facilisis mi, in suscipit est fermentum...", "Medium", false, "04DC1BD2C03480");
//        NfcTag tag_4 = new NfcTag("Blue", "Class aptent taciti sociosqu ad litora torquent per...", "Hard", false, "04BCE16AC82982");
//        NfcTag tag_5 = new NfcTag("Green", "Sed convallis diam eu mi consequat, at varius...", "Easy", false, "04BCE16AC82983");
//        NfcTag tag_6 = new NfcTag("Orange", "Quisque sed nisi dignissim, ornare urna sed, tempor...", "Medium", false, "04BCE16AC82984");
//        NfcTag tag_7 = new NfcTag("Gray", "Donec efficitur vitae ante a egestas. Quisque sed...", "Easy", false, "04BCE16AC82985");
//        NfcTag tag_8 = new NfcTag("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
//        NfcTag tag_9 = new NfcTag("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
//        NfcTag tag_10 = new NfcTag("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
//        NfcTag tag_11 = new NfcTag("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        
        nfcTags.add(nfcTag_1);
        nfcTags.add(nfcTag_2);
        nfcTags.add(nfcTag_3);
//        nfcTags.add(tag_4);
//        nfcTags.add(tag_5);
//        nfcTags.add(tag_6);
//        nfcTags.add(tag_7);
//        nfcTags.add(tag_8);
//        nfcTags.add(tag_9);
//        nfcTags.add(tag_10);
//        nfcTags.add(tag_11);
    }

    // Creates a Single Object for this class (Singleton)
    // and returns it.
    public static MyTags get(Context context) {
        if (myTags == null) {
            myTags = new MyTags(context.getApplicationContext());
        }
        return myTags;
    }

    // Gets all the nfcTags
    public ArrayList<NfcTag> getNfcTags() {
        return nfcTags;
    }

    // Gets a riddle through it's tagId
    public NfcTag getTag(String tagId) {
        for (NfcTag nfcTag : nfcTags) {
            if (nfcTag.getTagId().equals(tagId)) {
                return nfcTag;
            }
        }
        return null;
    }
}
