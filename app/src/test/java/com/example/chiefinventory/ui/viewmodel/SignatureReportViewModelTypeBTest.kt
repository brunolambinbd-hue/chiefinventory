package com.example.chiefinventory.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SignatureStats
import com.example.chiefinventory.repo.CollectionRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires pour [SignatureReportViewModel].
 */
class SignatureReportViewModelTypeBTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRepo: CollectionRepository

    @Before
    fun setUp() {
        mockRepo = mock()
    }

    @Test
    fun `isSignatureProblematic and isSignatureValid helpers should evaluate byte arrays`() {
        assertThat(SignatureReportViewModel.isSignatureProblematic(null)).isTrue()
        assertThat(SignatureReportViewModel.isSignatureProblematic(byteArrayOf())).isTrue()
        assertThat(SignatureReportViewModel.isSignatureProblematic(byteArrayOf(1, 2, 3))).isFalse()

        assertThat(SignatureReportViewModel.isSignatureValid(null)).isFalse()
        assertThat(SignatureReportViewModel.isSignatureValid(byteArrayOf())).isFalse()
        assertThat(SignatureReportViewModel.isSignatureValid(byteArrayOf(1, 2, 3))).isTrue()
    }

    @Test
    fun `SignatureReportViewModel should combine problematic and valid items`() {
        val allItemsLiveData = MutableLiveData<List<CollectionItem>>()
        val statsLiveData = MutableLiveData<SignatureStats>(SignatureStats(2, 1, 0, 1))

        whenever(mockRepo.getAll()).thenReturn(allItemsLiveData)
        whenever(mockRepo.getSignatureStats()).thenReturn(statsLiveData)

        val vm = SignatureReportViewModel(mockRepo)
        val observer = Observer<List<CollectionItem>> { }
        vm.filteredItems.observeForever(observer)

        val problematicItem = CollectionItem(id = 1L, titre = "Sans Image", imageEmbedding = null)
        val validItem = CollectionItem(id = 2L, titre = "Avec Image", imageEmbedding = byteArrayOf(1, 2, 3))

        allItemsLiveData.value = listOf(problematicItem, validItem)

        val list = vm.filteredItems.value ?: emptyList()
        assertThat(list).hasSize(2)
        assertThat(list[0]).isEqualTo(problematicItem)
        assertThat(list[1]).isEqualTo(validItem)

        assertThat(vm.signatureStats.value?.totalCount).isEqualTo(2)

        vm.filteredItems.removeObserver(observer)
    }
}
