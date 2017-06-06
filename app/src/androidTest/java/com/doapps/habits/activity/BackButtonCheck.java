package com.doapps.habits.activity;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.doapps.habits.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class BackButtonCheck {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void checkBackButton() {
        onView(withId(R.id.drawer_layout)).perform(open());
        onView(withText(R.string.lists)).perform(click());
        mActivityTestRule.getActivity().runOnUiThread(() ->
                mActivityTestRule.getActivity().onBackPressed());
        onView(withText(R.string.tasks_due)).check(matches(isDisplayed()));
    }

    @Test
    public void checkBackButtonOnFragmentSequence() {
        onView(withId(R.id.drawer_layout)).perform(open());
        onView(withText(R.string.lists)).perform(click());
        onView(withId(R.id.drawer_layout)).perform(open());
        onView(withText(R.string.programs)).perform(click());
        mActivityTestRule.getActivity().runOnUiThread(() ->
                mActivityTestRule.getActivity().onBackPressed());
        onView(withText(R.string.tasks_due)).check(matches(isDisplayed()));
    }

}