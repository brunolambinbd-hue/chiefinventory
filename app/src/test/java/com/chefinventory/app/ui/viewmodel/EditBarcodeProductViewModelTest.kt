package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chefinventory.app.model.OpenFoodFactsProduct
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B vérifiant le ViewModel d'édition de produit code-barres [EditBarcodeProductViewModel]
 * à l'aide des 5 codes-barres réels d'Open Food Facts.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditBarcodeProductViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockOffRepository: OpenFoodFactsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockOffRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadAndSaveProduct_5RealBarcodes() {
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

                val viewModel = EditBarcodeProductViewModel(mockOffRepository)

                // Load product by barcode
                viewModel.loadProduct(product.barcode)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(viewModel.product.value).isEqualTo(product)

                // Save product by barcode
                viewModel.saveProduct(product.barcode, product.productName, product.brand, product.quantityStr, product.nutriScore, product.imageUrl, product.ingredientsText)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(viewModel.saveStatus.value).contains("succès")
                viewModel.clearSaveStatus()
                assertThat(viewModel.saveStatus.value).isNull()
            }
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadAndSaveProduct_BlankBarcodeAndExceptionBranches() {
        runBlockingTest {
            val viewModel = EditBarcodeProductViewModel(mockOffRepository)

            // Blank barcode on load
            viewModel.loadProduct("")
            assertThat(viewModel.product.value).isNull()

            // Blank barcode on save
            viewModel.saveProduct("", "Nutella", "Ferrero", "400g", "E", null, null)
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(viewModel.saveStatus.value).contains("Erreur")
            verify(mockOffRepository, never()).saveProductLocally(any())
        }

        runBlockingTest {
            whenever(mockOffRepository.saveProductLocally(any())).doThrow(RuntimeException("Save error"))

            val viewModel = EditBarcodeProductViewModel(mockOffRepository)
            viewModel.saveProduct("3017620422003", "Nutella", null, null, null, null, null)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.saveStatus.value).contains("Erreur")
        }
    }

    private fun runBlockingTest(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking { block() }
}
