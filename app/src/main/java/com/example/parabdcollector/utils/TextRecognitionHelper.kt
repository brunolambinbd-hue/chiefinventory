package com.example.parabdcollector.utils

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility to recognize text from a Bitmap using Google ML Kit.
 */
object TextRecognitionHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extracts text from the given bitmap.
     * Returns a list of words found in the image.
     */
    suspend fun extractText(bitmap: Bitmap): List<String> = withContext(Dispatchers.Default) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = Tasks.await(recognizer.process(image))
            
            // On récupère tous les mots et on garde ceux de plus de 3 caractères
            result.text.split("\\s+".toRegex())
                .map { it.trim().lowercase() }
                .filter { it.length > 3 }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
