package com.example.chiefinventory.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.model.DisplayLocation
import com.example.chiefinventory.ui.viewmodel.LocationViewModel
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
        allLocationsLiveData.value = emptyList()
        val newLocation = Location(id = 10, name = "New Root", parentId = null)
        allLocationsLiveData.value = listOf(newLocation)

        val visible = viewModel.visibleLocations.value
        assertTrue("Le nouvel emplacement racine devrait être visible", visible?.any { it.location.id == 10L } == true)
    }

    /**
     * Teste que l'insertion d'un sous-emplacement est visible sans expansion (mode simplifié).
     */
    @Test
    fun `insert sub-location should be visible immediately even without expansion`() {
        val parent = Location(id = 1, name = "Parent", parentId = null)
        val child = Location(id = 2, name = "Child", parentId = 1)
        
        // On simule l'ajout successif
        allLocationsLiveData.value = listOf(parent)
        allLocationsLiveData.value = listOf(parent, child)

        val visible = viewModel.visibleLocations.value
        assertEquals(2, visible?.size)
        assertTrue("Le sous-emplacement doit être visible immédiatement", visible?.any { it.location.id == 2L } == true)
    }

    /**
     * Vérifie que le mode simplifié conserve la visibilité après un toggle.
     */
    @Test
    fun `toggleExpansion should not hide items in simplified mode`() {
        val locations = listOf(
            Location(id = 1, name = "Parent", parentId = null),
            Location(id = 2, name = "Child", parentId = 1)
        )
        allLocationsLiveData.value = locations
        
        // On simule un repli (collapse)
        viewModel.toggleExpansion(1L)
        
        // En mode simplifié, l'enfant doit RESTER visible
        val visible = viewModel.visibleLocations.value
        assertEquals("L'enfant doit rester visible même si on clique sur le parent", 2, visible?.size)
    }

    @Test
    fun expandAll_shouldShowAllNodes() {
        val locations = listOf(
            Location(id = 1, name = "Parent 1", parentId = null),
            Location(id = 2, name = "Child 1.1", parentId = 1),
            Location(id = 3, name = "Parent 2", parentId = null)
        )
        allLocationsLiveData.value = locations
        viewModel.expandAll()
        val visible = viewModel.visibleLocations.value
        assertEquals(3, visible?.size)
    }
}
