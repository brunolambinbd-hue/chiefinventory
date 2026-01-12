package com.example.parabdcollector.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        whenever(locationRepository.getAll()).thenReturn(MutableLiveData(emptyList()))
        
        viewModel = BatchPossessionViewModel(repository, locationRepository)
    }

    @Test
    fun `analyzeSeries should correctly identify range size`() = runTest {
        val item = createMockItem(1, "Spirou n°1500", false)
        whenever(repository.getAllByTitle(any())).thenReturn(listOf(item))

        viewModel.analyzeSeries("Spirou", 1500, 1600)
        advanceUntilIdle() // Attendre la fin de l'analyse

        val result = viewModel.analysisResult.value
        assertEquals(101, result?.rangeSize)
        assertEquals(1, result?.foundCount)
    }

    @Test
    fun `applyUpdate should update items with possessed status and selected location`() = runTest {
        // GIVEN: Un objet identifié
        val item = createMockItem(1, "Spirou n°1500", false)
        whenever(repository.getAllByTitle(any())).thenReturn(listOf(item))
        
        viewModel.analyzeSeries("Spirou", 1500, 1500)
        advanceUntilIdle() // INDISPENSABLE : attendre que l'objet soit ajouté à la liste interne
        
        val targetLocationId = 100L
        viewModel.selectedLocationId = targetLocationId

        // WHEN: Application de la mise à jour
        viewModel.applyUpdate()
        advanceUntilIdle() // Attendre la fin de la mise à jour

        // THEN: Le repository doit avoir été appelé
        verify(repository).update(argThat { 
            this.id == 1L && this.isPossessed && this.locationId == targetLocationId 
        })
        assertEquals(1, viewModel.updateStatus.value)
    }

    private fun createMockItem(id: Long, title: String, possessed: Boolean): CollectionItem {
        return CollectionItem(
            id = id, titre = title, isPossessed = possessed,
            editeur = "Dupuis", annee = 1980, mois = 1, categorie = "Spirou", superCategorie = "Magazines"
        )
    }
}
