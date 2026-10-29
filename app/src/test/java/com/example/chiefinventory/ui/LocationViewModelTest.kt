package com.example.chiefinventory.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.model.DisplayLocation
import com.example.chiefinventory.ui.viewmodel.LocationViewModel
import com.example.chiefinventory.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class LocationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: LocationViewModel

    private val allLocationsLiveData = MutableLiveData<List<Location>>()
    private val observer = Observer<List<DisplayLocation>> { }

    @Before
    fun setup() {
        locationRepository = mock()
        whenever(locationRepository.getAll()).thenReturn(allLocationsLiveData)
        whenever(locationRepository.getAllByType(org.mockito.kotlin.any())).thenReturn(allLocationsLiveData)

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
        assertThat(visibleLocations).isEmpty()
    }

    @Test
    fun `insert root location should be visible immediately`() {
        allLocationsLiveData.value = emptyList()
        val newLocation = Location(id = 10, name = "New Root", parentId = null)
        allLocationsLiveData.value = listOf(newLocation)

        val visible = viewModel.visibleLocations.value ?: emptyList()
        val containsNewRoot = visible.any { it.location.id == 10L }
        assertThat(containsNewRoot).isTrue()
    }

    @Test
    fun `insert sub-location should be visible immediately even without expansion`() {
        val parent = Location(id = 1, name = "Parent", parentId = null)
        val child = Location(id = 2, name = "Child", parentId = 1)
        
        allLocationsLiveData.value = listOf(parent)
        allLocationsLiveData.value = listOf(parent, child)

        val visible = viewModel.visibleLocations.value ?: emptyList()
        assertThat(visible).hasSize(2)
        val containsChild = visible.any { it.location.id == 2L }
        assertThat(containsChild).isTrue()
    }

    @Test
    fun `toggleExpansion should not hide items in simplified mode`() {
        val locations = listOf(
            Location(id = 1, name = "Parent", parentId = null),
            Location(id = 2, name = "Child", parentId = 1)
        )
        allLocationsLiveData.value = locations
        
        viewModel.toggleExpansion(1L)
        
        val visible = viewModel.visibleLocations.value ?: emptyList()
        assertThat(visible).hasSize(2)
    }

    @Test
    fun `toggleLocationExpandedState and extractParentIds helper functions should be covered atomically`() {
        // toggleLocationExpandedState from null
        val setFromNull = LocationViewModel.toggleLocationExpandedState(null, 1L)
        assertThat(setFromNull).containsExactly(1L)

        // toggleLocationExpandedState add and remove
        val setAdd = LocationViewModel.toggleLocationExpandedState(setOf(1L), 2L)
        assertThat(setAdd).containsExactly(1L, 2L)

        val setRemove = LocationViewModel.toggleLocationExpandedState(setOf(1L, 2L), 1L)
        assertThat(setRemove).containsExactly(2L)

        // extractParentIds
        val displayLocs = listOf(
            DisplayLocation(Location(id = 1L, name = "Parent", parentId = null), 0),
            DisplayLocation(Location(id = 2L, name = "Child", parentId = 1L), 1)
        )
        val parentIds = LocationViewModel.extractParentIds(displayLocs)
        assertThat(parentIds).containsExactly(1L)
    }

    @Test
    fun expandAll_shouldShowAllNodes() {
        val locations = listOf(
            Location(id = 1, name = "Parent 1", parentId = null),
            Location(id = 2, name = "Child 1.1", parentId = 1),
            Location(id = 3, name = "Parent 2", parentId = null)
        )
        allLocationsLiveData.value = locations

        val observer = Observer<List<DisplayLocation>> { }
        viewModel.displayLocations.observeForever(observer)

        viewModel.expandAll()
        val visible = viewModel.visibleLocations.value ?: emptyList()
        assertThat(visible).hasSize(3)

        viewModel.displayLocations.removeObserver(observer)
    }

    @Test
    fun `setLocationType insert update delete and updateLocationParent should execute successfully`() = kotlinx.coroutines.test.runTest {
        viewModel.setLocationType(com.example.chiefinventory.model.LocationType.INGREDIENT)
        assertThat(viewModel.currentType.value).isEqualTo(com.example.chiefinventory.model.LocationType.INGREDIENT)

        val loc = Location(id = 5L, name = "Placard", parentId = null)

        viewModel.insert("Placard", null).join()
        org.mockito.kotlin.verify(locationRepository).insert(org.mockito.kotlin.any())

        viewModel.update(loc).join()
        org.mockito.kotlin.verify(locationRepository).update(loc)

        viewModel.delete(loc).join()
        org.mockito.kotlin.verify(locationRepository).delete(loc)

        viewModel.updateLocationParent(5L, 10L).join()
        org.mockito.kotlin.verify(locationRepository).updateLocationParent(5L, 10L)
    }
}
