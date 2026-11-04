package com.chefinventory.app.repository

import com.chefinventory.app.dao.OpenFoodFactsDao
import com.chefinventory.app.model.OpenFoodFactsProduct
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.chefinventory.app.network.OpenFoodFactsApiClient
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B vérifiant le comportement, le cache et la résilience de [OpenFoodFactsRepository].
 */
class OpenFoodFactsRepositoryTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetProductByBarcode_BlankBarcode_ReturnsNull() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val mockApi = mock<OpenFoodFactsApiClient>()
            val repo = OpenFoodFactsRepository(mockDao, mockApi)

            assertThat(repo.getProductByBarcode("")).isNull()
            assertThat(repo.getProductByBarcode("   ")).isNull()
            verify(mockDao, never()).getProductByBarcode(any())
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetProductByBarcode_LocalCacheHit_ReturnsLocalProductWithoutCallingApi() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val mockApi = mock<OpenFoodFactsApiClient>()
            val repo = OpenFoodFactsRepository(mockDao, mockApi)

            val localProd = OpenFoodFactsProduct(barcode = "3017620422003", productName = "Nutella")
            whenever(mockDao.getProductByBarcode("3017620422003")).thenReturn(localProd)

            val result = repo.getProductByBarcode("3017620422003")

            assertThat(result).isEqualTo(localProd)
            verify(mockApi, never()).fetchProductByBarcode(any(), any())
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetProductByBarcode_LocalCacheMiss_CallsApiAndCachesProductInRoom() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val mockApi = mock<OpenFoodFactsApiClient>()
            val repo = OpenFoodFactsRepository(mockDao, mockApi)

            whenever(mockDao.getProductByBarcode("3228857000166")).thenReturn(null)

            val remoteProd = OpenFoodFactsProduct(barcode = "3228857000166", productName = "Lait demi-écrémé")
            whenever(mockApi.fetchProductByBarcode("3228857000166", 5000)).thenReturn(remoteProd)

            val result = repo.getProductByBarcode("3228857000166")

            assertThat(result).isEqualTo(remoteProd)
            verify(mockDao).insertProduct(remoteProd)
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetProductByBarcode_LocalCacheMissAndRemoteApiMiss_ReturnsNullWithoutCaching() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val mockApi = mock<OpenFoodFactsApiClient>()
            val repo = OpenFoodFactsRepository(mockDao, mockApi)

            whenever(mockDao.getProductByBarcode("0000000000000")).thenReturn(null)
            whenever(mockApi.fetchProductByBarcode("0000000000000", 5000)).thenReturn(null)

            val result = repo.getProductByBarcode("0000000000000")

            assertThat(result).isNull()
            verify(mockDao, never()).insertProduct(any())
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetProductByBarcode_DaoInsertException_CatchesExceptionAndReturnsRemoteProduct() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val mockApi = mock<OpenFoodFactsApiClient>()
            val repo = OpenFoodFactsRepository(mockDao, mockApi)

            whenever(mockDao.getProductByBarcode("123456")).thenReturn(null)

            val remoteProd = OpenFoodFactsProduct(barcode = "123456", productName = "Biscuits")
            whenever(mockApi.fetchProductByBarcode("123456", 5000)).thenReturn(remoteProd)
            whenever(mockDao.insertProduct(remoteProd)).doThrow(RuntimeException("DB Lock"))

            val result = repo.getProductByBarcode("123456")

            assertThat(result).isEqualTo(remoteProd)
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSaveProductLocally_ValidAndBlankBarcode() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val repo = OpenFoodFactsRepository(mockDao)

            val validProd = OpenFoodFactsProduct(barcode = "3017620422003", productName = "Nutella")
            repo.saveProductLocally(validProd)
            verify(mockDao).insertProduct(validProd)

            val invalidProd = OpenFoodFactsProduct(barcode = "", productName = "Invalide")
            repo.saveProductLocally(invalidProd)
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSearchTaxonomy_BlankAndValidQuery() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val repo = OpenFoodFactsRepository(mockDao)

            assertThat(repo.searchTaxonomy("")).isEmpty()
            assertThat(repo.searchTaxonomy("   ")).isEmpty()

            val expectedList = listOf(OpenFoodFactsTaxonomy(cleanName = "haricots verts"))
            whenever(mockDao.searchTaxonomy("%haricots%")).thenReturn(expectedList)

            val result = repo.searchTaxonomy("haricots")
            assertThat(result).isEqualTo(expectedList)
        }
    }
}
