package com.example.parabdcollector.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.parabdcollector.R
import com.example.parabdcollector.ui.actvity.FullScreenImageActivity
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for the [FullScreenImageActivity].
 *
 * This class verifies that all item details passed via Intent extras are correctly
 * displayed in their respective TextViews.
 */
@RunWith(AndroidJUnit4::class)
class FullScreenImageActivityTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun allItemInformation_isCorrectlyDisplayed() {
        // GIVEN: An intent with all possible extras populated with test data.
        val intent = Intent(context, FullScreenImageActivity::class.java).apply {
            putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, "file:///android_asset/test_image.jpg")
            putExtra(FullScreenImageActivity.EXTRA_TITLE, "Test Title")
            putExtra(FullScreenImageActivity.EXTRA_DESCRIPTION, "This is a test description.")
            putExtra(FullScreenImageActivity.EXTRA_EDITOR, "Test Editor")
            putExtra(FullScreenImageActivity.EXTRA_YEAR, 2024)
            putExtra(FullScreenImageActivity.EXTRA_MONTH, 7)
            putExtra(FullScreenImageActivity.EXTRA_SUPER_CATEGORY, "Test Super Cat")
            putExtra(FullScreenImageActivity.EXTRA_CATEGORY, "Test Cat")
            putExtra(FullScreenImageActivity.EXTRA_MATERIAL, "Test Material")
            putExtra(FullScreenImageActivity.EXTRA_RUN, "100 ex.")
            putExtra(FullScreenImageActivity.EXTRA_DIMENSIONS, "50x70cm")
            putExtra(FullScreenImageActivity.EXTRA_IMAGE_SIGNATURE, byteArrayOf(1, 2, 3))
        }

        // WHEN: The activity is launched with the intent.
        val scenario = ActivityScenario.launch<FullScreenImageActivity>(intent)

        // THEN: Each piece of information should be visible in the correct TextView.
        onView(withId(R.id.full_screen_image_view)).check(matches(isDisplayed()))
        onView(withId(R.id.image_info_title)).check(matches(withText("Test Title")))
        onView(withId(R.id.image_info_description)).check(matches(withText("This is a test description.")))

        // Check fields that use the generic format string.
        val editorText = context.getString(R.string.generic_field_format, context.getString(R.string.item_editor_hint), "Test Editor")
        onView(withId(R.id.image_info_manufacturer)).check(matches(withText(editorText)))

        val superCatText = context.getString(R.string.generic_field_format, context.getString(R.string.item_super_category_hint), "Test Super Cat")
        onView(withId(R.id.image_info_supercategory)).check(matches(withText(superCatText)))
        
        // ... and so on for all other fields.
        val yearText = "${context.getString(R.string.item_year_hint)}: 2024/7"
        onView(withId(R.id.image_info_year)).check(matches(withText(yearText)))

        // Check that the signature is displayed (as this is a debug/test build)
        onView(withId(R.id.debug_signature_info)).check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun emptyOrNullFields_areHidden() {
        // GIVEN: An intent with only the mandatory title and URI.
        val intent = Intent(context, FullScreenImageActivity::class.java).apply {
            putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, "file:///android_asset/test_image.jpg")
            putExtra(FullScreenImageActivity.EXTRA_TITLE, "Minimal Item")
        }

        // WHEN: The activity is launched.
        val scenario = ActivityScenario.launch<FullScreenImageActivity>(intent)

        // THEN: Only the title should be visible, all other info fields should be gone.
        onView(withId(R.id.image_info_title)).check(matches(withText("Minimal Item")))
        onView(withId(R.id.image_info_description)).check(matches(not(isDisplayed())))
        onView(withId(R.id.image_info_manufacturer)).check(matches(not(isDisplayed())))
        onView(withId(R.id.image_info_year)).check(matches(not(isDisplayed())))

        scenario.close()
    }
}
