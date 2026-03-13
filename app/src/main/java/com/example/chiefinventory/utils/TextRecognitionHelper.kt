package com.example.chiefinventory.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * A helper class for performing OCR (Optical Character Recognition) using ML Kit.
 */
class TextRecognitionHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Recognizes text from a [Bitmap] and returns the raw text string.
     */
    suspend fun recognizeText(bitmap: Bitmap): String? = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text.ifBlank { null })
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Text recognition failed", e)
                continuation.resumeWithException(e)
            }
    }

    /**
     * Recognizes text from a [Bitmap] and returns the full structural [Text] object.
     * Use this for hierarchical parsing (Blocks -> Lines).
     */
    suspend fun recognizeVisionText(bitmap: Bitmap): Text? = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Vision Text recognition failed", e)
                continuation.resumeWithException(e)
            }
    }

    companion object {
        private const val TAG = "TextRecognitionHelper"
    }
}
