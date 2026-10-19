package com.example.imagecomparison

import com.google.common.truth.Truth.assertThat
import com.google.mediapipe.tasks.components.containers.Embedding
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Unit tests for the [EmbeddingUtils] object.
 *
 * These tests verify the conversion logic between MediaPipe's [Embedding]
 * and a serializable [ByteArray] for both float and quantized embeddings.
 */
class EmbeddingUtilsTest {

    /**
     * Verifies that `embeddingToByteArray` throws an [IllegalArgumentException]
     * when given an empty float embedding.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `embeddingToByteArray should throw exception for empty float embedding`() {
        val mockEmbedding: Embedding = mock()
        whenever(mockEmbedding.floatEmbedding()).thenReturn(floatArrayOf())
        whenever(mockEmbedding.quantizedEmbedding()).thenReturn(null)

        EmbeddingUtils.embeddingToByteArray(mockEmbedding)
    }

    /**
     * Verifies that `embeddingToByteArray` throws an [IllegalArgumentException]
     * when both quantized and float embeddings are empty.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `embeddingToByteArray should throw exception for all empty embeddings`() {
        val mockEmbedding: Embedding = mock()
        whenever(mockEmbedding.quantizedEmbedding()).thenReturn(byteArrayOf())
        whenever(mockEmbedding.floatEmbedding()).thenReturn(floatArrayOf())

        EmbeddingUtils.embeddingToByteArray(mockEmbedding)
    }

    /**
     * Verifies that `embeddingToByteArray` correctly converts a valid float embedding.
     */
    @Test
    fun `embeddingToByteArray should correctly convert a valid float embedding`() {
        val testFloats = floatArrayOf(0.1f, -0.2f, 1.0f)
        val mockEmbedding: Embedding = mock()
        whenever(mockEmbedding.floatEmbedding()).thenReturn(testFloats)
        whenever(mockEmbedding.quantizedEmbedding()).thenReturn(null)

        val resultBytes = EmbeddingUtils.embeddingToByteArray(mockEmbedding)

        assertThat(resultBytes).hasLength(12)
        val buffer = ByteBuffer.wrap(resultBytes).order(ByteOrder.LITTLE_ENDIAN)
        val restoredFloats = FloatArray(3) { buffer.getFloat() }
        assertThat(restoredFloats.toList()).containsExactly(0.1f, -0.2f, 1.0f).inOrder()
    }

    /**
     * Verifies that `embeddingToByteArray` directly returns a valid quantized embedding.
     */
    @Test
    fun `embeddingToByteArray should correctly return a valid quantized embedding`() {
        val testBytes = byteArrayOf(10, 20, 30, 40)
        val mockEmbedding: Embedding = mock()
        whenever(mockEmbedding.quantizedEmbedding()).thenReturn(testBytes)
        whenever(mockEmbedding.floatEmbedding()).thenReturn(floatArrayOf(0.1f))

        val resultBytes = EmbeddingUtils.embeddingToByteArray(mockEmbedding)

        assertThat(resultBytes).isSameInstanceAs(testBytes)
    }

    /**
     * Verifies that `byteArrayToMyEmbedding` correctly restores a float embedding.
     */
    @Test
    fun `byteArrayToMyEmbedding should correctly restore a float embedding`() {
        val buffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putFloat(0.1f).putFloat(-0.2f).putFloat(1.0f)
        val sourceBytes = buffer.array()

        val resultEmbedding = EmbeddingUtils.byteArrayToMyEmbedding(sourceBytes, fromQuantized = false)

        assertThat(resultEmbedding.floatValues).isNotNull()
        assertThat(resultEmbedding.floatValues!!.toList()).containsExactly(0.1f, -0.2f, 1.0f).inOrder()
    }

    /**
     * Verifies that `byteArrayToMyEmbedding` correctly restores a quantized embedding.
     */
    @Test
    fun `byteArrayToMyEmbedding should correctly restore a quantized embedding`() {
        val sourceBytes = byteArrayOf(-10, 20, -30, 40)

        val resultEmbedding = EmbeddingUtils.byteArrayToMyEmbedding(sourceBytes, fromQuantized = true)

        assertThat(resultEmbedding.quantizedValues).isNotNull()
        assertThat(resultEmbedding.floatValues).isNull()
        assertThat(resultEmbedding.isQuantized).isTrue()
        assertThat(resultEmbedding.quantizedValues).isEqualTo(sourceBytes)
    }
}
