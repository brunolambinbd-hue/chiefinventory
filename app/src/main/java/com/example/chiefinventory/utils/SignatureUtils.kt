package com.example.chiefinventory.utils

import android.content.Context
import com.example.chiefinventory.R
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A utility object for formatting and handling image signatures (embeddings) for UI display.
 */
object SignatureUtils {

    /**
     * Formats a signature from a [ByteArray] for display in the UI.
     *
     * This version is typically used for embeddings loaded from the database.
     * It handles null or empty cases by providing a descriptive status from string resources.
     *
     * @param context The application context, used to access string resources.
     * @param embedding The nullable [ByteArray] representing the image signature.
     * @return A formatted string (e.g., "[0.12, -0.45,...]", "Manquante", "Vide").
     */
    fun formatSignaturePreview(context: Context, embedding: ByteArray?): String {
        return embedding?.let {
            if (it.isNotEmpty()) {
                val floatArray = toFloatArray(it)
                formatSignaturePreview(context, floatArray)
            } else {
                context.getString(R.string.signature_status_empty)
            }
        } ?: context.getString(R.string.signature_status_missing)
    }

    /**
     * Formats a signature from a [FloatArray] for display in the UI.
     *
     * This version is typically used for newly computed embeddings before they are saved.
     * It shows a preview of the first few floating-point values.
     *
     * @param context The application context, used to access string resources.
     * @param embedding The nullable [FloatArray] representing the image signature.
     * @return A formatted string showing a preview of the signature (e.g., "[0.12, -0.45,...]").
     */
    fun formatSignaturePreview(context: Context, embedding: FloatArray?): String {
        return embedding?.let {
            if (it.isNotEmpty()) {
                val preview = it.take(5).joinToString(", ") { value ->
                    "%.2f".format(value)
                }
                context.getString(R.string.signature_preview_format, preview)
            } else {
                context.getString(R.string.signature_status_empty)
            }
        } ?: context.getString(R.string.signature_status_missing)
    }

    /**
     * Converts a [ByteArray] back into a [FloatArray].
     *
     * This assumes the byte array was created from a float array using LITTLE_ENDIAN byte order.
     *
     * @param bytes The raw byte array from the database.
     * @return The resulting [FloatArray].
     */
    private fun toFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val fb = buffer.asFloatBuffer()
        return FloatArray(bytes.size / 4).apply {
            for (i in indices) {
                this[i] = fb.get()
            }
        }
    }
}
