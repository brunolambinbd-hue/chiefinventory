package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repository.OpenFoodFactsRepository
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et la réactivité de [OpenFoodFactsCatalogViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OpenFoodFactsCatalogViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockOffRepository: OpenFoodFactsRepository
    private lateinit var mockIngredientRepository: IngredientRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockOffRepository = mock()
        mockIngredientRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadInitialData_SuccessAndExceptionBranches() {
        val taxonomyList = listOf(OpenFoodFactsTaxonomy(cleanName = "haricots verts", category = "Légumes"))

        runBlockingTest {
            whenever(mockOffRepository.getTaxonomyByCodePaged(any(), anyOrNull(), any(), any())).thenReturn(taxonomyList)

            val viewModel = OpenFoodFactsCatalogViewModel(mockOffRepository)
            viewModel.loadInitialData()
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.taxonomyList.value).isEqualTo(taxonomyList)
            assertThat(viewModel.isLoading.value).isFalse()
        }

        runBlockingTest {
            val failingRepo = mock<OpenFoodFactsRepository>()
            whenever(failingRepo.getTaxonomyByCodePaged(any(), anyOrNull(), any(), any())).doThrow(RuntimeException("DB error"))

            val viewModelException = OpenFoodFactsCatalogViewModel(failingRepo)
            viewModelException.loadInitialData()
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModelException.taxonomyList.value).isEmpty()
            assertThat(viewModelException.isLoading.value).isFalse()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSearch_BlankAndValidQuery() {
        val taxonomyList = listOf(OpenFoodFactsTaxonomy(cleanName = "haricots verts"))

        runBlockingTest {
            whenever(mockOffRepository.getTaxonomyByCodePaged(any(), anyOrNull(), any(), any())).thenReturn(taxonomyList)

            val viewModel = OpenFoodFactsCatalogViewModel(mockOffRepository)
            viewModel.loadInitialData()
            testDispatcher.scheduler.advanceUntilIdle()

            // Blank query search
            viewModel.search("")
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.taxonomyList.value).isEqualTo(taxonomyList)

            // Valid query search
            viewModel.search("haricots")
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.taxonomyList.value).isEqualTo(taxonomyList)
        }

        runBlockingTest {
            whenever(mockOffRepository.getTaxonomyByCodePaged(any(), anyOrNull(), any(), any())).doThrow(RuntimeException("Search error"))

            val viewModel = OpenFoodFactsCatalogViewModel(mockOffRepository)
            viewModel.search("error")
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.taxonomyList.value).isEmpty()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFilterByLetter_FullBranches() {
        val taxonomyList = listOf(OpenFoodFactsTaxonomy(cleanName = "haricots verts", category = "Légumes"))

        runBlockingTest {
            whenever(mockOffRepository.getTaxonomyByCodePaged(any(), anyOrNull(), any(), any())).thenReturn(taxonomyList)

            val viewModel = OpenFoodFactsCatalogViewModel(mockOffRepository)
            viewModel.loadInitialData()
            testDispatcher.scheduler.advanceUntilIdle()

            // Filter by letter "H"
            viewModel.filterByLetter("H")
            testDispatcher.scheduler.advanceUntilIdle()

            // Filter by letter "TOUS"
            viewModel.filterByLetter("TOUS")
            testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testImportToPantry_SuccessAndExceptionAndNullRepo() {
        val taxonomy = OpenFoodFactsTaxonomy(cleanName = "haricots verts", category = "Légumes")

        // 1. With null ingredientRepository -> returns early
        runBlockingTest {
            val viewModelNullRepo = OpenFoodFactsCatalogViewModel(mockOffRepository, null)
            viewModelNullRepo.importToPantry(taxonomy)
            assertThat(viewModelNullRepo.importStatus.value).isNull()
        }

        // 2. Success import
        runBlockingTest {
            val viewModel = OpenFoodFactsCatalogViewModel(mockOffRepository, mockIngredientRepository)
            viewModel.importToPantry(taxonomy)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.importStatus.value).contains("Haricots verts")
            verify(mockIngredientRepository).insert(any())

            viewModel.clearImportStatus()
            assertThat(viewModel.importStatus.value).isNull()
        }

        // 3. Import with uppercase cleanName and null category
        runBlockingTest {
            val taxonomyUppercase = OpenFoodFactsTaxonomy(cleanName = "Haricots verts", category = null)
            val viewModel = OpenFoodFactsCatalogViewModel(mockOffRepository, mockIngredientRepository)
            viewModel.importToPantry(taxonomyUppercase)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.importStatus.value).contains("Haricots verts")
        }

        // 4. Import with digit-starting cleanName ("200 g de farine")
        runBlockingTest {
            val taxonomyDigit = OpenFoodFactsTaxonomy(cleanName = "200 g de farine", category = "Légumes")
            val viewModel = OpenFoodFactsCatalogViewModel(mockOffRepository, mockIngredientRepository)
            viewModel.importToPantry(taxonomyDigit)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.importStatus.value).contains("200 g de farine")
        }

        // 5. Exception during import
        runBlockingTest {
            whenever(mockIngredientRepository.insert(any())).doThrow(RuntimeException("Insert error"))

            val viewModel = OpenFoodFactsCatalogViewModel(mockOffRepository, mockIngredientRepository)
            viewModel.importToPantry(taxonomy)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.importStatus.value).contains("Erreur")
        }
    }

    private fun runBlockingTest(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking { block() }
}
