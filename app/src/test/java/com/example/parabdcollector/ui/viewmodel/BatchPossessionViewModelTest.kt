package com.example.parabdcollector.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class BatchPossessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var viewModel: BatchPossessionViewModel

    @Before
    fun setup() {
        repository = mock()
        viewModel = BatchPossessionViewModel(repository)
    }

    @Test
    fun `analyzeSeries should correctly identify and filter items by number range`() = runTest {
        // GIVEN: A list of items with various titles and possession statuses
        val seriesName = "Spirou"
        val mockItems = listOf(
            createMockItem(1, "Spirou n°1500", false), // In range
            createMockItem(2, "Spirou n°1550", false), // In range
            createMockItem(3, "Spirou 1600", false),   // In range (different format)
            createMockItem(4, "Spirou n°1499", false), // Out of range (too low)
            createMockItem(5, "Spirou n°1601", false), // Out of range (too high)
            createMockItem(6, "Spirou n°1525", true),  // In range but ALREADY possessed
            createMockItem(7, "Autre Titre 1550", false) // Wrong series name
        )
        whenever(repository.getAllByTitle(any())).thenReturn(mockItems)

        // WHEN: Analyzing the series from 1500 to 1600
        viewModel.analyzeSeries(seriesName, 1500, 1600)

        // THEN: It should find exactly 3 items
        val result = viewModel.analysisResult.value
        assertNotNull(result)
        assertEquals(3, result?.foundCount)
        assertEquals(101, result?.rangeSize) // 1600 - 1500 + 1
    }

    @Test
    fun `analyzeSeries with empty results from repository should return 0 found`() = runTest {
        whenever(repository.getAllByTitle(any())).thenReturn(emptyList())

        viewModel.analyzeSeries("Spirou", 1, 100)

        val result = viewModel.analysisResult.value
        assertEquals(0, result?.foundCount)
    }

    private fun createMockItem(id: Long, title: String, possessed: Boolean): CollectionItem {
        return CollectionItem(
            id = id,
            titre = title,
            isPossessed = possessed,
            editeur = "Dupuis",
            annee = 1980,
            mois = 1,
            categorie = "Spirou",
            superCategorie = "Magazines",
            materiau = null, tirage = null, dimensions = null, prixAchat = null,
            valeurEstimee = null, lieuAchat = null, description = null, imageUri = null,
            imageEmbedding = null, locationId = null, remoteId = null
        )
    }
}
