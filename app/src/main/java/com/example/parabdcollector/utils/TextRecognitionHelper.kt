package com.example.parabdcollector.utils

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interface defining the contract for text recognition services.
 *
 * This abstraction allows for the extraction of textual content from images while
 * facilitating loose coupling and enabling the use of mock implementations in unit tests.
 */
interface ITextRecognizer {
    suspend fun extractText(bitmap: Bitmap): List<String>
}

/**
 * Utility to recognize text from a Bitmap using Google ML Kit.
 */
object TextRecognitionHelper : ITextRecognizer {
    // Utilisation de 'by lazy' pour permettre le mocking en test unitaire
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    /**
     * Extracts text from the given bitmap.
     * Returns a list of words found in the image.
     */
    // Version utilisée par l'application
    override suspend fun extractText(bitmap: Bitmap): List<String> =
        extractText(bitmap, Dispatchers.Default)

    // Version permettant l'injection (pour les tests)
    suspend fun extractText(bitmap: Bitmap, dispatcher: kotlinx.coroutines.CoroutineDispatcher): List<String> =
        withContext(dispatcher) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = Tasks.await(recognizer.process(image))
                filterWords(result.text)
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * Filters and cleans the raw text from OCR.
     */
    fun filterWords(rawText: String?): List<String> {
        if (rawText.isNullOrBlank()) return emptyList()

        // On récupère tous les mots
        val allWords = rawText.split("\\s+".toRegex())
            .map { it.trim().lowercase() }

        // On filtre :
        // 1. Longueur >= 2 (pour Ed., BD, etc.)
        // 2. Pas de patterns type tirage "118/300"
        // 3. Pas uniquement des chiffres
        return allWords.filter { word ->
            word.length >= 2 &&
                    !word.matches(Regex(".*\\d+/\\d+.*")) && // Exclut 118/300
                    !word.all { it.isDigit() } // Exclut les nombres purs
        }
    }
}
