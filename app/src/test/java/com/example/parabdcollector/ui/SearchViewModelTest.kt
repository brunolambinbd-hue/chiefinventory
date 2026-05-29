package com.example.parabdcollector.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.AdvancedSearchResult
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.ui.viewmodel.SearchResultState
import com.example.parabdcollector.ui.viewmodel.SearchViewModel
import com.example.parabdcollector.util.MainDispatcherRule
import com.example.parabdcollector.utils.IImageProcessor
import com.example.parabdcollector.utils.ITextRecognizer
import com.google.mediapipe.tasks.components.containers.Embedding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Unit tests for the [com.example.parabdcollector.ui.viewmodel.SearchViewModel].
 */
@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var imageEmbedderHelper: ImageEmbedderHelper
    private lateinit var textRecognizer: ITextRecognizer
    private lateinit var imageProcessor: IImageProcessor
    private lateinit var application: Application
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        // Mock des dépendances
        repository = mock()
        imageEmbedderHelper = mock()
        textRecognizer = mock()
        imageProcessor = mock()
        application = mock() // AndroidViewModel a besoin d'une Application
        // Configure the mock to return a dummy string for any string resource
        whenever(application.getString(any())).thenReturn("dummy error message")

        // Création du ViewModel avec les mocks et le dispatcher de test
        viewModel = SearchViewModel(
            application = application,
            repository = repository,
            imageEmbedderHelper = imageEmbedderHelper,
            dispatcher = mainDispatcherRule.testDispatcher,
            textRecognizer = textRecognizer,
            imageProcessor = imageProcessor
        )
    }

    @Test
    fun `search with simple query should call repository and update results`(): Unit = runTest {
        // GIVEN : Le repository est programmé pour retourner une liste de résultats réalistes.
        val query = "test"
        val mockResults = listOf(createTestItem(1, "Test Item"))
        whenever(repository.search(query)).thenReturn(mockResults)

        // WHEN : La fonction de recherche est appelée.
        viewModel.search(query)

        // THEN : Le LiveData des résultats doit être mis à jour avec les données du repository.
        val expectedSearchResults = mockResults.map { SearchResultItem(it) }
        val state = viewModel.searchResultState.value
        assertTrue(state is SearchResultState.Success)
        assertEquals(expectedSearchResults, (state as SearchResultState.Success).results)
    }

    @Test
    fun `search should correctly handle possessed items`(): Unit = runTest {
        // GIVEN : Un résultat de recherche contenant un objet possédé
        val query = "possessed"
        val possessedItem = createTestItem(1, "Possessed Item")
        whenever(repository.search(query)).thenReturn(listOf(possessedItem))

        // WHEN : Recherche
        viewModel.search(query)

        // THEN : L'état Success doit contenir l'objet marqué comme possédé
        val state = viewModel.searchResultState.value as SearchResultState.Success
        assertTrue("Item should be possessed", state.results[0].item.isPossessed)
    }

    @Test
    fun `search should correctly handle sought items`(): Unit = runTest {
        // GIVEN : Un résultat de recherche contenant un objet recherché (non possédé)
        val query = "sought"
        val soughtItem = createTestItem(2, "Sought Item", isPossessed = false)
        whenever(repository.search(query)).thenReturn(listOf(soughtItem))

        // WHEN : Recherche
        viewModel.search(query)

        // THEN : L'état Success doit contenir l'objet marqué comme non possédé
        val state = viewModel.searchResultState.value as SearchResultState.Success
        assertTrue("Item should not be possessed", !state.results[0].item.isPossessed)
    }

    @Test
    fun `advancedSearch without image should call repository and update results`(): Unit = runTest {
        // GIVEN : Des critères de recherche et des résultats mockés réalistes.
        val criteria = SearchCriteria(titre = "Advanced")
        val mockList = listOf(SearchResultItem(createTestItem(2, "Advanced Result")))
        val mockResult = AdvancedSearchResult(mockList, mockList.size)
        whenever(repository.advancedSearch(any(), anyOrNull(), any())).thenReturn(mockResult)

        // WHEN : La recherche avancée est appelée sans bitmap.
        viewModel.advancedSearch(criteria, null)

        // THEN : Le LiveData des résultats doit être mis à jour.
        val state = viewModel.searchResultState.value
        assertTrue(state is SearchResultState.Success)
        assertEquals(mockList, (state as SearchResultState.Success).results)
        assertEquals(mockList.size, state.totalCount)
        // On vérifie que la bonne méthode du repository a été appelée.
        verify(repository).advancedSearch(any(), anyOrNull(), any())
    }

    @Test
    fun `advancedSearch with fallback should trigger second chance with enhanced contrast`(): Unit = runTest {
        // GIVEN
        val criteria = SearchCriteria()
        val mockBitmap = mock<Bitmap>()
        val mockEnhancedBitmap = mock<Bitmap>()
        
        val mockEmbedding = mock<Embedding>()
        val floatArray = floatArrayOf(0.1f)
        whenever(mockEmbedding.floatEmbedding()).thenReturn(floatArray)
        
        // Mock the embedder
        whenever(imageEmbedderHelper.computeSignature(any())).thenReturn(mockEmbedding)
        
        // Mock image processor
        whenever(imageProcessor.enhanceContrast(mockBitmap)).thenReturn(mockEnhancedBitmap)
        
        // Mock OCR to return empty list (avoiding native calls)
        whenever(textRecognizer.extractText(any())).thenReturn(emptyList())

        // First attempt returns fallback
        val fallbackResult = AdvancedSearchResult(emptyList(), 0, isFallback = true)
        // Second attempt returns success
        val successItem = SearchResultItem(createTestItem(1, "Success Item"))
        val successResult = AdvancedSearchResult(listOf(successItem), 1, isFallback = false)
        
        // Setup sequential answers for repository
        whenever(repository.advancedSearch(any(), anyOrNull(), any()))
            .thenReturn(fallbackResult) // First call
            .thenReturn(successResult)  // Second call (after contrast enhancement)

        // WHEN
        viewModel.advancedSearch(criteria, mockBitmap)
        
        // Wait for all coroutines to complete
        testScheduler.advanceUntilIdle()

        // THEN
        val state = viewModel.searchResultState.value
        assertTrue("State should be Success, was $state", state is SearchResultState.Success)
        if (state is SearchResultState.Success) {
            assertEquals("Success Item", state.results[0].item.titre)
            // Verify repository was called twice
            verify(repository, times(2)).advancedSearch(any(), anyOrNull(), any())
            // Verify image enhancement was called
            verify(imageProcessor).enhanceContrast(mockBitmap)
        }
    }

    @Test
    fun `clearSearchResults should set state to Idle and clear preview`() {
        // GIVEN : Le ViewModel est dans un état quelconque.

        // WHEN : La fonction de nettoyage est appelée.
        viewModel.clearSearchResults()

        // THEN : Les LiveData des résultats et de la prévisualisation doivent être vides.
        val state = viewModel.searchResultState.value
        assertTrue("State should be Idle after clearing", state is SearchResultState.Idle)
        assertTrue("Signature preview should be empty", viewModel.signaturePreview.value?.isEmpty() ?: true)
    }

    private fun createTestItem(id: Long, titre: String, isPossessed: Boolean = true): CollectionItem {
        return CollectionItem(
            id = id,
            remoteId = 1000 + id.toInt(),
            titre = titre,
            editeur = "Éditions Dupuis",
            annee = 2024,
            mois = 6,
            categorie = "Albums",
            superCategorie = "Bandes Dessinées",
            materiau = "Papier",
            tirage = "Édition de luxe limitée",
            dimensions = "24 x 32 cm",
            prixAchat = 29.90,
            valeurEstimee = 45.00,
            lieuAchat = "Librairie spécialisée",
            description = "Un exemplaire de test très détaillé pour valider le comportement du SearchViewModel.",
            imageUri = "https://example.com/images/item_$id.jpg",
            locationId = if (id % 2 == 0L) 10L else null,
            isPossessed = isPossessed
        )
    }
}
