package com.example.chiefinventory.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SignatureStats
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.ui.viewmodel.SignatureReportViewModel
import com.example.chiefinventory.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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

        val viewModel = SignatureReportViewModel(repository)
        val observer = Observer<List<CollectionItem>> { }
        viewModel.filteredItems.observeForever(observer)

        liveData.value = allItems

        val filtered = viewModel.filteredItems.value ?: emptyList()
        val expectedSize = emptyItems.size + missingItems.size + 5

        assertThat(filtered).hasSize(expectedSize)
        assertThat(filtered).containsAtLeastElementsIn(emptyItems)
        assertThat(filtered).containsAtLeastElementsIn(missingItems)
        assertThat(filtered).containsAtLeastElementsIn(validItems.take(5))
        assertThat(filtered.none { it.id == 6L }).isTrue()

        viewModel.filteredItems.removeObserver(observer)
    }

    @Test
    fun `signatureStats should be exposed from repository`() {
        val stats = SignatureStats(totalCount = 20, validCount = 10, emptyCount = 5, missingCount = 5)
        val liveData = MutableLiveData(stats)
        whenever(repository.getSignatureStats()).thenReturn(liveData)
        whenever(repository.getAll()).thenReturn(MutableLiveData(emptyList()))

        val viewModel = SignatureReportViewModel(repository)

        assertThat(viewModel.signatureStats.value).isEqualTo(stats)
    }
}
