package com.example.parabdcollector.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.model.ExpandableLocation
import com.example.parabdcollector.ui.viewmodel.LocationViewModel
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [com.example.parabdcollector.ui.viewmodel.LocationViewModel].
 *
 * This class uses Mockito to create a mock [LocationRepository] to test the ViewModel's logic
 * in isolation from the data layer. Each test is self-contained to prevent state leakage.
 */
@ExperimentalCoroutinesApi
class LocationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * Verifies that the initial state of visibleLocations has parent items expanded by default,
     * and that root items are sorted alphabetically.
     */
    @Test
    fun `initial state should have parents expanded by default and be sorted`() = runTest {
        // GIVEN: A repository with a parent ("Salon") and its child, plus another root ("Bureau")
        val rootLocation1 = Location(id = 1, name = "Salon", parentLocationId = null)
        val childLocation1 = Location(id = 2, name = "Vitrine 1", parentLocationId = 1)
        val rootLocation2 = Location(id = 3, name = "Bureau", parentLocationId = null)
        val allLocations = listOf(rootLocation1, childLocation1, rootLocation2)
        val allLocationsLiveData = MutableLiveData<List<Location>>()
        val locationRepository: LocationRepository = mock()
        whenever(locationRepository.getAll()).thenReturn(allLocationsLiveData)

        val viewModel = LocationViewModel(locationRepository)
        val observer = Observer<List<ExpandableLocation>> { }
        viewModel.visibleLocations.observeForever(observer)

        // WHEN: The data is loaded
        allLocationsLiveData.value = allLocations
        val visibleLocations = viewModel.visibleLocations.value

        // THEN: The list should contain 3 items, sorted alphabetically at the root level.
        assertEquals("Default-expanded list size should be 3", 3, visibleLocations?.size)
        // "Bureau" comes before "Salon" alphabetically.
        assertEquals("Bureau", visibleLocations?.get(0)?.location?.name)
        assertEquals("Salon", visibleLocations?.get(1)?.location?.name)
        assertEquals("Salon should be expanded by default", true, visibleLocations?.get(1)?.isExpanded)
        assertEquals("Vitrine 1", visibleLocations?.get(2)?.location?.name)

        viewModel.visibleLocations.removeObserver(observer)
    }

    /**
     * Verifies that toggling an already expanded parent collapses it, and toggling it again re-expands it.
     */
    @Test
    fun `toggleExpansion should collapse an expanded parent and then re-expand it`() = runTest {
        // GIVEN: A ViewModel in its default state (parents are expanded)
        val rootLocation1 = Location(id = 1, name = "Salon", parentLocationId = null)
        val childLocation1 = Location(id = 2, name = "Vitrine 1", parentLocationId = 1)
        val allLocations = listOf(rootLocation1, childLocation1)
        val allLocationsLiveData = MutableLiveData<List<Location>> ()
        val locationRepository: LocationRepository = mock()
        whenever(locationRepository.getAll()).thenReturn(allLocationsLiveData)

        val viewModel = LocationViewModel(locationRepository)
        val observer = Observer<List<ExpandableLocation>> { }
        viewModel.visibleLocations.observeForever(observer)

        // Set value after observing
        allLocationsLiveData.value = allLocations
        // Initial state check: Parent is expanded by default
        assertEquals("Initial size should be 2 (parent expanded)", 2, viewModel.visibleLocations.value?.size)

        // WHEN: We toggle the already-expanded parent
        viewModel.toggleExpansion(1)
        val collapsedVisible = viewModel.visibleLocations.value

        // THEN: The list should collapse to 1 item
        assertEquals("List size should be 1 after collapse", 1, collapsedVisible?.size)
        assertEquals("Parent should be collapsed", false, collapsedVisible?.first()?.isExpanded)

        // WHEN: We toggle the collapsed parent again
        viewModel.toggleExpansion(1)
        val reExpandedVisible = viewModel.visibleLocations.value

        // THEN: The list should expand back to 2 items
        assertEquals("List size should be 2 after re-expansion", 2, reExpandedVisible?.size)
        assertEquals("Parent should be expanded again", true, reExpandedVisible?.first()?.isExpanded)

        viewModel.visibleLocations.removeObserver(observer)
    }

    /**
     * Verifies that calling [LocationViewModel.insert] correctly calls the repository.
     */
    @Test
    fun `insert should call insert on repository`() = runTest {
        // GIVEN: A repository and a fresh ViewModel
        val locationRepository: LocationRepository = mock()
        val liveData = MutableLiveData<List<Location>>(emptyList())
        whenever(locationRepository.getAll()).thenReturn(liveData)
        val viewModel = LocationViewModel(locationRepository)
        val newLocation = Location(name = "Chambre", parentLocationId = null)

        // WHEN: The insert method is called
        viewModel.insert(newLocation)

        // THEN: The repository's insert method should be called
        verify(locationRepository).insert(newLocation)
    }
}
