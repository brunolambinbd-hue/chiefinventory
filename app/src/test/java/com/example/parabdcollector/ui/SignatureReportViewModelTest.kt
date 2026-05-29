package com.example.parabdcollector.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.parabdcollector.dao.SignatureReportItem
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.ui.viewmodel.SignatureReportViewModel
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [com.example.parabdcollector.ui.viewmodel.SignatureReportViewModel].
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
    fun `filteredItems should contain all problem items and only 20 valid items`() {
        // GIVEN: A list of items with various signature statuses
        // In SignatureReportItem, hasEmbedding=false means it's a "problem"
        val validItems = (1..30).map { 
            SignatureReportItem(id = it.toLong(), titre = "Valid $it", imageUri = null, hasEmbedding = true) 
        }
        val problemItems = (31..35).map { 
            SignatureReportItem(id = it.toLong(), titre = "Problem $it", imageUri = null, hasEmbedding = false) 
        }
        val allItems = validItems + problemItems

        val liveData = MutableLiveData<List<SignatureReportItem>>()
        whenever(repository.getSignatureReportItems()).thenReturn(liveData)

        // WHEN: The ViewModel is created and its output is observed
        val viewModel = SignatureReportViewModel(repository)
        val observer = Observer<List<SignatureReportItem>> { }
        viewModel.filteredItems.observeForever(observer)

        // AND WHEN: The data is emitted from the repository
        liveData.value = allItems

        // THEN: The filtered list should contain all problem items (sorted by id) and only the first 20 valid items.
        val filtered = viewModel.filteredItems.value
        val expectedSize = problemItems.size + 20

        assertEquals("Filtered list should have the correct size", expectedSize, filtered?.size)
        assertTrue("Filtered list should contain all problem items", filtered?.containsAll(problemItems) ?: false)
        assertTrue("Filtered list should contain the first 20 valid items", filtered?.containsAll(validItems.take(20)) ?: false)
        assertTrue("Filtered list should NOT contain the 21st valid item", filtered?.none { it.id == 21L } ?: true)

        // Clean up the observer
        viewModel.filteredItems.removeObserver(observer)
    }

    @Test
    fun `signatureStats should be exposed from repository`() {
        // GIVEN: The repository is programmed to return specific stats
        val stats = SignatureStats(totalCount = 20, validCount = 10, emptyCount = 5, missingCount = 5)
        val liveData = MutableLiveData(stats)
        whenever(repository.getSignatureStats()).thenReturn(liveData)
        // We also need to provide a source for _reportItems for the init block to run
        whenever(repository.getSignatureReportItems()).thenReturn(MutableLiveData(emptyList()))

        // WHEN: The ViewModel is created
        val viewModel = SignatureReportViewModel(repository)

        // THEN: The signatureStats LiveData should expose the data from the repository
        assertEquals(stats, viewModel.signatureStats.value)
    }
}
