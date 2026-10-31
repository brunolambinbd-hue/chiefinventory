package com.chefinventory.app.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageStorageHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSaveImageToInternalStorage_ValidFileUri_SavesToImagesDirWhenDirExistsAndNotExists() {
        val tempFile = File(context.cacheDir, "temp_photo.jpg")
        tempFile.writeText("photo_bytes_data")

        val tempUri = Uri.fromFile(tempFile)

        // First call: imageDir does not exist yet
        val imagesDir = File(context.filesDir, "images")
        imagesDir.deleteRecursively()

        val savedUri1 = ImageStorageHelper.saveImageToInternalStorage(context, tempUri)
        assertThat(savedUri1).isNotNull()
        assertThat(savedUri1?.path).contains("images")

        // Second call: imageDir ALREADY exists
        val savedUri2 = ImageStorageHelper.saveImageToInternalStorage(context, tempUri)
        assertThat(savedUri2).isNotNull()

        // Cleanup
        tempFile.delete()
        if (savedUri1?.path != null) File(savedUri1.path!!).delete()
        if (savedUri2?.path != null) File(savedUri2.path!!).delete()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testSaveImageToInternalStorage_InvalidUri_ReturnsNull() {
        val invalidUri = Uri.parse("content://invalid.provider/missing.jpg")
        val savedUri = ImageStorageHelper.saveImageToInternalStorage(context, invalidUri)

        assertThat(savedUri).isNull()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testSaveImageToInternalStorage_NullContentResolver_ReturnsNullAndLogsError() {
        val mockContext = mock<Context> {
            on { contentResolver } doReturn (null as ContentResolver?)
            on { filesDir } doReturn context.filesDir
        }
        val savedUri = ImageStorageHelper.saveImageToInternalStorage(mockContext, Uri.parse("file:///tmp/sample.jpg"))
        assertThat(savedUri).isNull()
    }
}
