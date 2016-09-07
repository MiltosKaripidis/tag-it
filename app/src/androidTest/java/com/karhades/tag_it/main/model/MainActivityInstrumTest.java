/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.model;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumTest {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void list_scrolls() {
        ViewInteraction recyclerView = onView(withId(R.id.track_game_recycler_view));

        recyclerView.perform(actionOnItemAtPosition(0, click()));
        for (int i = 0; i < 4; i++) {
            onView(withId(R.id.track_tag_pager_view_pager)).perform(swipeLeft());
        }
        pressBack();
    }

    @Test
    public void scrollItems_reverse() {
        onView(withId(R.id.track_game_recycler_view)).perform(actionOnItemAtPosition(4, click()));
        for (int i = 0; i < 4; i++) {
            onView(withId(R.id.track_tag_pager_view_pager)).perform(swipeRight());
        }
        pressBack();
    }

//    @Test
//    public void changeTab() {
//        onView(withText("CREATE")).perform(click());
//
//    }
}
