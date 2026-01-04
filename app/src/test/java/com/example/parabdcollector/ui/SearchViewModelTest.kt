package com.example.parabdcollector.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.parabdcollector.model.AdvancedSearchResult
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.ui.viewmodel.SearchResultState
import com.example.parabdcollector.ui.viewmodel.SearchViewModel
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [com.example.parabdcollector.ui.viewmodel.SearchViewModel].
 */
@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var application: Application
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        // Mock des dépendances
        repository = mock()
        application = mock() // AndroidViewModel a besoin d'une Application
        // Configure the mock to return a dummy string for any string resource
        whenever(application.getString(any())).thenReturn("dummy error message")

        // Création du ViewModel avec les mocks
        viewModel = SearchViewModel(application, repository)
    }

    @Test
    fun `search with simple query should call repository and update results`() = runTest {
        // GIVEN: Le repository est programmé pour retourner une liste de résultats.
        val query = "test"
        val mockResults = listOf(CollectionItem(id = 1, titre = "Test Item", isPossessed = true, description = "", editeur = null, annee = null, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, imageUri = null, imageEmbedding = null, locationId = null, remoteId = null))
        whenever(repository.search(query)).thenReturn(mockResults)

        // WHEN: La fonction de recherche est appelée.
        viewModel.search(query)

        // THEN: Le LiveData des résultats doit être mis à jour avec les données du repository.
        val expectedSearchResults = mockResults.map { SearchResultItem(it) }
        val state = viewModel.searchResultState.value
        assertTrue(state is SearchResultState.Success)
        assertEquals(expectedSearchResults, (state as SearchResultState.Success).results)
    }

    @Test
    fun `advancedSearch without image should call repository and update results`() = runTest {
        // GIVEN: Des critères de recherche et des résultats mockés.
        val criteria = SearchCriteria(titre = "Advanced")
        val mockList = listOf(SearchResultItem(CollectionItem(id = 2, titre = "Advanced Result", isPossessed = true, description = "", editeur = null, annee = null, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, imageUri = null, imageEmbedding = null, locationId = null, remoteId = null)))
        val mockResult = AdvancedSearchResult(mockList, mockList.size)
        whenever(repository.advancedSearch(any(), anyOrNull())).thenReturn(mockResult)

        // WHEN: La recherche avancée est appelée sans bitmap.
        viewModel.advancedSearch(criteria, null)

        // THEN: Le LiveData des résultats doit être mis à jour.
        val state = viewModel.searchResultState.value
        assertTrue(state is SearchResultState.Success)
        assertEquals(mockList, (state as SearchResultState.Success).results)
        assertEquals(mockList.size, state.totalCount)
        // On vérifie que la bonne méthode du repository a été appelée.
        verify(repository).advancedSearch(criteria, null)
    }

    @Test
    fun `clearSearchResults should set state to Idle and clear preview`() {
        // GIVEN: Le ViewModel est dans un état quelconque.

        // WHEN: La fonction de nettoyage est appelée.
        viewModel.clearSearchResults()

        // THEN: Les LiveData des résultats et de la prévisualisation doivent être vides.
        val state = viewModel.searchResultState.value
        assertTrue("State should be Idle after clearing", state is SearchResultState.Idle)
        assertTrue("Signature preview should be empty", viewModel.signaturePreview.value?.isEmpty() ?: true)
    }
}
