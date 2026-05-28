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
            
            // On récupère tous les mots
            val allWords = result.text.split("\\s+".toRegex())
                .map { it.trim().lowercase() }
            
            // On filtre : 
            // 1. Longueur >= 2 (pour Ed., BD, etc.)
            // 2. Pas de patterns type tirage "118/300"
            // 3. Pas uniquement des chiffres
            allWords.filter { word ->
                word.length >= 2 &&
                !word.matches(Regex(".*\\d+/\\d+.*")) && // Exclut 118/300
                !word.all { it.isDigit() } // Exclut les nombres purs
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
