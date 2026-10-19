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
    fun expandAll_shouldShowAllNodes() {
        val locations = listOf(
            Location(id = 1, name = "Parent 1", parentId = null),
            Location(id = 2, name = "Child 1.1", parentId = 1),
            Location(id = 3, name = "Parent 2", parentId = null)
        )
        allLocationsLiveData.value = locations
        viewModel.expandAll()
        val visible = viewModel.visibleLocations.value ?: emptyList()
        assertThat(visible).hasSize(3)
    }
}
