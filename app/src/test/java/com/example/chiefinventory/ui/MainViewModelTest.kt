package com.example.chiefinventory.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SignatureStats
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.ui.viewmodel.MainViewModel
import com.example.chiefinventory.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [com.example.chiefinventory.ui.viewmodel.MainViewModel].
 */
@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        repository = mock()
    }

    @Test
    fun `possessedItems LiveData exposes data from repository`() {
        val testData = listOf(createMockItem(1, true))
        whenever(repository.getAllPossessed()).thenReturn(MutableLiveData(testData))

        viewModel = MainViewModel(repository)
        assertEquals(testData, viewModel.possessedItems.value)
    }

    @Test
    fun `soughtItems LiveData exposes data from repository`() {
        val testData = listOf(createMockItem(2, false))
        whenever(repository.getAllSought()).thenReturn(MutableLiveData(testData))

        viewModel = MainViewModel(repository)
        assertEquals(testData, viewModel.soughtItems.value)
    }

    @Test
    fun `totalItemsCount LiveData exposes data from repository`() {
        whenever(repository.getTotalCount()).thenReturn(MutableLiveData(42))

        viewModel = MainViewModel(repository)
        assertEquals(42, viewModel.totalItemsCount.value)
    }

    @Test
    fun `signatureStats LiveData exposes data from repository`() {
        val stats = SignatureStats(totalCount = 10, validCount = 5, emptyCount = 2, missingCount = 3)
        whenever(repository.getSignatureStats()).thenReturn(MutableLiveData(stats))

        viewModel = MainViewModel(repository)
        assertEquals(stats, viewModel.signatureStats.value)
    }

    @Test
    fun `recentPossessedItems LiveData exposes data from repository`() {
        val testData = listOf(createMockItem(3, true))
        whenever(repository.getRecentPossessed()).thenReturn(MutableLiveData(testData))

        viewModel = MainViewModel(repository)
        assertEquals(testData, viewModel.recentPossessedItems.value)
    }

    @Test
    fun `recentLocatedItems LiveData exposes data from repository`() {
        val testData = listOf(createMockItem(4, true).copy(locationId = 100L))
        whenever(repository.getRecentLocated()).thenReturn(MutableLiveData(testData))

        viewModel = MainViewModel(repository)
        assertEquals(testData, viewModel.recentLocatedItems.value)
    }

    private fun createMockItem(id: Long, possessed: Boolean): CollectionItem {
        return CollectionItem(
            id = id,
            titre = "Bande Dessinée n°$id",
            isPossessed = possessed,
            editeur = "Dupuis",
            annee = 2023,
            mois = 5,
            categorie = "Albums",
            superCategorie = "Bandes Dessinées",
            materiau = "Papier",
            tirage = "Édition originale",
            dimensions = "22 x 30 cm",
            prixAchat = 15.0,
            valeurEstimee = 20.0,
            lieuAchat = "Librairie du Centre",
            description = "Un exemplaire de test pour le MainViewModel."
        )
    }
}
