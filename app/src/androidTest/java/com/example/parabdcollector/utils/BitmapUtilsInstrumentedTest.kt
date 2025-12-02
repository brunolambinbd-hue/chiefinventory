package com.example.parabdcollector.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumented tests for the [BitmapUtils] object.
 *
 * These tests run on an Android device or emulator to verify that the bitmap
 * decoding and conversion logic works correctly with the Android framework APIs.
 */
@RunWith(AndroidJUnit4::class)
class BitmapUtilsInstrumentedTest {

    private lateinit var context: Context

    /**
     * Sets up the test environment before each test.
     */
    @Before
    fun setup() {
        // Get the context from the instrumentation registry.
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Verifies that [BitmapUtils.getBitmapFromUri] correctly decodes a bitmap and converts it
     * to the required ARGB_8888 format.
     */
    @Test
    fun getBitmapFromUri_shouldDecodeAndConvertBitmapToARGB8888() {
        // GIVEN: A dummy bitmap with a non-ARGB_8888 config saved to a file.
        val tempBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565)
        val uri = saveBitmapToTempFileAndGetUri(tempBitmap)

        // WHEN: The getBitmapFromUri function is called.
        val resultBitmap = BitmapUtils.getBitmapFromUri(context, uri)

        // THEN: The resulting bitmap should not be null and should have the ARGB_8888 config.
        assertNotNull("The resulting bitmap should not be null", resultBitmap)
        assertEquals("Bitmap config should be ARGB_8888", Bitmap.Config.ARGB_8888, resultBitmap.config)
    }

    /**
     * Helper function to save a bitmap to a temporary file and get its content URI.
     * @param bitmap The bitmap to save.
     * @return The content URI of the saved file.
     */
    private fun saveBitmapToTempFileAndGetUri(bitmap: Bitmap): Uri {
        val cacheDir = context.cacheDir
        val tempFile = File.createTempFile("test_image", ".jpg", cacheDir)
        FileOutputStream(tempFile).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }
}
