package com.example.parabdcollector.utils

import android.content.Context
import com.example.parabdcollector.R
import java.nio.ByteBuffer
import java.nio.ByteOrder


object SignatureUtils {

    // Version pour les signatures stockées en ByteArray dans la base de données
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

    // Version pour les signatures venant d'être calculées (en FloatArray)
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

    private fun toFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val floatArray = FloatArray(bytes.size / 4)
        buffer.asFloatBuffer().get(floatArray)
        return floatArray
    }
}