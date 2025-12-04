package com.example.parabdcollector.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CollectionItem
import com.example.imagecomparison.EmbeddingUtils
import com.google.mediapipe.tasks.components.containers.Embedding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [CollectionRepository].
 */
@ExperimentalCoroutinesApi
class CollectionRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var collectionDao: CollectionDao
    private lateinit var repository: CollectionRepository

    @Before
    fun setup() {
        collectionDao = mock()
        repository = CollectionRepository(collectionDao)
    }

    // Helper function to create a dummy embedding ByteArray from a simple float.
    private fun createDummyEmbedding(value: Float): ByteArray {
        val floatArray = FloatArray(10) { value } // Dummy array
        val embedding = Embedding.create(floatArray, 0)
        return EmbeddingUtils.embeddingToByteArray(embedding)
    }

    @Test
    fun `findMostSimilarItems should return top 3 sorted results`() = runTest {
        // GIVEN: A query embedding and a list of items in the DAO with varying similarity.
        val queryEmbedding = FloatArray(10) { 0.9f } // The vector we are searching for

        val allItemsWithEmbeddings = listOf(
            CollectionItem(id = 1, titre = "Low Similarity", imageEmbedding = createDummyEmbedding(0.1f), isPossessed = true, description = "", editeur = "", annee = 2023, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "", prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", locationId = null, remoteId = null),
            CollectionItem(id = 2, titre = "High Similarity", imageEmbedding = createDummyEmbedding(0.95f), isPossessed = true, description = "", editeur = "", annee = 2023, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "", prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", locationId = null, remoteId = null), // Most similar
            CollectionItem(id = 3, titre = "Medium Similarity", imageEmbedding = createDummyEmbedding(0.5f), isPossessed = true, description = "", editeur = "", annee = 2023, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "", prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", locationId = null, remoteId = null),
            CollectionItem(id = 4, titre = "Very High Similarity", imageEmbedding = createDummyEmbedding(0.99f), isPossessed = true, description = "", editeur = "", annee = 2023, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "", prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", locationId = null, remoteId = null), // Should be first
            CollectionItem(id = 5, titre = "Another Low Similarity", imageEmbedding = createDummyEmbedding(0.2f), isPossessed = true, description = "", editeur = "", annee = 2023, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "", prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", locationId = null, remoteId = null)
        )
        whenever(collectionDao.getAllItemsWithEmbeddings()).thenReturn(allItemsWithEmbeddings)

        // WHEN: We call the function to find the most similar items.
        val results = repository.findMostSimilarItems(queryEmbedding)

        // THEN: The result should contain exactly 3 items, sorted by similarity descending.
        assertEquals(3, results.size)
        assertEquals("Very High Similarity", results[0].item.titre) // ID 4
        assertEquals("High Similarity", results[1].item.titre)    // ID 2
        assertEquals("Medium Similarity", results[2].item.titre)   // ID 3
    }
}
