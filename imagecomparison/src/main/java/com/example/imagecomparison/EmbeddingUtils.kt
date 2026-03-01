package com.example.imagecomparison // Changed package name

import android.util.Log
import com.google.mediapipe.tasks.components.containers.Embedding
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * A data class to hold embedding values when a full MediaPipe [Embedding] object cannot be reconstructed.
 * This serves as an internal, serializable representation.
 *
 * @property floatValues The float array for a standard embedding.
 * @property quantizedValues The byte array for a quantized embedding.
 * @property isQuantized A flag indicating which of the two arrays is populated.
 */
data class MyEmbedding(
    val floatValues: FloatArray? = null,
    val quantizedValues: ByteArray? = null,
    val isQuantized: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyEmbedding

        if (floatValues != null) {
            if (other.floatValues == null) return false
            if (!floatValues.contentEquals(other.floatValues)) return false
        } else if (other.floatValues != null) return false
        if (quantizedValues != null) {
            if (other.quantizedValues == null) return false
            if (!quantizedValues.contentEquals(other.quantizedValues)) return false
        } else if (other.quantizedValues != null) return false
        if (isQuantized != other.isQuantized) return false

        return true
    }

    override fun hashCode(): Int {
        var result = floatValues?.contentHashCode() ?: 0
        result = 31 * result + (quantizedValues?.contentHashCode() ?: 0)
        result = 31 * result + isQuantized.hashCode()
        return result
    }
}
/**
 * A utility object for serializing and deserializing MediaPipe [Embedding] objects.
 */
object EmbeddingUtils {

    private const val TAG = "EmbeddingUtils"

    /**
     * Converts a MediaPipe [Embedding] object to a [ByteArray] for database storage.
     *
     * It prioritizes the quantized embedding if available, otherwise it converts the float embedding
     * into a byte array using Little Endian byte order.
     *
     * @param embedding The MediaPipe embedding to convert.
     * @return A [ByteArray] representation of the embedding.
     * @throws IllegalArgumentException if the embedding is null or empty.
     */
    @Suppress("kotlin:S6529") // isNullOrEmpty() is not available for FloatArray, so this check is necessary.
    fun embeddingToByteArray(embedding: Embedding): ByteArray {
        // Vérifie d'abord l'embedding quantifié
        embedding.quantizedEmbedding()?.let {
            if (it.isNotEmpty()) return it
        }

        // Sinon, on vérifie l'embedding float
        val floatArray = embedding.floatEmbedding()

        // Crucial check: if the float array is null or empty, this is an error.
        require(floatArray != null && floatArray.isNotEmpty()) { "Embedding is null or empty, cannot convert." }

        val buffer = ByteBuffer.allocate(floatArray.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (f in floatArray) buffer.putFloat(f)
        return buffer.array()
    }

    /**
     * Reconstructs our internal [MyEmbedding] representation from a [ByteArray].
     *
     * @param bytes The serialized embedding data from the database.
     * @param fromQuantized True if the original embedding was quantized (INT8).
     * @return A [MyEmbedding] object containing the deserialized data.
     */
    fun byteArrayToMyEmbedding(bytes: ByteArray, fromQuantized: Boolean = false): MyEmbedding {
        return if (fromQuantized) {
            MyEmbedding(quantizedValues = bytes, isQuantized = true)
        } else {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val floats = FloatArray(bytes.size / 4)
            for (i in floats.indices) floats[i] = buffer.getFloat()
            MyEmbedding(floatValues = floats)
        }
    }

    /**
     * Example of use: converts an embedding to bytes and rebuilds it.
     * @param embedding The original embedding to process.
     */
    @Suppress("unused")
    fun demoUsage(embedding: Embedding) {
        val bytes = embeddingToByteArray(embedding)
        val restored = byteArrayToMyEmbedding(bytes, fromQuantized = embedding.quantizedEmbedding() != null)

        Log.d(TAG, "Original : ${bytes.size} bytes, restauré : ${if (restored.isQuantized) "quantized" else "float"}")
    }
}
