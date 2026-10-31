package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.LocationType
import com.chefinventory.app.repo.LocationRepository
import com.chefinventory.app.ui.model.DisplayLocation
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockLocRepo: LocationRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockLocRepo = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLocationViewModel_OperationsAndLiveData() = runTest {
        val rootLoc = Location(id = 1L, name = "Frigo", parentId = null, type = LocationType.INGREDIENT)
        val childLoc = Location(id = 2L, name = "Bac Légumes", parentId = 1L, type = LocationType.INGREDIENT)
        whenever(mockLocRepo.getAllByType(LocationType.INGREDIENT)).thenReturn(MutableLiveData(listOf(rootLoc, childLoc)))

        val viewModel = LocationViewModel(mockLocRepo)

        viewModel.allLocations.observeForever {}
        viewModel.displayLocations.observeForever {}
        viewModel.visibleLocations.observeForever {}

        viewModel.setLocationType(LocationType.INGREDIENT)
        viewModel.toggleExpansion(1L)
        viewModel.expandAll()

        assertThat(viewModel.currentType.value).isEqualTo(LocationType.INGREDIENT)
        assertThat(viewModel.allLocations.value).hasSize(2)
        assertThat(viewModel.displayLocations.value).hasSize(2)
        assertThat(viewModel.visibleLocations.value).hasSize(2)

        // Expand all when displayLocations value is null
        whenever(mockLocRepo.getAllByType(LocationType.RECIPE)).thenReturn(MutableLiveData())
        val vmEmpty = LocationViewModel(mockLocRepo)
        vmEmpty.expandAll()

        viewModel.insert("Cave", null).join()
        verify(mockLocRepo).insert(any())

        viewModel.update(rootLoc).join()
        verify(mockLocRepo).update(rootLoc)

        viewModel.delete(rootLoc).join()
        verify(mockLocRepo).delete(rootLoc)

        viewModel.updateLocationParent(2L, 1L).join()
        verify(mockLocRepo).updateLocationParent(2L, 1L)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCompanionHelpers_ToggleExpandedExtractParentsBuildDisplayList() {
        // toggleLocationExpandedState null vs non-null set
        val toggledFromNull = LocationViewModel.toggleLocationExpandedState(null, 10L)
        assertThat(toggledFromNull).contains(10L)

        val toggledRemove = LocationViewModel.toggleLocationExpandedState(setOf(10L, 20L), 10L)
        assertThat(toggledRemove).doesNotContain(10L)

        // extractParentIds
        val loc1 = DisplayLocation(Location(id = 1L, name = "A", parentId = null, type = LocationType.INGREDIENT), 0)
        val loc2 = DisplayLocation(Location(id = 2L, name = "B", parentId = 1L, type = LocationType.INGREDIENT), 1)
        val parentIds = LocationViewModel.extractParentIds(listOf(loc1, loc2))
        assertThat(parentIds).containsExactly(1L)

        // buildDisplayList
        val locList = listOf(
            Location(id = 10L, name = "Root", parentId = null, type = LocationType.INGREDIENT),
            Location(id = 20L, name = "Child", parentId = 10L, type = LocationType.INGREDIENT)
        )
        val displayList = LocationViewModel.buildDisplayList(locList)
        assertThat(displayList).hasSize(2)
        assertThat(displayList[0].location.id).isEqualTo(10L)
        assertThat(displayList[1].location.id).isEqualTo(20L)
    }
}
