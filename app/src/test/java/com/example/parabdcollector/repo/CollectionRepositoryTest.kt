package com.example.parabdcollector.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CollectionItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [CollectionRepository].
 *
 * This class uses Mockito to create a mock [CollectionDao] to test the repository's logic
 * in isolation from the actual database.
 */
class CollectionRepositoryTest {

    /**
     * This rule makes sure that LiveData updates happen synchronously in tests.
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // The mock DAO that will be used in the tests.
    private lateinit var collectionDao: CollectionDao
    // The repository instance under test.
    private lateinit var collectionRepository: CollectionRepository

    /**
     * Sets up the test environment before each test.
     * This creates a new mock DAO and a new repository instance.
     */
    @Before
    fun setup() {
        collectionDao = mock()
        collectionRepository = CollectionRepository(collectionDao)
    }

    /**
     * Verifies that [CollectionRepository.getAllPossessed] correctly calls the DAO
     * and returns the expected data.
     */
    @Test
    fun `getAllPossessed should return possessed items from dao`() {
        // GIVEN: A LiveData object with a test item.
        val testData = listOf(CollectionItem(id = 1, titre = "Test Item", isPossessed = true, editeur = null, annee = null, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, description = "", imageUri = null, imageEmbedding = null, locationId = null, remoteId = null))
        val liveData = MutableLiveData(testData)
        whenever(collectionDao.getAllPossessed()).thenReturn(liveData)

        // WHEN: The method is called on the repository.
        val result = collectionRepository.getAllPossessed()

        // THEN: The result should be the LiveData provided by the DAO.
        assertEquals(testData, result.value)
    }

    /**
     * Verifies that [CollectionRepository.getAllSought] correctly calls the DAO
     * and returns the expected data.
     */
    @Test
    fun `getAllSought should return sought items from dao`() {
        // GIVEN: A LiveData object with a test item.
        val testData = listOf(CollectionItem(id = 2, titre = "Sought Item", isPossessed = false, editeur = null, annee = null, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, description = "", imageUri = null, imageEmbedding = null, locationId = null, remoteId = null))
        val liveData = MutableLiveData(testData)
        whenever(collectionDao.getAllSought()).thenReturn(liveData)

        // WHEN: The method is called on the repository.
        val result = collectionRepository.getAllSought()

        // THEN: The result should be the LiveData provided by the DAO.
        assertEquals(testData, result.value)
    }

    /**
     * Verifies that calling [CollectionRepository.insert] correctly calls the
     * corresponding suspend method on the DAO.
     */
    @Test
    fun `insert should call insert on dao`() = runBlocking {
        // GIVEN: A collection item to insert.
        val item = CollectionItem(id = 3, titre = "New Item", isPossessed = true, editeur = null, annee = null, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, description = "", imageUri = null, imageEmbedding = null, locationId = null, remoteId = null)

        // WHEN: The insert method is called on the repository.
        collectionRepository.insert(item)

        // THEN: The insert method on the DAO should be called with the same item.
        verify(collectionDao).insert(item)
    }
}
