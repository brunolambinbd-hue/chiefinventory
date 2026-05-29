package com.example.parabdcollector.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
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
class CategoryAuditViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CategoryAuditViewModel

    @Before
    fun setup() {
        repository = mock()
        viewModel = CategoryAuditViewModel(repository, testDispatcher)
    }

    private fun createItem(id: Long, category: String, superCategory: String?): CollectionItem {
        return CollectionItem(
            id = id,
            titre = "Item $id",
            categorie = category,
            superCategorie = superCategory,
            editeur = "Test",
            annee = 2024,
        )
    }

    @Test
    fun `performAudit should identify items with missing or N-D supercategories`() = runTest {
        // GIVEN
        val items = listOf(
            createItem(1, "Albums", null),           // To fix (null)
            createItem(2, "Albums", "N/D"),          // To fix (N/D)
            createItem(3, "Albums", "#N/D"),         // To fix (#N/D)
            createItem(4, "Albums", "Non Défini"),   // To fix (Non Défini)
            createItem(5, "Albums", "Album"),        // Correct
            createItem(6, "Unknown", "N/D")          // Skip (no mapping for "Unknown")
        )
        whenever(repository.getAllItemsSuspend()).thenReturn(items)

        // WHEN
        viewModel.performAudit()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        assertEquals(4, viewModel.auditResult.value)
    }

    @Test
    fun `fixInconsistencies should update repository and clear audit result`() = runTest {
        // GIVEN
        val itemToFix = createItem(1, "Albums", "N/D")
        whenever(repository.getAllItemsSuspend()).thenReturn(listOf(itemToFix))
        
        // Audit first to populate itemsToFix
        viewModel.performAudit()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.auditResult.value)

        // WHEN
        viewModel.fixInconsistencies()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        // Correct superCategory for "Albums" is "Album"
        verify(repository).update(any())
        assertEquals(1, viewModel.updateStatus.value)
        assertNull(viewModel.auditResult.value)
    }

    @Test
    fun `fixInconsistencies should do nothing if no items were audited`() = runTest {
        // WHEN
        viewModel.fixInconsistencies()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        verify(repository, times(0)).update(any())
        assertNull(viewModel.updateStatus.value)
    }
}
