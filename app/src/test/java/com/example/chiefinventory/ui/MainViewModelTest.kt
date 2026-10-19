package com.example.chiefinventory.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SignatureStats
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.ui.viewmodel.MainViewModel
import com.example.chiefinventory.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var ingredientRepository: IngredientRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        repository = mock()
        ingredientRepository = mock()
    }

    @Test
    fun `possessedItems LiveData exposes data from repository`() {
        val testData = listOf(createMockItem(1, true))
        whenever(repository.getAllPossessed()).thenReturn(MutableLiveData(testData))

        viewModel = MainViewModel(repository, ingredientRepository)
        assertThat(viewModel.possessedItems.value).isEqualTo(testData)
    }

    @Test
    fun `soughtItems LiveData exposes data from repository`() {
        val testData = listOf(createMockItem(2, false))
        whenever(repository.getAllSought()).thenReturn(MutableLiveData(testData))

        viewModel = MainViewModel(repository, ingredientRepository)
        assertThat(viewModel.soughtItems.value).isEqualTo(testData)
    }

    @Test
    fun `totalItemsCount LiveData exposes data from repository`() {
        whenever(repository.getTotalCount()).thenReturn(MutableLiveData(42))

        viewModel = MainViewModel(repository, ingredientRepository)
        assertThat(viewModel.totalItemsCount.value).isEqualTo(42)
    }

    @Test
    fun `signatureStats LiveData exposes data from repository`() {
        val stats = SignatureStats(totalCount = 10, validCount = 5, emptyCount = 2, missingCount = 3)
        whenever(repository.getSignatureStats()).thenReturn(MutableLiveData(stats))

        viewModel = MainViewModel(repository, ingredientRepository)
        assertThat(viewModel.signatureStats.value).isEqualTo(stats)
    }

    @Test
    fun `recentPossessedItems LiveData exposes data from repository`() {
        val testData = listOf(createMockItem(3, true))
        whenever(repository.getRecentPossessed()).thenReturn(MutableLiveData(testData))

        viewModel = MainViewModel(repository, ingredientRepository)
        assertThat(viewModel.recentPossessedItems.value).isEqualTo(testData)
    }

    @Test
    fun `recentLocatedItems LiveData exposes data from repository`() {
        val testData = listOf(createMockItem(4, true).copy(locationId = 100L))
        whenever(repository.getRecentLocated()).thenReturn(MutableLiveData(testData))

        viewModel = MainViewModel(repository, ingredientRepository)
        assertThat(viewModel.recentLocatedItems.value).isEqualTo(testData)
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
