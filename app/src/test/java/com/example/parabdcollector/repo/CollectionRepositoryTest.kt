package com.example.parabdcollector.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@ExperimentalCoroutinesApi
class CollectionRepositoryTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var collectionDao: CollectionDao
    private lateinit var repository: CollectionRepository

    @Before
    fun setup() {
        collectionDao = mock()
        repository = CollectionRepository(collectionDao)
    }

    /**
     * Creates a dummy embedding that has a predictable cosine similarity to the query vector.
     * The query vector used in tests is [1, 0, 0, ...].
     */
    private fun createPredictableEmbedding(similarityValue: Float): ByteArray {
        val floatArray = FloatArray(10) { 0f }
        floatArray[0] = similarityValue
        floatArray[1] = kotlin.math.sqrt(1f - (similarityValue * similarityValue))

        val buffer = ByteBuffer.allocate(floatArray.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (f in floatArray) buffer.putFloat(f)
        return buffer.array()
    }

    private fun createTestItem(id: Long, titre: String, editeur: String = "Dupuis", dimensions: String? = null, embedding: ByteArray? = null): CollectionItem {
        return CollectionItem(
            id = id,
            titre = titre,
            editeur = editeur,
            dimensions = dimensions ?: "20 x 30",
            imageEmbedding = embedding,
            annee = 2024
        )
    }

    @Test
    fun `findMostSimilarItems should return top 3 sorted results`(): Unit = runTest {
        val queryEmbedding = FloatArray(10) { 0f }.apply { this[0] = 1f }
        val allItems = listOf(
            createTestItem(1, "Low", embedding = createPredictableEmbedding(0.1f)),
            createTestItem(2, "High", embedding = createPredictableEmbedding(0.95f)),
            createTestItem(3, "Medium", embedding = createPredictableEmbedding(0.5f)),
            createTestItem(4, "Very High", embedding = createPredictableEmbedding(0.99f))
        )
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(allItems)

        val results = repository.findMostSimilarItems(queryEmbedding)

        assertEquals(3, results.size)
        assertEquals("Very High", results[0].item.titre)
        assertEquals("High", results[1].item.titre)
        assertEquals("Medium", results[2].item.titre)
    }

    @Test
    fun `advancedSearch with OCR should boost similarity score`(): Unit = runTest {
        // GIVEN: A query with an embedding and detected words
        val queryEmbedding = FloatArray(10) { 0f }.apply { this[0] = 1f }
        val detectedWords = listOf("Dupuis", "Tintin")
        
        // Item with 0.7 similarity + "Dupuis" match (+0.10) + "Tintin" match (+0.05) = 0.85
        val item = createTestItem(1, "Tintin au Tibet", editeur = "Dupuis", embedding = createPredictableEmbedding(0.7f))
        
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(listOf(item))

        // WHEN: Calling advancedSearch
        val result = repository.advancedSearch(SearchCriteria(), queryEmbedding, detectedWords)

        // THEN: Score should be boosted
        val score = result.results[0].similarity ?: 0.0
        assertTrue("Score should be boosted by OCR: $score", score > 0.84 && score < 0.86)
    }

    @Test
    fun `advancedSearch with physical dimensions should penalize mismatch`(): Unit = runTest {
        // GIVEN: Query dimensions 20x30
        val queryEmbedding = FloatArray(10) { 0f }.apply { this[0] = 1f }
        val criteria = SearchCriteria(detectedWidth = 20.0, detectedHeight = 30.0)
        
        // Item with dimensions 40x50 (Mismatch)
        val item = createTestItem(1, "Big Item", dimensions = "40 x 50", embedding = createPredictableEmbedding(0.9f))
        
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(listOf(item))

        // WHEN
        val result = repository.advancedSearch(criteria, queryEmbedding)

        // THEN: Score should be penalized (-0.50) -> 0.4
        val score = result.results[0].similarity ?: 0.0
        assertTrue("Score should be penalized for dimension mismatch: $score", score < 0.5)
    }

    @Test
    fun `advancedSearch with physical dimensions should boost match`(): Unit = runTest {
        // GIVEN: Query dimensions 20x30
        val queryEmbedding = FloatArray(10) { 0f }.apply { this[0] = 1f }
        val criteria = SearchCriteria(detectedWidth = 20.0, detectedHeight = 30.0)
        
        // Item with dimensions 20x30 (Match)
        val item = createTestItem(1, "Perfect Match", dimensions = "20 x 30", embedding = createPredictableEmbedding(0.8f))
        
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(listOf(item))

        // WHEN
        val result = repository.advancedSearch(criteria, queryEmbedding)

        // THEN: Score should be boosted (+0.05) -> 0.85
        val score = result.results[0].similarity ?: 0.0
        assertTrue("Score should be boosted for dimension match: $score", score > 0.84)
    }

    @Test
    fun `advancedSearch fallback to aspect ratio when physical dimensions are missing`(): Unit = runTest {
        // GIVEN: Aspect ratio 1.5 (e.g., 30/20)
        val queryEmbedding = FloatArray(10) { 0f }.apply { this[0] = 1f }
        val criteria = SearchCriteria(queryAspectRatio = 1.5)
        
        // Item with ratio 1.0 (e.g., 20x20) -> Mismatch
        val item = createTestItem(1, "Square Item", dimensions = "20 x 20", embedding = createPredictableEmbedding(0.9f))
        
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(listOf(item))

        // WHEN
        val result = repository.advancedSearch(criteria, queryEmbedding)

        // THEN: Score should be slightly penalized (-0.15) -> 0.75
        val score = result.results[0].similarity ?: 0.0
        assertTrue("Score should be penalized for ratio mismatch: $score", score < 0.8)
    }

    @Test
    fun `getSignatureStats should aggregate correctly`(): Unit = runTest {
        // GIVEN
        val items = listOf(
            createTestItem(1, "Valid", embedding = createPredictableEmbedding(0.5f)),
            createTestItem(2, "Empty", embedding = ByteArray(0)),
            createTestItem(3, "Missing", embedding = null),
            createTestItem(4, "Valid 2", embedding = createPredictableEmbedding(0.6f))
        )
        whenever(collectionDao.getAllSuspend()).thenReturn(items)

        // WHEN
        val stats = repository.getSignatureStats().getOrAwaitValue()

        // THEN
        assertEquals("Total count mismatch", 4, stats.totalCount)
        assertEquals("Valid count mismatch", 2, stats.validCount)
        assertEquals("Empty count mismatch", 1, stats.emptyCount)
        assertEquals("Missing count mismatch", 1, stats.missingCount)
    }

    /**
     * Helper to observe a LiveData and return its value, waiting for up to 2 seconds.
     */
    private fun <T> androidx.lifecycle.LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }

        this.observeForever(observer)

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            this.removeObserver(observer)
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }

    @Test
    fun `advancedSearch should separate high confidence results`(): Unit = runTest {
        // GIVEN
        val queryEmbedding = FloatArray(10) { 0f }.apply { this[0] = 1f }
        val items = listOf(
            createTestItem(1, "High Confidence", embedding = createPredictableEmbedding(0.8f)),
            createTestItem(2, "Low Confidence", embedding = createPredictableEmbedding(0.4f))
        )
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(items)

        // WHEN
        val result = repository.advancedSearch(SearchCriteria(), queryEmbedding)

        // THEN
        assertTrue("Should not be fallback mode if high confidence items exist", !result.isFallback)
        assertEquals(1, result.results.size)
        assertEquals("High Confidence", result.results[0].item.titre)
    }

    @Test
    fun `advancedSearch with strict dimension filter should exclude mismatching items`(): Unit = runTest {
        // GIVEN: Query dimensions 48x70
        val criteria = SearchCriteria(detectedWidth = 48.0, detectedHeight = 70.0)
        
        val items = listOf(
            createTestItem(1, "Matching Item", dimensions = "48 x 70"),
            createTestItem(2, "Mismatching Item (Small)", dimensions = "17 x 24.5"),
            createTestItem(3, "Mismatching Item (Close but off)", dimensions = "40 x 70")
        )
        // We use getAllSuspend because it's a dimension search without image and without text criteria in this test
        whenever(collectionDao.getAllSuspend()).thenReturn(items)

        // WHEN
        val result = repository.advancedSearch(criteria, null)

        // THEN
        assertEquals("Only the matching item should remain", 1, result.results.size)
        assertEquals("Matching Item", result.results[0].item.titre)
        assertTrue("Score should be high for match", result.results[0].similarity!! > 0.5)
    }
}
