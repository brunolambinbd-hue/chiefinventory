package com.example.imagecomparison

import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the [ImageEmbedderHelper].
 *
 * These tests run on an Android device or emulator to verify that the MediaPipe
 * ImageEmbedder can be initialized and can compute valid signatures and comparisons.
 */
@RunWith(AndroidJUnit4::class)
class ImageEmbedderHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Verifies that the helper computes a valid signature using the default (LARGE) model.
     */
    @Test
    fun computeSignature_shouldReturnValidEmbeddingForLargeModel() {
        val imageEmbedderHelper = ImageEmbedderHelper(context, null)
        val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val embeddingResult = imageEmbedderHelper.computeSignature(dummyBitmap)

        assertNotNull("Embedding result should not be null", embeddingResult)
        val floatEmbedding = embeddingResult?.floatEmbedding()
        assertEquals(1280, floatEmbedding?.size)

        imageEmbedderHelper.clearImageEmbedder()
    }

    /**
     * Verifies that the helper computes a valid signature when explicitly using the SMALL model.
     */
    @Test
    fun computeSignature_shouldReturnValidEmbeddingForSmallModel() {
        val imageEmbedderHelper = ImageEmbedderHelper(
            context = context,
            currentDelegate = ImageEmbedderHelper.DELEGATE_CPU,
            currentModel = ImageEmbedderHelper.MODEL_MOBILENETV3_SMALL,
            listener = null
        )
        val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val embeddingResult = imageEmbedderHelper.computeSignature(dummyBitmap)

        assertNotNull(embeddingResult)
        val floatEmbedding = embeddingResult?.floatEmbedding()
        assertEquals(1024, floatEmbedding?.size)

        imageEmbedderHelper.clearImageEmbedder()
    }

    /**
     * Verifies that `computeSignature` returns null and does not crash when given a recycled (invalid) bitmap.
     * This test locks in the stability fix.
     */
    @Test
    fun computeSignature_shouldReturnNullForRecycledBitmap() {
        // GIVEN: An instance of the helper and a bitmap that is then recycled
        val imageEmbedderHelper = ImageEmbedderHelper(context, null)
        val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        dummyBitmap.recycle() // Makes the bitmap invalid

        // WHEN: We try to compute the signature for the invalid bitmap
        val embeddingResult = imageEmbedderHelper.computeSignature(dummyBitmap)

        // THEN: The result should be null, and the app should not have crashed.
        assertNull("Embedding result should be null for a recycled bitmap", embeddingResult)

        imageEmbedderHelper.clearImageEmbedder()
    }

    /**
     * Verifies that the `embed` function correctly calculates a similarity of nearly 1.0 for identical images.
     */
    @Test
    fun embed_shouldReturnHighSimilarityForIdenticalImages() {
        val imageEmbedderHelper = ImageEmbedderHelper(context, null)
        val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val resultBundle = imageEmbedderHelper.embed(dummyBitmap, dummyBitmap)

        assertNotNull("Result bundle should not be null", resultBundle)
        assertEquals(1.0, resultBundle!!.similarity, 0.001)

        imageEmbedderHelper.clearImageEmbedder()
    }

    /**
     * Verifies that after calling `clearImageEmbedder`, the helper can no longer compute signatures.
     */
    @Test
    fun clearImageEmbedder_shouldPreventFurtherComputations() {
        val imageEmbedderHelper = ImageEmbedderHelper(context, null)
        val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        assertNotNull(imageEmbedderHelper.computeSignature(dummyBitmap))

        imageEmbedderHelper.clearImageEmbedder()

        val resultAfterClear = imageEmbedderHelper.computeSignature(dummyBitmap)
        assertNull("Signature should be null after clearing the embedder", resultAfterClear)
    }
}
