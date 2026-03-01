package com.example.chiefinventory.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.model.CollectionItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.ByteBuffer
import java.nio.ByteOrder

@ExperimentalCoroutinesApi
class CollectionRepositoryTest {

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
     * The query vector is [1, 0, 0, ...].
     * This dummy vector is [value, sqrt(1-value^2), 0, ...], ensuring it's normalized.
     * Their dot product (and cosine similarity) will be exactly `value`.
     */
    private fun createPredictableEmbedding(similarityValue: Float): ByteArray {
        val floatArray = FloatArray(10) { 0f }
        floatArray[0] = similarityValue
        floatArray[1] = kotlin.math.sqrt(1f - similarityValue * similarityValue)

        val buffer = ByteBuffer.allocate(floatArray.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (f in floatArray) buffer.putFloat(f)
        return buffer.array()
    }

    @Test
    fun `findMostSimilarItems should return top 3 sorted results`(): Unit = runTest {
        // GIVEN: A normalized query embedding and a list of items with predictable similarities.
        val queryEmbedding = FloatArray(10) { 0f }.apply { this[0] = 1f }

        val allItemsWithEmbeddings = listOf(
            CollectionItem(id = 1, titre = "Low Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.1f)),
            CollectionItem(id = 2, titre = "High Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.95f)),
            CollectionItem(id = 3, titre = "Medium Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.5f)),
            CollectionItem(id = 4, titre = "Very High Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.99f)),
            CollectionItem(id = 5, titre = "Another Low Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.2f))
        )
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(allItemsWithEmbeddings)

        // WHEN: We call the function to find the most similar items.
        val results = repository.findMostSimilarItems(queryEmbedding)

        // THEN: The result should contain exactly 3 items, sorted by similarity descending.
        assertEquals(3, results.size)
        assertEquals("Very High Similarity", results[0].item.titre) // Similarity 0.99
        assertEquals("High Similarity", results[1].item.titre)    // Similarity 0.95
        assertEquals("Medium Similarity", results[2].item.titre)   // Similarity 0.5
    }
}
