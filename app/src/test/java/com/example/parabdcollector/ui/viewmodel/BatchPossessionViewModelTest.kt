package com.example.parabdcollector.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class BatchPossessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: BatchPossessionViewModel

    @Before
    fun setup() {
        repository = mock()
        locationRepository = mock()
        // Mock default locations to prevent null pointer
        whenever(locationRepository.getAll()).thenReturn(MutableLiveData(emptyList()))
        
        viewModel = BatchPossessionViewModel(repository, locationRepository)
    }

    @Test
    fun `analyzeSeries should correctly identify range size`() = runTest {
        // GIVEN: Repository returns one matching item
        val item = createMockItem(1, "Spirou n°1500", false)
        whenever(repository.getAllByTitle(any())).thenReturn(listOf(item))

        // WHEN: Analyzing 1500 to 1600
        viewModel.analyzeSeries("Spirou", 1500, 1600)

        // THEN: Range size should be 101
        val result = viewModel.analysisResult.value
        assertEquals(101, result?.rangeSize)
        assertEquals(1, result?.foundCount)
    }

    @Test
    fun `applyUpdate should update items with possessed status and selected location`() = runTest {
        // GIVEN: One item identified and a location selected
        val item = createMockItem(1, "Spirou n°1500", false)
        whenever(repository.getAllByTitle(any())).thenReturn(listOf(item))
        viewModel.analyzeSeries("Spirou", 1500, 1500)
        
        val targetLocationId = 100L
        viewModel.selectedLocationId = targetLocationId

        // WHEN: Applying update
        viewModel.applyUpdate()

        // THEN: Repository should receive update with possessed=true AND correct locationId
        verify(repository).update(argThat { 
            this.id == 1L && this.isPossessed && this.locationId == targetLocationId 
        })
        assertEquals(1, viewModel.updateStatus.value)
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
