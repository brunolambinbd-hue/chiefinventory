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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for [LocationViewModel].
 *
 * This class tests the logic for managing the visibility and expansion of the location hierarchy,
 * using a mock [LocationRepository] to isolate the ViewModel.
 */
class LocationViewModelTest {

    /**
     * This rule swaps the background executor used by the Architecture Components with a different one
     * that executes each task synchronously. This is crucial for testing LiveData.
     */
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: LocationViewModel

    // Mocks for LiveData and its observer
    private val allLocationsLiveData = MutableLiveData<List<Location>>()
    private val observer = Observer<List<DisplayLocation>> { }

    /**
     * Sets up the test environment before each test case.
     * It mocks the dependencies and initializes the [LocationViewModel].
     */
    @Before
    fun setup() {
        locationRepository = mock()

        // Whenever the repository's getAll() is called, return our controlled LiveData instance.
        whenever(locationRepository.getAll()).thenReturn(allLocationsLiveData)

        // Initialize the ViewModel with the mock repository.
        viewModel = LocationViewModel(locationRepository)

        // The MediatorLiveData `visibleLocations` is only activated when it has an active observer.
        // We observe it forever to ensure its transformation logic is triggered during tests.
        viewModel.visibleLocations.observeForever(observer)
    }

    /**
     * Cleans up the environment after each test case.
     * It removes the LiveData observer to prevent memory leaks and interference between tests.
     */
    @After
    fun tearDown() {
        viewModel.visibleLocations.removeObserver(observer)
    }

    /**
     * Verifies that when the repository provides an empty list, the visible locations list is also empty.
     */
    @Test
    fun `visibleLocations should be empty when no data is provided`() {
        // GIVEN: The repository provides an empty list.
        allLocationsLiveData.value = emptyList()

        // THEN: The list of visible locations should not be null and should be empty.
        val visibleLocations = viewModel.visibleLocations.value
        assertEquals(0, visibleLocations?.size)
    }

    /**
     * Verifies that toggling an expansion on a parent correctly shows and then hides its child.
     */
    @Test
    fun `toggleExpansion should make child visible and then hide it`() {
        // GIVEN: A parent and child location, with the tree initially collapsed.
        val locations = listOf(
            Location(id = 1, name = "Parent", parentId = null),
            Location(id = 2, name = "Child", parentId = 1)
        )
        allLocationsLiveData.value = locations
        // Ensure a collapsed state to start by toggling twice.
        viewModel.toggleExpansion(1L) // Expand
        viewModel.toggleExpansion(1L) // Then collapse

        // WHEN: We expand the parent.
        viewModel.toggleExpansion(1L)

        // THEN: The child should now be visible.
        var visible = viewModel.visibleLocations.value
        assertEquals(2, visible?.size)
        assertEquals(2L, visible?.get(1)?.location?.id)

        // WHEN: We collapse the parent again.
        viewModel.toggleExpansion(1L)

        // THEN: The child should be hidden again.
        visible = viewModel.visibleLocations.value
        assertEquals(1, visible?.size)
    }

    /**
     * Verifies that `expandAll` correctly expands the entire tree, making all nodes visible.
     * This reflects the new default behavior of the location management screen.
     */
    @Test
    fun `expandAll_shouldMakeAllNodesVisible`() {
        // GIVEN: A multi-level hierarchy of locations.
        val locations = listOf(
            Location(id = 1, name = "Parent 1", parentId = null),
            Location(id = 2, name = "Child 1.1", parentId = 1),
            Location(id = 3, name = "Parent 2", parentId = null),
            Location(id = 4, name = "Child 2.1", parentId = 3),
            Location(id = 5, name = "Grandchild 2.1.1", parentId = 4)
        )
        allLocationsLiveData.value = locations

        // WHEN: We expand the entire tree.
        viewModel.expandAll()

        // THEN: All 5 locations should be visible.
        val visible = viewModel.visibleLocations.value
        assertEquals("All 5 locations should be visible after expandAll", 5, visible?.size)

        // AND: The IDs of visible locations should match the full set, proving deep expansion.
        val visibleIds = visible?.map { it.location.id }?.toSet()
        val expectedIds = setOf(1L, 2L, 3L, 4L, 5L)
        assertEquals(expectedIds, visibleIds)
    }
}
