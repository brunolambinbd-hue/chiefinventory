package com.example.parabdcollector.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.model.DisplayLocation
import com.example.parabdcollector.ui.viewmodel.LocationViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for [LocationViewModel].
 */
class LocationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: LocationViewModel

    private val allLocationsLiveData = MutableLiveData<List<Location>>()
    private val observer = Observer<List<DisplayLocation>> { }

    @Before
    fun setup() {
        locationRepository = mock()
        whenever(locationRepository.getAll()).thenReturn(allLocationsLiveData)
        viewModel = LocationViewModel(locationRepository)
        viewModel.visibleLocations.observeForever(observer)
    }

    @After
    fun tearDown() {
        viewModel.visibleLocations.removeObserver(observer)
    }

    @Test
    fun `visibleLocations should be empty when no data is provided`() {
        allLocationsLiveData.value = emptyList()
        val visibleLocations = viewModel.visibleLocations.value
        assertEquals(0, visibleLocations?.size)
    }

    /**
     * Teste que l'insertion d'un emplacement racine le rend immédiatement visible.
     */
    @Test
    fun `insert root location should be visible immediately`() {
        // GIVEN: Liste initiale vide
        allLocationsLiveData.value = emptyList()

        // WHEN: On simule l'insertion d'un nouvel emplacement racine dans la DB
        val newLocation = Location(id = 10, name = "New Root", parentId = null)
        allLocationsLiveData.value = listOf(newLocation)

        // THEN: Il doit apparaître dans visibleLocations
        val visible = viewModel.visibleLocations.value
        assertTrue("Le nouvel emplacement racine devrait être visible", visible?.any { it.location.id == 10L } == true)
    }

    /**
     * Teste que l'insertion d'un sous-emplacement le rend immédiatement visible (mode simplifié).
     */
    @Test
    fun `insert sub-location should be visible immediately even without expansion`() {
        // GIVEN: Un parent présent
        val parent = Location(id = 1, name = "Parent", parentId = null)
        allLocationsLiveData.value = listOf(parent)

        // WHEN: On insère un enfant (sans avoir appelé d'expansion explicitement)
        val child = Location(id = 2, name = "Child", parentId = 1)
        allLocationsLiveData.value = listOf(parent, child)

        // THEN: L'enfant doit être visible immédiatement car tout est affiché par défaut
        val visible = viewModel.visibleLocations.value
        assertEquals(2, visible?.size)
        assertTrue("Le sous-emplacement devrait être visible immédiatement", 
            visible?.any { it.location.id == 2L } == true)
    }

    /**
     * Teste que toggleExpansion n'impacte plus la visibilité dans ce mode simplifié.
     */
    @Test
    fun `toggleExpansion should not hide items in simplified always-visible mode`() {
        val locations = listOf(
            Location(id = 1, name = "Parent", parentId = null),
            Location(id = 2, name = "Child", parentId = 1)
        )
        allLocationsLiveData.value = locations
        
        viewModel.toggleExpansion(1L)
        assertEquals(2, viewModel.visibleLocations.value?.size)

        viewModel.toggleExpansion(1L)
        // La taille reste à 2 car on affiche tout, tout le temps
        assertEquals(2, viewModel.visibleLocations.value?.size)
    }

    @Test
    fun `expandAll_shouldMakeAllNodesVisible`() {
        val locations = listOf(
            Location(id = 1, name = "Parent 1", parentId = null),
            Location(id = 2, name = "Child 1.1", parentId = 1),
            Location(id = 3, name = "Parent 2", parentId = null),
            Location(id = 4, name = "Child 2.1", parentId = 3),
            Location(id = 5, name = "Grandchild 2.1.1", parentId = 4)
        )
        allLocationsLiveData.value = locations
        viewModel.expandAll()
        val visible = viewModel.visibleLocations.value
        assertEquals(5, visible?.size)
    }
}
