package com.example.parabdcollector.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.model.ExpandableLocation
import com.example.parabdcollector.ui.viewmodel.LocationViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [com.example.parabdcollector.ui.viewmodel.LocationViewModel].
 */
class LocationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var locationRepository: LocationRepository
    private lateinit var collectionRepository: CollectionRepository
    private lateinit var viewModel: LocationViewModel

    // LiveData mocks
    private val allLocationsLiveData = MutableLiveData<List<Location>>()
    private val itemCountLiveData = MutableLiveData<List<com.example.parabdcollector.ui.model.ItemCountForLocation>>()
    private val observer = Observer<List<ExpandableLocation>> { }

    @Before
    fun setup() {
        locationRepository = mock()
        collectionRepository = mock()

        // Whenever the repositories are asked for data, return our mock LiveData objects.
        whenever(locationRepository.getAll()).thenReturn(allLocationsLiveData)
        whenever(collectionRepository.getItemCountByLocation()).thenReturn(itemCountLiveData)

        // Initialize the ViewModel with both mocked repositories
        viewModel = LocationViewModel(locationRepository, collectionRepository)

        // Crucial: Attach an observer to trigger the MediatorLiveData computations.
        viewModel.visibleLocations.observeForever(observer)
    }

    @After
    fun tearDown() {
        // Clean up the observer to prevent test leaks.
        viewModel.visibleLocations.removeObserver(observer)
    }

    @Test
    fun `visibleLocations should be empty when no data is provided`() {
        // GIVEN: The repositories provide empty lists.
        allLocationsLiveData.value = emptyList()
        itemCountLiveData.value = emptyList()

        // THEN: The list of visible locations should not be null and should be empty.
        val visibleLocations = viewModel.visibleLocations.value
        assertEquals(0, visibleLocations?.size)
    }

    @Test
    fun `visibleLocations should contain root items with correct counts`() {
        // GIVEN: The repositories provide a list of root locations and their item counts.
        val locations = listOf(
            Location(id = 1, name = "Salon", parentLocationId = null),
            Location(id = 2, name = "Bureau", parentLocationId = null)
        )
        val counts = listOf(
            com.example.parabdcollector.ui.model.ItemCountForLocation(1, 5), // 5 items in Salon
            com.example.parabdcollector.ui.model.ItemCountForLocation(2, 10)  // 10 items in Bureau
        )
        allLocationsLiveData.value = locations
        itemCountLiveData.value = counts

        // THEN: The visible list should contain two items with the correct counts.
        val visible = viewModel.visibleLocations.value
        assertEquals(2, visible?.size)
        assertEquals(5, visible?.find { it.location.id == 1L }?.itemCount)
        assertEquals(10, visible?.find { it.location.id == 2L }?.itemCount)
    }

    @Test
    fun `toggleExpansion should add and remove id from expanded set`() {
        // GIVEN: A list of locations is available.
        val locations = listOf(
            Location(id = 1, name = "Parent", parentLocationId = null),
            Location(id = 2, name = "Child", parentLocationId = 1)
        )
        allLocationsLiveData.value = locations
        itemCountLiveData.value = emptyList()

        // WHEN: We toggle a location twice.
        viewModel.toggleExpansion(1L) // Expand
        val isExpanded = viewModel.visibleLocations.value?.find { it.location.id == 1L }?.isExpanded
        
        viewModel.toggleExpansion(1L) // Collapse
        val isCollapsed = viewModel.visibleLocations.value?.find { it.location.id == 1L }?.isExpanded

        // THEN: The expanded state should change accordingly.
        assertEquals(true, isExpanded)
        assertEquals(false, isCollapsed)
    }
}
