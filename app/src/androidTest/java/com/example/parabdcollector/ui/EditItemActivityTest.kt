package com.example.parabdcollector.ui

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.parabdcollector.R
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for the [EditItemActivity].
 *
 * These tests simulate user interaction to verify that the activity behaves as expected.
 */
@RunWith(AndroidJUnit4::class)
class EditItemActivityTest {

    /**
     * Verifies the full user flow for creating a new item.
     * It launches the activity, enters a title, clicks save, and asserts that the activity finishes.
     */
    @Test
    fun createNewItem_shouldSaveAndFinishActivity() {
        // GIVEN: The EditItemActivity is launched in 'new item' mode.
        val scenario = ActivityScenario.launch(EditItemActivity::class.java)

        // WHEN: The user types a title and clicks the save button.
        val testTitle = "Nouveau Titre de Test UI"
        onView(withId(R.id.etTitle)).perform(typeText(testTitle))
        onView(withId(R.id.btnSave)).perform(click())

        // THEN: The activity should finish, indicating that the save operation was triggered.
        // We wait a moment for the activity to close.
        Thread.sleep(500) // This is a simple way to wait for the UI to update.
        assertTrue("Activity should be finishing or destroyed after saving", 
            scenario.state == Lifecycle.State.DESTROYED)

        // Note: A more advanced test would also query the database to ensure the item was actually saved.
    }
}
