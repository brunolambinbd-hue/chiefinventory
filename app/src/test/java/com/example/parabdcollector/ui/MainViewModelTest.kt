package com.example.parabdcollector.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [MainViewModel].
 *
 * These tests verify that the ViewModel correctly exposes data from the [CollectionRepository].
 */
@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        repository = mock()
    }

    @Test
    fun `possessedItems LiveData exposes data from repository`() {
        // GIVEN: The repository is programmed to return a specific list of possessed items.
        val testData = listOf(CollectionItem(id = 1, titre = "Item 1", isPossessed = true, description = null, editeur = null, annee = null, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, imageUri = null, imageEmbedding = null, locationId = null, remoteId = null))
        val liveData = MutableLiveData(testData)
        whenever(repository.getAllPossessed()).thenReturn(liveData)

        // WHEN: The ViewModel is created.
        viewModel = MainViewModel(repository)
        val result = viewModel.possessedItems.value

        // THEN: The value of the ViewModel's LiveData should match the repository's data.
        assertEquals(testData, result)
    }

    @Test
    fun `soughtItems LiveData exposes data from repository`() {
        // GIVEN: The repository is programmed to return a specific list of sought items.
        val testData = listOf(CollectionItem(id = 2, titre = "Item 2", isPossessed = false, description = null, editeur = null, annee = null, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, imageUri = null, imageEmbedding = null, locationId = null, remoteId = null))
        val liveData = MutableLiveData(testData)
        whenever(repository.getAllSought()).thenReturn(liveData)

        // WHEN: The ViewModel is created.
        viewModel = MainViewModel(repository)
        val result = viewModel.soughtItems.value

        // THEN: The value of the ViewModel's LiveData should match the repository's data.
        assertEquals(testData, result)
    }

    @Test
    fun `totalItemsCount LiveData exposes data from repository`() {
        // GIVEN: The repository is programmed to return a specific total count.
        val liveData = MutableLiveData(42)
        whenever(repository.getTotalCount()).thenReturn(liveData)

        // WHEN: The ViewModel is created.
        viewModel = MainViewModel(repository)
        val result = viewModel.totalItemsCount.value

        // THEN: The value should match the repository's data.
        assertEquals(42, result)
    }

    @Test
    fun `signatureStats LiveData exposes data from repository`() {
        // GIVEN: The repository is programmed to return specific signature stats.
        val stats = SignatureStats(totalCount = 10, validCount = 5, emptyCount = 2, missingCount = 3)
        val liveData = MutableLiveData(stats)
        whenever(repository.getSignatureStats()).thenReturn(liveData)

        // WHEN: The ViewModel is created.
        viewModel = MainViewModel(repository)
        val result = viewModel.signatureStats.value

        // THEN: The value should match the repository's data.
        assertEquals(stats, result)
    }
}
