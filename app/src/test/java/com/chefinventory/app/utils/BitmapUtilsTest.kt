package com.chefinventory.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BitmapUtilsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBitmapUtils_ValidFileUri_DecodesArgb8888Bitmap() {
        // Create a dummy bitmap file in context cache directory
        val file = File(context.cacheDir, "test_image.png")
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = Uri.fromFile(file)
        val decoded = BitmapUtils.getBitmapFromUri(context, uri)

        assertThat(decoded).isNotNull()
        assertThat(decoded?.config).isEqualTo(Bitmap.Config.ARGB_8888)

        // Cleanup
        file.delete()
    }

    @Test
    @Config(sdk = [27])
    @CoveredRequirement("6.1.1.9")
    fun testBitmapUtils_LegacyApi27_DecodesBitmapViaMediaStore() {
        val file = File(context.cacheDir, "test_legacy_api27.png")
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = Uri.fromFile(file)
        val decoded = BitmapUtils.getBitmapFromUri(context, uri)

        assertThat(decoded).isNotNull()
        assertThat(decoded?.config).isEqualTo(Bitmap.Config.ARGB_8888)

        file.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBitmapUtils_Rgb565Bitmap_ConvertsToArgb8888() {
        val file = File(context.cacheDir, "test_rgb565.png")
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = Uri.fromFile(file)
        val decoded = BitmapUtils.getBitmapFromUri(context, uri)

        assertThat(decoded).isNotNull()
        assertThat(decoded?.config).isEqualTo(Bitmap.Config.ARGB_8888)

        file.delete()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testBitmapUtils_InvalidUri_ReturnsNull() {
        val mockContext = mock<Context>()
        whenever(mockContext.contentResolver).thenThrow(RuntimeException("ContentResolver error"))

        val invalidUri = Uri.parse("file:///invalid.png")
        val decoded = BitmapUtils.getBitmapFromUri(mockContext, invalidUri)

        assertThat(decoded).isNull()
    }
}
