package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SearchCriteria
import com.example.chiefinventory.model.AdvancedSearchResult
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.ui.model.SearchResultItem
import com.google.common.truth.Truth.assertThat
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric pour [SearchViewModel] et [SearchResultState].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SearchViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var app: Application
    private lateinit var mockRepo: CollectionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
        mockRepo = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search should update state to Success when repository returns items`() = runTest {
        val items = listOf(CollectionItem(id = 1L, titre = "Spirou"))
        whenever(mockRepo.search("Spirou")).thenReturn(items)

        val vm = SearchViewModel(app, mockRepo)
        vm.search("Spirou").join()

        val state = vm.searchResultState.value
        assertThat(state).isInstanceOf(SearchResultState.Success::class.java)
        val success = state as SearchResultState.Success
        assertThat(success.results).hasSize(1)
        assertThat(success.totalCount).isEqualTo(1)
    }

    @Test
    fun `search should update state to Error when repository throws exception`() = runTest {
        whenever(mockRepo.search("Error")).thenThrow(RuntimeException("DB failure"))

        val vm = SearchViewModel(app, mockRepo)
        vm.search("Error").join()

        val state = vm.searchResultState.value
        assertThat(state).isInstanceOf(SearchResultState.Error::class.java)
    }

    @Test
    fun `advancedSearch should update state to Success when criteria matches`() = runTest {
        val criteria = SearchCriteria(titre = "Tintin")
        val searchResult = SearchResultItem(CollectionItem(id = 2L, titre = "Tintin au pays des Soviets"))
        val advResult = AdvancedSearchResult(results = listOf(searchResult), totalCount = 1)

        whenever(mockRepo.advancedSearch(eq(criteria), anyOrNull(), anyOrNull())).thenReturn(advResult)

        val vm = SearchViewModel(app, mockRepo)
        vm.advancedSearch(criteria, bitmap = null).join()

        val state = vm.searchResultState.value
        assertThat(state).isInstanceOf(SearchResultState.Success::class.java)
        val success = state as SearchResultState.Success
        assertThat(success.results).hasSize(1)
    }

    @Test
    fun `advancedSearch should update state to Error when exception occurs`() = runTest {
        val criteria = SearchCriteria(titre = "Invalid")
        whenever(mockRepo.advancedSearch(eq(criteria), anyOrNull(), anyOrNull())).thenThrow(RuntimeException("Search error"))

        val vm = SearchViewModel(app, mockRepo)
        vm.advancedSearch(criteria, bitmap = null).join()

        val state = vm.searchResultState.value
        assertThat(state).isInstanceOf(SearchResultState.Error::class.java)
    }

    @Test
    fun `calculateSignatureForPreview should compute preview text`() = runTest {
        val dummyBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        val vm = SearchViewModel(app, mockRepo)
        vm.calculateSignatureForPreview(dummyBitmap).join()

        assertThat(vm.signaturePreview.value).isNotNull()
    }

    @Test
    fun `clearSearchResults should reset search state to Idle and clear preview`() {
        val vm = SearchViewModel(app, mockRepo)

        vm.clearSearchResults()

        assertThat(vm.searchResultState.value).isEqualTo(SearchResultState.Idle)
        assertThat(vm.signaturePreview.value).isEmpty()
    }
}
