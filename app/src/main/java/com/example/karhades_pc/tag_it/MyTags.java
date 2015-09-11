package com.example.karhades_pc.tag_it;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class MyTags {

    private static MyTags myTags;
    private Context context;

    private final ArrayList<Tag> tags;

    // Private constructor that gets called only once
    // by it's get(context) method.
    private MyTags(Context context) {
        this.context = context;
        tags = new ArrayList<>();

        // Dummy Riddles
        Tag tag_1 = new Tag("Black", "Cras aliquet blandit vehicula. Maecenas auctor egestas eros...", "Hard", false, "04D11AD2C03480");
        Tag tag_2 = new Tag("Red", "Nulla et lacus quis erat luctus elementum. Mauris...", "Easy", false, "04BCE16AC82980");
        Tag tag_3 = new Tag("White", "Suspendisse rhoncus facilisis mi, in suscipit est fermentum...", "Medium", false, "04DC1BD2C03480");
        Tag tag_4 = new Tag("Blue", "Class aptent taciti sociosqu ad litora torquent per...", "Hard", false, "04BCE16AC82982");
        Tag tag_5 = new Tag("Green", "Sed convallis diam eu mi consequat, at varius...", "Easy", false, "04BCE16AC82983");
        Tag tag_6 = new Tag("Orange", "Quisque sed nisi dignissim, ornare urna sed, tempor...", "Medium", false, "04BCE16AC82984");
        Tag tag_7 = new Tag("Gray", "Donec efficitur vitae ante a egestas. Quisque sed...", "Easy", false, "04BCE16AC82985");
        Tag tag_8 = new Tag("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        Tag tag_9 = new Tag("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        Tag tag_10 = new Tag("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        Tag tag_11 = new Tag("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        
        tags.add(tag_1);
        tags.add(tag_2);
        tags.add(tag_3);
        tags.add(tag_4);
        tags.add(tag_5);
        tags.add(tag_6);
        tags.add(tag_7);
        tags.add(tag_8);
        tags.add(tag_9);
        tags.add(tag_10);
        tags.add(tag_11);
    }

    // Creates a Single Object for this class (Singleton)
    // and returns it.
    public static MyTags get(Context context) {
        if (myTags == null) {
            myTags = new MyTags(context.getApplicationContext());
        }
        return myTags;
    }

    // Gets all the tags
    public ArrayList<Tag> getTags() {
        return tags;
    }

    // Gets a riddle through it's tagId
    public Tag getRiddle(String tagId) {
        for (Tag tag : tags) {
            if (tag.getTagId().equals(tagId)) {
                return tag;
            }
        }
        return null;
    }
}
