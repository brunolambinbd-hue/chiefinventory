package com.example.chiefinventory.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.model.CollectionItem
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
        val queryEmbedding = FloatArray(10) { 0f }.apply { this[0] = 1f }

        val allItemsWithEmbeddings = listOf(
            CollectionItem(id = 1, titre = "Low Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.1f)),
            CollectionItem(id = 2, titre = "High Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.95f)),
            CollectionItem(id = 3, titre = "Medium Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.5f)),
            CollectionItem(id = 4, titre = "Very High Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.99f)),
            CollectionItem(id = 5, titre = "Another Low Similarity", editeur = "Dupuis", annee = 2023, imageEmbedding = createPredictableEmbedding(0.2f))
        )
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(allItemsWithEmbeddings)

        val results = repository.findMostSimilarItems(queryEmbedding)

        assertThat(results).hasSize(3)
        assertThat(results[0].item.titre).isEqualTo("Very High Similarity")
        assertThat(results[1].item.titre).isEqualTo("High Similarity")
        assertThat(results[2].item.titre).isEqualTo("Medium Similarity")
    }
}
