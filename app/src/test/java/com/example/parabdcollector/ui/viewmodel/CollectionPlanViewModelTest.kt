package com.example.parabdcollector.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.parabdcollector.dao.FullHierarchyItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for [CollectionPlanViewModel].
 */
@ExperimentalCoroutinesApi
class CollectionPlanViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository

    @Before
    fun setup() {
        repository = mock()
    }

    @Test
    fun `hierarchy should expose data from repository`() {
        // GIVEN: Repository provides a specific hierarchy list
        val mockData = listOf(
            FullHierarchyItem("Album", "Albums", 10, 20),
            FullHierarchyItem("Image", "Affiches", 5, 15),
        )
        val liveData = MutableLiveData(mockData)
        whenever(repository.getFullHierarchy()).thenReturn(liveData)

        // WHEN: ViewModel is created
        val viewModel = CollectionPlanViewModel(repository)

        // THEN: The hierarchy LiveData should expose the repository data
        assertEquals(mockData, viewModel.hierarchy.value)
    }
}
