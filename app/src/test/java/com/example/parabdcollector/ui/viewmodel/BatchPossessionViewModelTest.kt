package com.example.parabdcollector.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class BatchPossessionViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var locationRepository: LocationRepository
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BatchPossessionViewModel

    @Before
    fun setup() {
        repository = mock()
        locationRepository = mock()
        // Mock default for locationRepository.getAll() as it is used in init
        whenever(locationRepository.getAll()).thenReturn(MutableLiveData(emptyList()))
        viewModel = BatchPossessionViewModel(repository, locationRepository, testDispatcher)
    }

    private fun createItem(id: Long, title: String, isPossessed: Boolean = false): CollectionItem {
        return CollectionItem(
            id = id,
            titre = title,
            isPossessed = isPossessed,
            editeur = "Test",
            annee = 2024,
        )
    }

    @Test
    fun `analyzeSeries should identify items within range and not possessed`(): Unit = runTest {
        // GIVEN
        val series = "Tintin"
        val mockItems = listOf(
            createItem(1, "Tintin n°1"),       // Found
            createItem(2, "Tintin n°2"),       // Found
            createItem(3, "Tintin n°5"),       // Out of range
            createItem(4, "Tintin n°2", isPossessed = true), // Already possessed
            createItem(5, "Other n°1"),        // Should not be in result of repository.getAllByTitle("Tintin") but good to test filter
        )
        whenever(repository.getAllByTitle(series)).thenReturn(mockItems)

        // WHEN
        viewModel.analyzeSeries(series, 1, 3)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        val result = viewModel.analysisResult.value
        assertEquals(2, result?.foundCount)
        assertEquals(3, result?.rangeSize) // Range 1..3 is size 3
    }

    @Test
    fun `applyUpdate should update identified items and post status`(): Unit = runTest {
        // GIVEN
        val series = "Tintin"
        val itemToUpdate = createItem(1, "Tintin n°1")
        whenever(repository.getAllByTitle(series)).thenReturn(listOf(itemToUpdate))
        
        viewModel.analyzeSeries(series, 1, 1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectedLocationId = 100L

        // WHEN
        viewModel.applyUpdate()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        verify(repository).update(itemToUpdate.copy(isPossessed = true, locationId = 100L))
        assertEquals(1, viewModel.updateStatus.value)
        assertNull(viewModel.analysisResult.value)
    }

    @Test
    fun `applyUpdate should do nothing if no items analyzed`(): Unit = runTest {
        // WHEN
        viewModel.applyUpdate()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        verify(repository, times(0)).update(any())
        assertNull(viewModel.updateStatus.value)
    }
}
