package com.example.chiefinventory.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SignatureStats
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.ui.viewmodel.SignatureReportViewModel
import com.example.chiefinventory.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [com.example.chiefinventory.ui.viewmodel.SignatureReportViewModel].
 */
@ExperimentalCoroutinesApi
class SignatureReportViewModelTest {

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
    fun `filteredItems should contain all problem items and only 5 valid items`() {
        // GIVEN: A list of items with various signature statuses
        val validItems = (1..10).map { 
            CollectionItem(id = it.toLong(), titre = "Valid $it", imageEmbedding = byteArrayOf(it.toByte())) 
        }
        val emptyItems = (11..12).map { 
            CollectionItem(id = it.toLong(), titre = "Empty $it", imageEmbedding = byteArrayOf()) 
        }
        val missingItems = (13..14).map { 
            CollectionItem(id = it.toLong(), titre = "Missing $it", imageEmbedding = null) 
        }
        val allItems = validItems + emptyItems + missingItems

        val liveData = MutableLiveData<List<CollectionItem>>()
        whenever(repository.getAll()).thenReturn(liveData)

        // WHEN: The ViewModel is created and its output is observed
        val viewModel = SignatureReportViewModel(repository)
        val observer = Observer<List<CollectionItem>> { }
        viewModel.filteredItems.observeForever(observer)

        // AND WHEN: The data is emitted from the repository
        liveData.value = allItems

        // THEN: The filtered list should contain all problem items (empty + missing) and only the first 5 valid items.
        val filtered = viewModel.filteredItems.value
        val expectedSize = emptyItems.size + missingItems.size + 5

        assertEquals("Filtered list should have the correct size", expectedSize, filtered?.size)
        assertTrue("Filtered list should contain all empty items", filtered?.containsAll(emptyItems) ?: false)
        assertTrue("Filtered list should contain all missing items", filtered?.containsAll(missingItems) ?: false)
        assertTrue("Filtered list should contain the first 5 valid items", filtered?.containsAll(validItems.take(5)) ?: false)
        assertTrue("Filtered list should NOT contain the 6th valid item", filtered?.none { it.id == 6L } ?: true)

        // Clean up the observer
        viewModel.filteredItems.removeObserver(observer)
    }

    @Test
    fun `signatureStats should be exposed from repository`() {
        // GIVEN: The repository is programmed to return specific stats
        val stats = SignatureStats(totalCount = 20, validCount = 10, emptyCount = 5, missingCount = 5)
        val liveData = MutableLiveData(stats)
        whenever(repository.getSignatureStats()).thenReturn(liveData)
        // We also need to provide a source for _allItems for the init block to run
        whenever(repository.getAll()).thenReturn(MutableLiveData(emptyList()))

        // WHEN: The ViewModel is created
        val viewModel = SignatureReportViewModel(repository)

        // THEN: The signatureStats LiveData should expose the data from the repository
        assertEquals(stats, viewModel.signatureStats.value)
    }
}
