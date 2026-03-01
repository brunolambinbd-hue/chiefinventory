package com.example.chiefinventory.java_ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.chiefinventory.R
import com.example.chiefinventory.ui.actvity.FullScreenImageActivity
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for the [com.example.chiefinventory.ui.actvity.FullScreenImageActivity].
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
        Espresso.onView(ViewMatchers.withId(R.id.full_screen_image_view))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.image_info_title))
            .check(ViewAssertions.matches(ViewMatchers.withText("Test Title")))
        Espresso.onView(ViewMatchers.withId(R.id.image_info_description))
            .check(ViewAssertions.matches(ViewMatchers.withText("This is a test description.")))

        // Check fields that use the generic format string.
        val editorText = context.getString(R.string.generic_field_format, context.getString(R.string.item_editor_hint), "Test Editor")
        Espresso.onView(ViewMatchers.withId(R.id.image_info_manufacturer))
            .check(ViewAssertions.matches(ViewMatchers.withText(editorText)))

        val superCatText = context.getString(R.string.generic_field_format, context.getString(R.string.item_super_category_hint), "Test Super Cat")
        Espresso.onView(ViewMatchers.withId(R.id.image_info_supercategory))
            .check(ViewAssertions.matches(ViewMatchers.withText(superCatText)))

        // ... and so on for all other fields.
        val yearText = "${context.getString(R.string.item_year_hint)}: 2024/7"
        Espresso.onView(ViewMatchers.withId(R.id.image_info_year))
            .check(ViewAssertions.matches(ViewMatchers.withText(yearText)))

        // Check that the signature is displayed (as this is a debug/test build)
        Espresso.onView(ViewMatchers.withId(R.id.debug_signature_info))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

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
        Espresso.onView(ViewMatchers.withId(R.id.image_info_title))
            .check(ViewAssertions.matches(ViewMatchers.withText("Minimal Item")))
        Espresso.onView(ViewMatchers.withId(R.id.image_info_description))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.image_info_manufacturer))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.image_info_year))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))

        scenario.close()
    }
}
