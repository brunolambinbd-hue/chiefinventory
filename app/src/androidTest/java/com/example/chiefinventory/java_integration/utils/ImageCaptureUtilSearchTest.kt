package com.example.chiefinventory.java_integration.utils

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.R
import com.example.chiefinventory.ui.actvity.SearchActivity
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for the image capture and gallery selection workflow in SearchActivity.
 */
@RunWith(AndroidJUnit4::class)
class ImageCaptureUtilSearchTest {

    /**
     * Verifies that clicking the camera button in SearchActivity does not crash.
     */
    @Test
    fun clickingCameraSearchButton_doesNotCrash() {
        val scenario = ActivityScenario.launch(SearchActivity::class.java)

        // WHEN: Clicking the camera icon in the search bar
        onView(withId(R.id.btn_search_by_camera)).perform(click())

        // THEN: No crash occurs.
        scenario.close()
    }

    /**
     * Verifies that clicking the gallery button in SearchActivity does not crash.
     */
    @Test
    fun clickingGallerySearchButton_doesNotCrash() {
        val scenario = ActivityScenario.launch(SearchActivity::class.java)

        // WHEN: Clicking the gallery icon in the search bar
        onView(withId(R.id.btn_search_by_gallery)).perform(click())

        // THEN: No crash occurs.
        scenario.close()
    }
}
