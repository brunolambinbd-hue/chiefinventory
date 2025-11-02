package com.example.parabdcollector

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.parabdcollector.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class EndToEndTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addItemAndCheckIfDisplayed() {
        val itemTitle = "Mon premier test Espresso"

        onView(withId(R.id.fabAdd)).perform(click())

        onView(withId(R.id.etTitle)).perform(typeText(itemTitle))

        onView(withId(R.id.etTitle)).perform(closeSoftKeyboard())

        onView(withId(R.id.btnSave)).perform(click())

        onView(withText(itemTitle)).check(matches(isDisplayed()))
    }
}