package com.example.karhades_pc.riddlehunting;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class MyRiddles {

    private static MyRiddles myRiddles;
    private Context context;

    private ArrayList<Riddle> riddles;

    //Private constructor that gets called only once
    // by it's get(context) method.
    private MyRiddles(Context context) {
        this.context = context;
        riddles = new ArrayList<Riddle>();

        //Dummy Riddles
        Riddle riddle_1 = new Riddle("Black", "The Black hole destroyed everything...", "Hard", false, "0421DC6AC82980");
        Riddle riddle_2 = new Riddle("Red", "Red blood everywhere around you...", "Easy", false, "04BCE16AC82980");
        Riddle riddle_3 = new Riddle("White", "White snow fell from above...", "Medium", false, "04BCE16AC82981");

        riddles.add(riddle_1);
        riddles.add(riddle_2);
        riddles.add(riddle_3);

        for (int i = 0; i < 20; i++) {
            Riddle riddle = new Riddle("Riddle #" + i, "", "Easy", false, "04BCE16AC8297" + i);
            riddles.add(riddle);
        }
    }

    //Creates a Single Object for this class (Singleton)
    //and returns it.
    public static MyRiddles get(Context context) {
        if (myRiddles == null) {
            myRiddles = new MyRiddles(context.getApplicationContext());
        }
        return myRiddles;
    }

    //Gets all the riddles
    public ArrayList<Riddle> getRiddles() {
        return riddles;
    }

    //Gets a riddle through it's tagId
    public Riddle getRiddle(String tagId) {
        for (Riddle riddle : riddles) {
            if (riddle.getTagId().equals(tagId)) {
                return riddle;
            }
        }
        return null;
    }
}
