package com.example.karhades_pc.riddlehunting;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class MyRiddles {

    private static MyRiddles myRiddles;
    private Context context;

    private final ArrayList<Riddle> riddles;

    // Private constructor that gets called only once
    // by it's get(context) method.
    private MyRiddles(Context context) {
        this.context = context;
        riddles = new ArrayList<>();

        // Dummy Riddles
        Riddle riddle_1 = new Riddle("Black", "Cras aliquet blandit vehicula. Maecenas auctor egestas eros...", "Hard", false, "04D11AD2C03480");
        Riddle riddle_2 = new Riddle("Red", "Nulla et lacus quis erat luctus elementum. Mauris...", "Easy", false, "04BCE16AC82980");
        Riddle riddle_3 = new Riddle("White", "Suspendisse rhoncus facilisis mi, in suscipit est fermentum...", "Medium", false, "04DC1BD2C03480");
        Riddle riddle_4 = new Riddle("Blue", "Class aptent taciti sociosqu ad litora torquent per...", "Hard", false, "04BCE16AC82982");
        Riddle riddle_5 = new Riddle("Green", "Sed convallis diam eu mi consequat, at varius...", "Easy", false, "04BCE16AC82983");
        Riddle riddle_6 = new Riddle("Orange", "Quisque sed nisi dignissim, ornare urna sed, tempor...", "Medium", false, "04BCE16AC82984");
        Riddle riddle_7 = new Riddle("Gray", "Donec efficitur vitae ante a egestas. Quisque sed...", "Easy", false, "04BCE16AC82985");
        Riddle riddle_8 = new Riddle("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        Riddle riddle_9 = new Riddle("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        Riddle riddle_10 = new Riddle("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        Riddle riddle_11 = new Riddle("Purple", "Donec ornare lacus a sapien maximus, eget semper...", "Hard", false, "04BCE16AC82986");
        
        riddles.add(riddle_1);
        riddles.add(riddle_2);
        riddles.add(riddle_3);
        riddles.add(riddle_4);
        riddles.add(riddle_5);
        riddles.add(riddle_6);
        riddles.add(riddle_7);
        riddles.add(riddle_8);
        riddles.add(riddle_9);
        riddles.add(riddle_10);
        riddles.add(riddle_11);
    }

    // Creates a Single Object for this class (Singleton)
    // and returns it.
    public static MyRiddles get(Context context) {
        if (myRiddles == null) {
            myRiddles = new MyRiddles(context.getApplicationContext());
        }
        return myRiddles;
    }

    // Gets all the riddles
    public ArrayList<Riddle> getRiddles() {
        return riddles;
    }

    // Gets a riddle through it's tagId
    public Riddle getRiddle(String tagId) {
        for (Riddle riddle : riddles) {
            if (riddle.getTagId().equals(tagId)) {
                return riddle;
            }
        }
        return null;
    }
}
