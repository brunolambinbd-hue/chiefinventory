package com.chefinventory.app.ui.actvity

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.R
import com.chefinventory.app.utils.BitmapUtils
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Test unitaire Robolectric vérifiant la persistance et le stockage interne des photos de recettes lors de l'enregistrement dans [EditRecipeActivity].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditRecipeActivityImagePersistenceTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(R.style.Theme_ChefInventory)
    }

    @After
    fun tearDown() {
        val imagesDir = File(context.filesDir, "recipe_images")
        if (imagesDir.exists()) {
            imagesDir.deleteRecursively()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSaveRecipe_WithNewBitmap_SavesPhotoToInternalStorageAndReturnsPermanentUri() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val fileName = "recipe_${System.currentTimeMillis()}.jpg"

        val savedInternalUri = BitmapUtils.saveBitmapToInternalStorage(context, bitmap, fileName)

        assertThat(savedInternalUri).isNotNull()
        val expectedFile = File(context.filesDir, "recipe_images/$fileName")
        assertThat(expectedFile.exists()).isTrue()
        assertThat(expectedFile.length()).isGreaterThan(0L)

        val uriString = savedInternalUri.toString()
        assertThat(uriString).startsWith("file://")
        assertThat(uriString).contains("recipe_images")

        expectedFile.delete()
    }
}
