package com.example.imagecomparison // Changed package name

import com.google.mediapipe.tasks.components.containers.Embedding
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Classe interne de remplacement pour stocker des embeddings
 * quand on ne peut pas reconstruire un Embedding MediaPipe.
 */
data class MyEmbedding(
    val floatValues: FloatArray? = null,
    val quantizedValues: ByteArray? = null,
    val isQuantized: Boolean = false
)
/**
 * Utilitaires pour convertir les embeddings MediaPipe en ByteArray et inversement.
 *
 * Compatible avec :
 *  - com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
 *  - com.google.mediapipe.tasks.components.containers.Embedding
 */
object EmbeddingUtils {

    /**
     * Convertit un Embedding en ByteArray.
     * - Si embedding quantifié → retourne directement quantizedEmbedding()
     * - Si float → convertit chaque float en 4 octets (Little Endian)
     */
    fun embeddingToByteArray(embedding: Embedding): ByteArray {
        embedding.quantizedEmbedding()?.let { return it } // déjà en bytes

        val floats = embedding.floatEmbedding()
            ?: throw IllegalArgumentException("Embedding ne contient ni float ni quantized data")

        val buffer = ByteBuffer.allocate(floats.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (f in floats) buffer.putFloat(f)
        return buffer.array()
    }

    /**
     * Recrée un Embedding à partir d’un ByteArray.
     * @param bytes les données de l’embedding sérialisé
     * @param fromQuantized true si l’embedding d’origine était quantifié (INT8)
     */
    fun byteArrayToMyEmbedding(bytes: ByteArray, fromQuantized: Boolean = false): MyEmbedding {
        return if (fromQuantized) {
            MyEmbedding(quantizedValues = bytes, isQuantized = true)
        } else {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val floats = FloatArray(bytes.size / 4)
            for (i in floats.indices) floats[i] = buffer.getFloat()
            MyEmbedding(floatValues = floats, isQuantized = false)
        }
    }

    /**
     * Exemple d’utilisation : convertit un embedding en bytes et le reconstruit.
     */
    fun demoUsage(embedding: Embedding) {
        val bytes = embeddingToByteArray(embedding)
        val restored = byteArrayToMyEmbedding(bytes, fromQuantized = embedding.quantizedEmbedding() != null)

        println("Original : ${bytes.size} bytes, restauré : ${if (restored.isQuantized) "quantized" else "float"}")
    }
}