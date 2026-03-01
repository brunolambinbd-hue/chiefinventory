package com.example.chiefinventory.java_integration.utils

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.chiefinventory.utils.ImageStorageHelper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented tests for the [ImageStorageHelper] object.
 *
 * These tests run on an Android device or emulator to verify that file operations
 * involving the Android [Context] and [Uri] work as expected.
 */
@RunWith(AndroidJUnit4::class)
class ImageStorageHelperInstrumentedTest {

    private lateinit var context: Context

    /**
     * Sets up the test environment by acquiring the application context.
     */
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Verifies that [ImageStorageHelper.saveImageToInternalStorage] successfully
     * copies a temporary file to a new permanent location in internal storage.
     */
    @Test
    fun saveImageToInternalStorage_shouldCreateNewPermanentFile() {
        // GIVEN: A temporary file with some dummy content.
        val tempFile = File.createTempFile("temp_image", ".txt", context.cacheDir).apply {
            writeText("dummy content")
        }
        val tempUri = Uri.fromFile(tempFile)

        // WHEN: The saveImageToInternalStorage function is called.
        val permanentUri = ImageStorageHelper.saveImageToInternalStorage(context, tempUri)

        // THEN: The returned URI should not be null and the corresponding file should exist.
        assertNotNull("The permanent URI should not be null", permanentUri)

        val permanentFile = File(permanentUri!!.path!!)
        assertTrue("The permanent file should exist", permanentFile.exists())
        assertTrue("The permanent file should not be empty", permanentFile.length() > 0)

        // Clean up the created files.
        tempFile.delete()
        permanentFile.delete()
    }
}
