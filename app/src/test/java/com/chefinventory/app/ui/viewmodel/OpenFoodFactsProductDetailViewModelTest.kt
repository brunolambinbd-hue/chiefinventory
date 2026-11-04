package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chefinventory.app.model.OpenFoodFactsProduct
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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B vérifiant [OpenFoodFactsProductDetailViewModel]
 * à l'aide des 5 codes-barres réels d'Open Food Facts :
 * 1. Nutella (3017620422003)
 * 2. Lait Lactel (3228857000166)
 * 3. Eau Evian (3083680085304)
 * 4. Beurre Président (3124480183344)
 * 5. Riz Taureau Ailé (3038350008000)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OpenFoodFactsProductDetailViewModelTest {

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
    fun testLoadAndImport_5RealBarcodes_Success() {
        val testProducts = listOf(
            OpenFoodFactsProduct(barcode = "3017620422003", productName = "Nutella", brand = "Ferrero", quantityStr = "350 g", nutriScore = "E"),
            OpenFoodFactsProduct(barcode = "3228857000166", productName = "Lait Demi-Écrémé", brand = "Lactel", quantityStr = "1 L", nutriScore = "B"),
            OpenFoodFactsProduct(barcode = "3083680085304", productName = "Eau Minérale Evian", brand = "Evian", quantityStr = "1.5 L", nutriScore = "A"),
            OpenFoodFactsProduct(barcode = "3124480183344", productName = "Beurre Doux Gastronomique", brand = "Président", quantityStr = "250 g", nutriScore = "E"),
            OpenFoodFactsProduct(barcode = "3038350008000", productName = "Riz Basmati", brand = "Taureau Ailé", quantityStr = "500 g", nutriScore = "A")
        )

        runBlockingTest {
            for (product in testProducts) {
                whenever(mockOffRepository.getProductByBarcode(product.barcode)).thenReturn(product)

                val viewModel = OpenFoodFactsProductDetailViewModel(mockOffRepository, mockIngredientRepository)
                viewModel.loadProduct(product.barcode, null)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(viewModel.product.value).isEqualTo(product)

                // Import into Pantry
                viewModel.importToPantry()
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(viewModel.importStatus.value).contains(product.productName)
                viewModel.clearImportStatus()
                assertThat(viewModel.importStatus.value).isNull()
            }
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadProduct_TaxonomyFallbackByIdNameAndSearchMatches() {
        val taxonomy = OpenFoodFactsTaxonomy(id = 42L, cleanName = "courgettes", category = "Légumes", synonyms = "courges")

        runBlockingTest {
            whenever(mockOffRepository.getTaxonomyById(42L)).thenReturn(taxonomy)
            whenever(mockOffRepository.getTaxonomyByExactName("courgettes")).thenReturn(taxonomy)
            whenever(mockOffRepository.searchTaxonomy("courgette")).thenReturn(listOf(taxonomy))
            whenever(mockOffRepository.searchTaxonomy("inconnu")).thenReturn(emptyList())

            val viewModel = OpenFoodFactsProductDetailViewModel(mockOffRepository, mockIngredientRepository)

            // 1. Load by taxonomy ID
            viewModel.loadProduct(null, null, taxonomyId = 42L)
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.taxonomy.value).isEqualTo(taxonomy)

            // 2. Load by exact name
            viewModel.loadProduct(null, "courgettes", taxonomyId = 0L)
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.taxonomy.value).isEqualTo(taxonomy)

            // 3. Fallback search Taxonomy matches
            viewModel.loadProduct(null, "courgette", taxonomyId = 0L)
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.taxonomy.value).isEqualTo(taxonomy)

            // 4. Fallback search no match -> constructs default
            viewModel.loadProduct(null, "inconnu", taxonomyId = 0L, category = "Divers")
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.taxonomy.value?.cleanName).isEqualTo("inconnu")

            // Import taxonomy item
            viewModel.importToPantry()
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.importStatus.value).contains("Inconnu")
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadProductAndImport_ExceptionBranches() {
        runBlockingTest {
            whenever(mockOffRepository.getProductByBarcode("error")).doThrow(RuntimeException("Network error"))

            val viewModel = OpenFoodFactsProductDetailViewModel(mockOffRepository, mockIngredientRepository)
            viewModel.loadProduct("error", null)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.product.value).isNull()
            assertThat(viewModel.taxonomy.value).isNull()
        }

        runBlockingTest {
            val nutellaProduct = OpenFoodFactsProduct(barcode = "3017620422003", productName = "Nutella")
            whenever(mockOffRepository.getProductByBarcode("3017620422003")).thenReturn(nutellaProduct)
            whenever(mockIngredientRepository.insert(any())).doThrow(RuntimeException("DB insert error"))

            val viewModel = OpenFoodFactsProductDetailViewModel(mockOffRepository, mockIngredientRepository)
            viewModel.loadProduct("3017620422003", null)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.importToPantry()
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.importStatus.value).contains("Erreur")
        }
    }

    private fun runBlockingTest(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking { block() }
}
