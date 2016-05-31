package com.karhades.tag_it.main.model;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.karhades.tag_it.main.R;
import com.karhades.tag_it.main.controller.activity.CreateTagActivity;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class CreateTagActivityTest {

    @Rule
    public IntentsTestRule<CreateTagActivity> intentsTestRule = new IntentsTestRule<>(CreateTagActivity.class);

    @Test
    public void spinner_VerifyDifficulties() {
        // Trigger the spinner's pop up window.
        onView(withId(R.id.spinner)).perform(click());

        // Should be displayed.
        onView(withText("Easy"))
                .inRoot(withDecorView(Matchers.not(Matchers.is(intentsTestRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        // Should be displayed.
        onView(withText("Medium"))
                .inRoot(withDecorView(Matchers.not(Matchers.is(intentsTestRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        // Should be displayed.
        onView(withText("Hard"))
                .inRoot(withDecorView(Matchers.not(Matchers.is(intentsTestRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        // Should not be displayed.
        onView(withText("Test"))
                .inRoot(withDecorView(Matchers.not(Matchers.is(intentsTestRule.getActivity().getWindow().getDecorView()))))
                .check(doesNotExist());
    }

    @Test
    public void spinner_clickAndCheck() {
        // Trigger the spinner's pop up window.
        onView(withId(R.id.spinner)).perform(click());

        // Select Hard difficulty.
        onView(withText("Hard")).perform(click());

        // Verify selection.
        onView(withId(R.id.spinner)).check(matches(withSpinnerText("Hard")));
    }
}
