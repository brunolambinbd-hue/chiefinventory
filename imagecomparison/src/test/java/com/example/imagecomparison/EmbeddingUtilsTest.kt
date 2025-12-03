package com.example.imagecomparison

import com.google.mediapipe.tasks.components.containers.Embedding
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
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
     * when given an empty float embedding. This locks in the fix for the regression.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `embeddingToByteArray should throw exception for empty float embedding`() {
        // GIVEN: A mock embedding that returns an empty float array and no quantized array
        val mockEmbedding: Embedding = mock()
        whenever(mockEmbedding.floatEmbedding()).thenReturn(floatArrayOf())
        whenever(mockEmbedding.quantizedEmbedding()).thenReturn(null)

        // WHEN: We try to convert it to a ByteArray
        EmbeddingUtils.embeddingToByteArray(mockEmbedding)

        // THEN: An IllegalArgumentException is expected.
    }

    /**
     * Verifies that `embeddingToByteArray` throws an [IllegalArgumentException]
     * when both quantized and float embeddings are empty.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `embeddingToByteArray should throw exception for all empty embeddings`() {
        // GIVEN: A mock embedding that returns empty arrays for both types
        val mockEmbedding: Embedding = mock()
        whenever(mockEmbedding.quantizedEmbedding()).thenReturn(byteArrayOf())
        whenever(mockEmbedding.floatEmbedding()).thenReturn(floatArrayOf())

        // WHEN: We try to convert it to a ByteArray (the code should fall through the quantized check)
        EmbeddingUtils.embeddingToByteArray(mockEmbedding)

        // THEN: An IllegalArgumentException is expected.
    }

    /**
     * Verifies that `embeddingToByteArray` correctly converts a valid float embedding.
     */
    @Test
    fun `embeddingToByteArray should correctly convert a valid float embedding`() {
        // GIVEN: A mock embedding with a valid float array
        val testFloats = floatArrayOf(0.1f, -0.2f, 1.0f)
        val mockEmbedding: Embedding = mock()
        whenever(mockEmbedding.floatEmbedding()).thenReturn(testFloats)
        whenever(mockEmbedding.quantizedEmbedding()).thenReturn(null)

        // WHEN: We convert it
        val resultBytes = EmbeddingUtils.embeddingToByteArray(mockEmbedding)

        // THEN: The resulting ByteArray should have the correct size and content
        assertEquals(12, resultBytes.size)
        val buffer = ByteBuffer.wrap(resultBytes).order(ByteOrder.LITTLE_ENDIAN)
        val restoredFloats = FloatArray(3) { buffer.getFloat() }
        assertArrayEquals(testFloats, restoredFloats, 0.001f)
    }

    /**
     * Verifies that `embeddingToByteArray` directly returns a valid quantized embedding.
     */
    @Test
    fun `embeddingToByteArray should correctly return a valid quantized embedding`() {
        // GIVEN: A mock embedding with a valid quantized byte array
        val testBytes = byteArrayOf(10, 20, 30, 40)
        val mockEmbedding: Embedding = mock()
        whenever(mockEmbedding.quantizedEmbedding()).thenReturn(testBytes)
        // The float embedding can be anything, it should be ignored
        whenever(mockEmbedding.floatEmbedding()).thenReturn(floatArrayOf(0.1f))

        // WHEN: We convert it
        val resultBytes = EmbeddingUtils.embeddingToByteArray(mockEmbedding)

        // THEN: The result should be the exact same byte array instance
        assertSame("Should return the same array instance for quantized embedding", testBytes, resultBytes)
    }

    /**
     * Verifies that `byteArrayToMyEmbedding` correctly restores a float embedding.
     */
    @Test
    fun `byteArrayToMyEmbedding should correctly restore a float embedding`() {
        // GIVEN: A ByteArray representing a float embedding
        val buffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putFloat(0.1f).putFloat(-0.2f).putFloat(1.0f)
        val sourceBytes = buffer.array()

        // WHEN: We convert it back to our custom embedding class
        val resultEmbedding = EmbeddingUtils.byteArrayToMyEmbedding(sourceBytes, fromQuantized = false)

        // THEN: The resulting object should contain the correct float array
        assert(resultEmbedding.floatValues != null)
        assertArrayEquals(floatArrayOf(0.1f, -0.2f, 1.0f), resultEmbedding.floatValues!!, 0.001f)
    }

    /**
     * Verifies that `byteArrayToMyEmbedding` correctly restores a quantized embedding.
     */
    @Test
    fun `byteArrayToMyEmbedding should correctly restore a quantized embedding`() {
        // GIVEN: A ByteArray representing a quantized embedding
        val sourceBytes = byteArrayOf(-10, 20, -30, 40)

        // WHEN: We convert it back, marking it as quantized
        val resultEmbedding = EmbeddingUtils.byteArrayToMyEmbedding(sourceBytes, fromQuantized = true)

        // THEN: The resulting object should contain the correct byte array and flag
        assert(resultEmbedding.quantizedValues != null)
        assert(resultEmbedding.floatValues == null)
        assertEquals(true, resultEmbedding.isQuantized)
        assertArrayEquals(sourceBytes, resultEmbedding.quantizedValues!!)
    }
}
