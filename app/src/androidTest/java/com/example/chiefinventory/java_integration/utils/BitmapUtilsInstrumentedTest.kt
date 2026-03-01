package com.example.chiefinventory.java_integration.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.chiefinventory.utils.BitmapUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Verifies that `getBitmapFromUri` correctly decodes a valid bitmap and converts it to ARGB_8888.
     */
    @Test
    fun getBitmapFromUri_shouldDecodeAndConvertValidBitmap() {
        // GIVEN: A dummy bitmap saved to a file.
        val tempBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565)
        val uri = saveBitmapToTempFileAndGetUri(tempBitmap)

        // WHEN: The getBitmapFromUri function is called.
        val resultBitmap = BitmapUtils.getBitmapFromUri(context, uri)

        // THEN: The resulting bitmap should not be null and should have the ARGB_8888 config.
        assertNotNull("The resulting bitmap should not be null", resultBitmap)
        assertEquals("Bitmap config should be ARGB_8888", Bitmap.Config.ARGB_8888, resultBitmap!!.config)
    }

    /**
     * Verifies that `getBitmapFromUri` returns null and does not crash when given a non-image file.
     * This test locks in the stability fix.
     */
    @Test
    fun getBitmapFromUri_shouldReturnNullForInvalidImageFile() {
        // GIVEN: A text file, not an image.
        val textFile = File.createTempFile("not_an_image", ".txt", context.cacheDir).apply {
            writeText("this is not an image")
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            textFile
        )

        // WHEN: We try to decode it as a bitmap.
        val resultBitmap = BitmapUtils.getBitmapFromUri(context, uri)

        // THEN: The result should be null, and the app should not have crashed.
        assertNull("Bitmap should be null for a non-image file", resultBitmap)
    }

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
