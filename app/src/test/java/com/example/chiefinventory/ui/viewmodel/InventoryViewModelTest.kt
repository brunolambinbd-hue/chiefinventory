package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.CollectionItem
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric pour [InventoryViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InventoryViewModelTest {

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
    fun `findSimilarItems should calculate embedding and post similar items`() = runTest {
        val dummyBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val imageUri = Uri.parse("file:///tmp/scan.jpg")

        val resultItem = SearchResultItem(CollectionItem(id = 1L, titre = "Scan Match"), similarity = 0.9)
        whenever(mockRepo.findMostSimilarItems(any())).thenReturn(listOf(resultItem))

        val vm = InventoryViewModel(app, mockRepo)
        vm.findSimilarItems(dummyBitmap, imageUri).join()

        assertThat(vm.similarItems.value).isNotNull()
        assertThat(vm.similarItems.value?.second).isEqualTo(imageUri)
    }

    @Test
    fun `updateItemLocationAndStatus should update location and set possessed to true`() = runTest {
        val item = CollectionItem(id = 10L, titre = "Livre Test", isPossessed = false, locationId = null)
        whenever(mockRepo.getItemById(10L)).thenReturn(item)

        val vm = InventoryViewModel(app, mockRepo)
        vm.updateItemLocationAndStatus(10L, locationId = 5L).join()

        verify(mockRepo).getItemById(10L)
        verify(mockRepo).update(any())
    }
}
