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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B vérifiant le comportement, le cache et la résilience de [OpenFoodFactsRepository]
 * à l'aide des 5 codes-barres réels d'Open Food Facts.
 */
class OpenFoodFactsRepositoryTest {

    private val realBarcodes = listOf(
        "3017620422003", // Nutella
        "3228857000166", // Lait Lactel
        "3083680085304", // Eau Evian
        "3124480183344", // Beurre Président
        "3038350008000"  // Riz Taureau Ailé
    )

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetProductByBarcode_5RealBarcodes_LocalHitAndRemoteMiss() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val mockApi = mock<OpenFoodFactsApiClient>()
            val repo = OpenFoodFactsRepository(mockDao, mockApi)

            for (barcode in realBarcodes) {
                val prod = OpenFoodFactsProduct(barcode = barcode, productName = "Produit $barcode")
                
                // Cache hit
                whenever(mockDao.getProductByBarcode(barcode)).thenReturn(prod)
                val resultHit = repo.getProductByBarcode(barcode)
                assertThat(resultHit).isEqualTo(prod)

                // Cache miss -> API hit
                whenever(mockDao.getProductByBarcode(barcode)).thenReturn(null)
                whenever(mockApi.fetchProductByBarcode(barcode, 5000)).thenReturn(prod)
                val resultRemote = repo.getProductByBarcode(barcode)
                assertThat(resultRemote).isEqualTo(prod)
                verify(mockDao).insertProduct(prod)
            }
        }
    }

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

            whenever(mockDao.getProductByBarcode("3017620422003")).thenReturn(null)

            val remoteProd = OpenFoodFactsProduct(barcode = "3017620422003", productName = "Nutella")
            whenever(mockApi.fetchProductByBarcode("3017620422003", 5000)).thenReturn(remoteProd)
            whenever(mockDao.insertProduct(remoteProd)).doThrow(RuntimeException("DB Lock"))

            val result = repo.getProductByBarcode("3017620422003")

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
    fun testGetTaxonomyByIdAndExactName() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val repo = OpenFoodFactsRepository(mockDao)

            val taxonomy = OpenFoodFactsTaxonomy(id = 10L, cleanName = "courgettes")
            whenever(mockDao.getTaxonomyById(10L)).thenReturn(taxonomy)
            whenever(mockDao.getTaxonomyByExactName("courgettes")).thenReturn(taxonomy)

            assertThat(repo.getTaxonomyById(10L)).isEqualTo(taxonomy)
            assertThat(repo.getTaxonomyById(0L)).isNull()

            assertThat(repo.getTaxonomyByExactName("courgettes")).isEqualTo(taxonomy)
            assertThat(repo.getTaxonomyByExactName("")).isNull()
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

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetTaxonomyPaged_SearchPaged_GetAllCategories() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val repo = OpenFoodFactsRepository(mockDao)

            val pagedList = listOf(OpenFoodFactsTaxonomy(cleanName = "pommes"))
            whenever(mockDao.getTaxonomyPaged(50, 0)).thenReturn(pagedList)

            val resultPaged = repo.getTaxonomyPaged(50, 0)
            assertThat(resultPaged).isEqualTo(pagedList)

            // Search paged with blank query -> calls getTaxonomyPaged
            val resultBlankSearch = repo.searchTaxonomyPaged("", 50, 0)
            assertThat(resultBlankSearch).isEqualTo(pagedList)

            // Search paged with query
            whenever(mockDao.searchTaxonomyPaged("%pommes%", 50, 0)).thenReturn(pagedList)
            val resultQuerySearch = repo.searchTaxonomyPaged("pommes", 50, 0)
            assertThat(resultQuerySearch).isEqualTo(pagedList)

            // Categories
            val categories = listOf("Épicerie", "Légumes")
            whenever(mockDao.getAllCategories()).thenReturn(categories)
            assertThat(repo.getAllCategories()).isEqualTo(categories)
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetFilteredTaxonomyPaged_AllBranchesAndAccents() {
        runBlocking {
            val mockDao = mock<OpenFoodFactsDao>()
            val repo = OpenFoodFactsRepository(mockDao)

            val expectedList = listOf(OpenFoodFactsTaxonomy(cleanName = "épinards", category = "Légumes"))
            whenever(mockDao.getFilteredTaxonomyPaged(anyOrNull(), anyOrNull(), anyOrNull(), any(), any())).thenReturn(expectedList)

            // 1. With category and query with accents
            val result1 = repo.getFilteredTaxonomyPaged(category = "Légumes", query = "épinards")
            assertThat(result1).isEqualTo(expectedList)

            // 2. With query ending with wildcard %
            val result2 = repo.getFilteredTaxonomyPaged(category = "Épicerie", query = "c%")
            assertThat(result2).isEqualTo(expectedList)

            // 3. With null category and query
            val result3 = repo.getFilteredTaxonomyPaged(category = null, query = null)
            assertThat(result3).isEqualTo(expectedList)
        }
    }
}
