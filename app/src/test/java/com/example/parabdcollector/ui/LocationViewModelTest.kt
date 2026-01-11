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
     * Teste que l'insertion d'un sous-emplacement sous un parent déplié le rend immédiatement visible.
     */
    @Test
    fun `insert sub-location under expanded parent should be visible immediately`() {
        // GIVEN: Un parent déplié
        val parent = Location(id = 1, name = "Parent", parentId = null)
        allLocationsLiveData.value = listOf(parent)
        viewModel.toggleExpansion(1L) // Déplier

        // WHEN: On insère un enfant sous ce parent
        val child = Location(id = 2, name = "Child", parentId = 1)
        allLocationsLiveData.value = listOf(parent, child)

        // THEN: L'enfant doit être visible immédiatement
        val visible = viewModel.visibleLocations.value
        assertTrue("Le sous-emplacement devrait être visible car son parent est déplié", 
            visible?.any { it.location.id == 2L } == true)
    }

    @Test
    fun `toggleExpansion should make child visible and then hide it`() {
        val locations = listOf(
            Location(id = 1, name = "Parent", parentId = null),
            Location(id = 2, name = "Child", parentId = 1)
        )
        allLocationsLiveData.value = locations
        viewModel.toggleExpansion(1L) // Expand
        viewModel.toggleExpansion(1L) // Then collapse

        viewModel.toggleExpansion(1L)
        var visible = viewModel.visibleLocations.value
        assertEquals(2, visible?.size)
        assertEquals(2L, visible?.get(1)?.location?.id)

        viewModel.toggleExpansion(1L)
        visible = viewModel.visibleLocations.value
        assertEquals(1, visible?.size)
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
        val visibleIds = visible?.map { it.location.id }?.toSet()
        val expectedIds = setOf(1L, 2L, 3L, 4L, 5L)
        assertEquals(expectedIds, visibleIds)
    }
}
