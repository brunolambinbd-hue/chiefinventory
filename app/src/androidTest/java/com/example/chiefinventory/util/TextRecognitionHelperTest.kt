package com.example.chiefinventory.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.utils.TextRecognitionHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for [TextRecognitionHelper].
 * OCR tests must be instrumented because ML Kit requires a context and native libraries.
 */
@RunWith(AndroidJUnit4::class)
class TextRecognitionHelperTest {

    private lateinit var helper: TextRecognitionHelper

    @Before
    fun setup() {
        helper = TextRecognitionHelper(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun recognizeText_shouldReturnCorrectTextFromBitmap() = runBlocking {
        // GIVEN: A bitmap with the text "CHEF" drawn on it
        val testText = "CHEF"
        val bitmap = createBitmapWithText(testText)

        // WHEN: Recognizing text
        val result = helper.recognizeText(bitmap)

        // THEN: The result should contain our test text
        assertThat(result).isNotNull()
        assertThat(result?.uppercase()).contains(testText)
    }

    @Test
    fun recognizeText_withCookingIngredients_shouldReturnCorrectText() = runBlocking {
        // GIVEN: A bitmap with French cooking ingredients
        val ingredients = listOf("Farine", "Oeufs", "Sucre", "Beurre")
        val bitmap = createBitmapWithLines(ingredients)

        // WHEN: Recognizing text
        val result = helper.recognizeText(bitmap)

        // THEN: All ingredients should be found in the OCR result
        assertThat(result).isNotNull()
        val resultText = result!!.lowercase()
        ingredients.forEach { ingredient ->
            assertThat(resultText).contains(ingredient.lowercase())
        }
    }

    @Test
    fun recognizeText_withEmptyBitmap_shouldReturnNull() = runBlocking {
        // GIVEN: A blank bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        // WHEN: Recognizing text
        val result = helper.recognizeText(bitmap)

        // THEN: Result should be null or blank
        assertThat(result).isNull()
    }

    /**
     * Helper to create a Bitmap with specific text for testing.
     */
    private fun createBitmapWithText(text: String): Bitmap {
        val bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            isAntiAlias = true
        }
        
        canvas.drawText(text, 50f, 60f, paint)
        return bitmap
    }

    /**
     * Helper to create a Bitmap with multiple lines of text.
     */
    private fun createBitmapWithLines(lines: List<String>): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            isAntiAlias = true
        }
        
        var y = 50f
        lines.forEach { line ->
            canvas.drawText(line, 50f, y, paint)
            y += 50f
        }
        return bitmap
    }
}
