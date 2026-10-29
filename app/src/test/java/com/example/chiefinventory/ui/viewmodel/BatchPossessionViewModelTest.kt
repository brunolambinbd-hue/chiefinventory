package com.example.chiefinventory.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class BatchPossessionViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: BatchPossessionViewModel

    @Before
    fun setup() {
        repository = mock()
        locationRepository = mock()
        whenever(locationRepository.getAll()).thenReturn(MutableLiveData(emptyList()))

        viewModel = BatchPossessionViewModel(repository, locationRepository, mainDispatcherRule.testDispatcher)
    }

    @Test
    fun `analyzeSeries should correctly identify range size`(): Unit = runTest {
        val item = createMockItem(1, "Spirou n°1500")
        whenever(repository.getAllByTitle(any())).thenReturn(listOf(item))

        viewModel.analyzeSeries("Spirou", 1500, 1600)
        advanceUntilIdle()

        val result = viewModel.analysisResult.value
        assertThat(result?.rangeSize).isEqualTo(101)
        assertThat(result?.foundCount).isEqualTo(1)
    }

    @Test
    fun `applyUpdate should update items with possessed status and selected location`(): Unit = runTest {
        val item = createMockItem(1, "Spirou n°1500")
        whenever(repository.getAllByTitle(any())).thenReturn(listOf(item))
        
        viewModel.analyzeSeries("Spirou", 1500, 1500)
        advanceUntilIdle() 
        
        val targetLocationId = 100L
        viewModel.selectedLocationId = targetLocationId

        viewModel.applyUpdate()
        
        kotlinx.coroutines.delay(100)
        advanceUntilIdle()

        verify(repository).update(argThat { 
            this.id == 1L && this.isPossessed && this.locationId == targetLocationId 
        })
        assertThat(viewModel.updateStatus.value).isEqualTo(1)
    }

    @Test
    fun `applyUpdate with empty itemsToUpdate should do nothing`() = runTest {
        viewModel.applyUpdate()
        assertThat(viewModel.updateStatus.value).isNull()
    }

    @Test
    fun `displayLocations should map locations hierarchy`() {
        val loc1 = com.example.chiefinventory.model.Location(id = 1L, name = "Étagère 1", parentId = null)
        val loc2 = com.example.chiefinventory.model.Location(id = 2L, name = "Boîte A", parentId = 1L)
        whenever(locationRepository.getAll()).thenReturn(MutableLiveData(listOf(loc1, loc2)))

        val vm = BatchPossessionViewModel(repository, locationRepository, mainDispatcherRule.testDispatcher)
        val list = vm.displayLocations.value ?: emptyList()

        assertThat(list).hasSize(2)
        assertThat(list[0].location.id).isEqualTo(1L)
        assertThat(list[0].depth).isEqualTo(0)
        assertThat(list[1].location.id).isEqualTo(2L)
        assertThat(list[1].depth).isEqualTo(1)
    }

    private fun createMockItem(id: Long, title: String, possessed: Boolean = false): CollectionItem {
        return CollectionItem(
            id = id, titre = title, isPossessed = possessed,
            editeur = "Dupuis", annee = 1980, mois = 1, categorie = "Spirou", superCategorie = "Magazines"
        )
    }
}
