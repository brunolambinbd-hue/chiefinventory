package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.util.MainDispatcherRule
import com.google.mediapipe.tasks.components.containers.Embedding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class InventoryViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var imageEmbedderHelper: ImageEmbedderHelper
    private lateinit var application: Application
    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setup() {
        repository = mock()
        imageEmbedderHelper = mock()
        application = mock()
        viewModel = InventoryViewModel(application, repository, imageEmbedderHelper)
    }

    @Test
    fun `updateItemLocationAndStatus should update item to possessed with new location`(): Unit = runTest {
        // GIVEN
        val itemId = 1L
        val locationId = 10L
        val originalItem = CollectionItem(id = itemId, titre = "Test", isPossessed = false, locationId = null)
        whenever(repository.getItemById(itemId)).thenReturn(originalItem)

        // WHEN
        viewModel.updateItemLocationAndStatus(itemId, locationId)

        // THEN
        val expectedItem = originalItem.copy(locationId = locationId, isPossessed = true)
        verify(repository).update(expectedItem)
    }

    @Test
    fun `updateItemLocationAndStatus should do nothing if item not found`(): Unit = runTest {
        // GIVEN
        val itemId = 1L
        whenever(repository.getItemById(itemId)).thenReturn(null)

        // WHEN
        viewModel.updateItemLocationAndStatus(itemId, 99L)

        // THEN
        verify(repository, never()).update(any())
    }

    @Test
    fun `findSimilarItems should update similarItems LiveData on success`(): Unit = runTest {
        // GIVEN
        val bitmap = mock<Bitmap>()
        val uri = mock<Uri>()
        val mockEmbedding = mock<Embedding>()
        val floatArray = floatArrayOf(0.1f, 0.2f)
        whenever(mockEmbedding.floatEmbedding()).thenReturn(floatArray)
        whenever(imageEmbedderHelper.computeSignature(bitmap)).thenReturn(mockEmbedding)

        val searchResults = listOf(SearchResultItem(mock(), 0.9))
        whenever(repository.findMostSimilarItems(floatArray)).thenReturn(searchResults)

        val observer = mock<Observer<Pair<List<SearchResultItem>, Uri>>>()
        viewModel.similarItems.observeForever(observer)

        // WHEN
        viewModel.findSimilarItems(bitmap, uri)

        // THEN
        verify(repository).findMostSimilarItems(floatArray)
        verify(observer).onChanged(Pair(searchResults, uri))
        assertEquals(Pair(searchResults, uri), viewModel.similarItems.value)
        
        viewModel.similarItems.removeObserver(observer)
    }

    @Test
    fun `findSimilarItems should post empty list if embedding is null`(): Unit = runTest {
        // GIVEN
        val bitmap = mock<Bitmap>()
        val uri = mock<Uri>()
        whenever(imageEmbedderHelper.computeSignature(bitmap)).thenReturn(null)

        val observer = mock<Observer<Pair<List<SearchResultItem>, Uri>>>()
        viewModel.similarItems.observeForever(observer)

        // WHEN
        viewModel.findSimilarItems(bitmap, uri)

        // THEN
        verify(repository, never()).findMostSimilarItems(any())
        verify(observer).onChanged(Pair(emptyList(), uri))
        
        viewModel.similarItems.removeObserver(observer)
    }
}
