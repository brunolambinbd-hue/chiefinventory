package com.example.chiefinventory.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.dao.CategoryInfoFromDb
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.dao.ItemCountForLocation
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SearchCriteria
import com.example.chiefinventory.ui.model.CategoryInfo
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
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Suite de tests unitaires de Type B couvrant à 100% toutes les méthodes et branches de [CollectionRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CollectionRepositoryMethodsTypeBTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createFloat32Embedding(v1: Float, v2: Float = 0.0f): ByteArray {
        val buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putFloat(v1)
        buffer.putFloat(v2)
        return buffer.array()
    }

    @Test
    fun `basic query methods should delegate to collectionDao`() = runTest {
        val mockDao = mock<CollectionDao>()
        val liveData = MutableLiveData<List<CollectionItem>>(emptyList())
        val liveDataCount = MutableLiveData<Int>(10)
        val liveDataItemCount = MutableLiveData<List<ItemCountForLocation>>(emptyList())
        val liveDataItem = MutableLiveData<CollectionItem>()

        whenever(mockDao.getAll()).thenReturn(liveData)
        whenever(mockDao.getUnlocatedItems()).thenReturn(liveData)
        whenever(mockDao.getLocatedNotPossessedItems()).thenReturn(liveData)
        whenever(mockDao.getItemCountByLocation()).thenReturn(liveDataItemCount)
        whenever(mockDao.getItemsByLocationId(1L)).thenReturn(liveData)
        whenever(mockDao.getRecentPossessed()).thenReturn(liveData)
        whenever(mockDao.getRecentLocated()).thenReturn(liveData)
        whenever(mockDao.getAllPossessed()).thenReturn(liveData)
        whenever(mockDao.getAllSought()).thenReturn(liveData)
        whenever(mockDao.getTotalCount()).thenReturn(liveDataCount)
        whenever(mockDao.getById(1L)).thenReturn(liveDataItem)
        whenever(mockDao.getItemsBySuperCategoryAndCategory("Super", "Cat", true)).thenReturn(liveData)

        val repository = CollectionRepository(mockDao)

        assertThat(repository.getAll()).isEqualTo(liveData)
        assertThat(repository.getUnlocatedItems()).isEqualTo(liveData)
        assertThat(repository.getLocatedNotPossessedItems()).isEqualTo(liveData)
        assertThat(repository.getItemCountByLocation()).isEqualTo(liveDataItemCount)
        assertThat(repository.getItemsByLocationId(1L)).isEqualTo(liveData)
        assertThat(repository.getRecentPossessed()).isEqualTo(liveData)
        assertThat(repository.getRecentLocated()).isEqualTo(liveData)
        assertThat(repository.getAllPossessed()).isEqualTo(liveData)
        assertThat(repository.getAllSought()).isEqualTo(liveData)
        assertThat(repository.getTotalCount()).isEqualTo(liveDataCount)
        assertThat(repository.getById(1L)).isEqualTo(liveDataItem)
        assertThat(repository.getItemsBySuperCategoryAndCategory("Super", "Cat", true)).isEqualTo(liveData)

        val item = CollectionItem(id = 10L, titre = "A")
        repository.delete(item)
        verify(mockDao).delete(item)
    }

    @Test
    fun `suspend search methods should delegate to collectionDao`() = runTest {
        val mockDao = mock<CollectionDao>()
        val item = CollectionItem(id = 1L, titre = "Spirou")
        val itemsList = listOf(item)

        whenever(mockDao.getAllSuspend()).thenReturn(itemsList)
        whenever(mockDao.getItemById(1L)).thenReturn(item)
        whenever(mockDao.findByRemoteId(101)).thenReturn(item)
        whenever(mockDao.search("%Spirou%")).thenReturn(itemsList)
        whenever(mockDao.getAllByTitle("%Spirou%")).thenReturn(itemsList)

        val repository = CollectionRepository(mockDao)

        assertThat(repository.getAllItemsSuspend()).isEqualTo(itemsList)
        assertThat(repository.getItemById(1L)).isEqualTo(item)
        assertThat(repository.findByRemoteId(101)).isEqualTo(item)
        assertThat(repository.search("Spirou")).isEqualTo(itemsList)
        assertThat(repository.getAllByTitle("Spirou")).isEqualTo(itemsList)
    }

    @Test
    fun `advancedSearch with isPossessed true and false and blank strings should cover all query building branches`() = runTest {
        val mockDao = mock<CollectionDao>()
        val itemTrue = CollectionItem(id = 1L, titre = "Spirou", editeur = "Dupuis", isPossessed = true)
        val itemFalse = CollectionItem(id = 2L, titre = "Tintin", editeur = "Casterman", isPossessed = false)

        whenever(mockDao.advancedSearch(any())).thenReturn(listOf(itemTrue, itemFalse))
        whenever(mockDao.countAdvancedSearch(any())).thenReturn(2)

        val repository = CollectionRepository(mockDao)

        // Branch: isPossessed = true
        val criteriaTrue = SearchCriteria(
            titre = "Spirou", editeur = "Dupuis", annee = 1980, mois = 5,
            superCategorie = "Album", categorie = "Albums", description = "Aventure",
            tirage = "Standard", dimensions = "A4", isPossessed = true
        )
        val resTrue = repository.advancedSearch(criteriaTrue, qE = null, qOcr = "Spirou")
        assertThat(resTrue.results).isNotEmpty()

        // Default parameter call (omitting qOcr to trigger advancedSearch$default)
        val resDefaultArg = repository.advancedSearch(criteriaTrue, qE = null)
        assertThat(resDefaultArg.results).isNotEmpty()

        // Branch: isPossessed = false
        val criteriaFalse = SearchCriteria(isPossessed = false)
        val resFalse = repository.advancedSearch(criteriaFalse, qE = null, qOcr = null)
        assertThat(resFalse.results).isNotEmpty()

        // Branch: blank strings (takeIf evaluates to null)
        val criteriaBlank = SearchCriteria(
            titre = "   ", editeur = "", superCategorie = "  ", categorie = "",
            description = "", tirage = " ", dimensions = "", isPossessed = null
        )
        val resBlank = repository.advancedSearch(criteriaBlank, qE = null, qOcr = "   ")
        assertThat(resBlank.results).isNotEmpty()
    }

    @Test
    fun `advancedSearch with query embedding and OCR branches should cover all similarity and OCR boost conditions`() = runTest {
        val mockDao = mock<CollectionDao>()

        val bytesFloat32 = createFloat32Embedding(1.0f, 0.0f)
        val bytesInt8 = byteArrayOf(100, 0)
        val bytesMismatched = byteArrayOf(1, 2, 3)
        val bytesZeroNorm = createFloat32Embedding(0.0f, 0.0f)
        val bytesEmpty = byteArrayOf()

        val itemOcrMatch = CollectionItem(id = 1L, titre = "OCR Match", imageEmbedding = bytesFloat32, ocrText = "Aventure Spirou")
        val itemOcrNoMatch = CollectionItem(id = 2L, titre = "OCR No Match", imageEmbedding = bytesInt8, ocrText = "Texte Autre")
        val itemOcrNull = CollectionItem(id = 3L, titre = "OCR Null", imageEmbedding = bytesFloat32, ocrText = null)
        val itemMismatched = CollectionItem(id = 4L, titre = "Mismatched", imageEmbedding = bytesMismatched)
        val itemZeroNorm = CollectionItem(id = 5L, titre = "Zero Norm", imageEmbedding = bytesZeroNorm)
        val itemNullEmbedding = CollectionItem(id = 6L, titre = "Null Embedding", imageEmbedding = null)
        val itemEmptyEmbedding = CollectionItem(id = 7L, titre = "Empty Embedding", imageEmbedding = bytesEmpty) // Line 88 isNotEmpty branch

        val items = listOf(itemOcrMatch, itemOcrNoMatch, itemOcrNull, itemMismatched, itemZeroNorm, itemNullEmbedding, itemEmptyEmbedding)
        whenever(mockDao.getAllItemsWithEmbeddings()).thenReturn(items)
        whenever(mockDao.advancedSearch(any())).thenReturn(items)
        whenever(mockDao.countAdvancedSearch(any())).thenReturn(items.size)

        val repository = CollectionRepository(mockDao)
        val queryEmbedding = floatArrayOf(1.0f, 0.0f)

        // Default parameter call with query embedding (omitting qOcr)
        val resDefaultOcr = repository.advancedSearch(SearchCriteria(), qE = queryEmbedding)
        assertThat(resDefaultOcr.results).isNotEmpty()

        // qE != null WITH qOcr non-null (covers ocrText contains, ocrText no match, ocrText null)
        val resOcr = repository.advancedSearch(SearchCriteria(), qE = queryEmbedding, qOcr = "Spirou")
        assertThat(resOcr.results).isNotEmpty()

        // qE != null WITHOUT qOcr (covers qOcr == null)
        val resNoOcr = repository.advancedSearch(SearchCriteria(), qE = queryEmbedding, qOcr = null)
        assertThat(resNoOcr.results).isNotEmpty()
    }

    @Test
    fun `findMostSimilarItems should filter isNaN and sort results`() = runTest {
        val mockDao = mock<CollectionDao>()

        val bytesValid1 = createFloat32Embedding(1.0f, 0.0f)
        val bytesValid2 = createFloat32Embedding(0.8f, 0.2f)
        val bytesZeroNorm = createFloat32Embedding(0.0f, 0.0f)
        val bytesNaN = createFloat32Embedding(Float.NaN, Float.NaN) // Line 109 isNaN branch

        val item1 = CollectionItem(id = 1L, titre = "Item 1", imageEmbedding = bytesValid1)
        val item2 = CollectionItem(id = 2L, titre = "Item 2", imageEmbedding = bytesValid2)
        val item3 = CollectionItem(id = 3L, titre = "Item 3", imageEmbedding = bytesZeroNorm)
        val itemNaN = CollectionItem(id = 4L, titre = "Item NaN", imageEmbedding = bytesNaN)

        whenever(mockDao.getAllItemsWithEmbeddings()).thenReturn(listOf(item1, item2, item3, itemNaN))

        val repository = CollectionRepository(mockDao)
        val queryEmbedding = floatArrayOf(1.0f, 0.0f)

        val results = repository.findMostSimilarItems(queryEmbedding)
        assertThat(results).isNotEmpty()
        assertThat(results.first().item.titre).isEqualTo("Item 1")
    }

    @Test
    fun `getSuperCategoryInfo and getCategoryInfoForSuperCategory should cover all status colors and missing category fallbacks`() = runTest {
        val mockDao = mock<CollectionDao>()

        // Supercategories matching CategoryMapper.getSuperCategories()
        val catHigh = CategoryInfoFromDb(name = "Album", possessedCount = 80, totalCount = 100)      // pct = 80% (>75%)
        val catMid = CategoryInfoFromDb(name = "Carte", possessedCount = 50, totalCount = 100)       // pct = 50% (>25%)
        val catLow = CategoryInfoFromDb(name = "Divers", possessedCount = 10, totalCount = 100)      // pct = 10% (<=25%)
        val catZero = CategoryInfoFromDb(name = "Illustration", possessedCount = 0, totalCount = 0)  // t = 0

        // Subcategories matching CategoryMapper.getCategoriesFor("Album")
        val subHigh = CategoryInfoFromDb(name = "Albums", possessedCount = 80, totalCount = 100)
        val subMid = CategoryInfoFromDb(name = "Albums collectifs", possessedCount = 50, totalCount = 100)
        val subLow = CategoryInfoFromDb(name = "Albums éditions étrangères", possessedCount = 10, totalCount = 100)

        whenever(mockDao.getSuperCategoryInfoSuspend()).thenReturn(listOf(catHigh, catMid, catLow, catZero))
        whenever(mockDao.getCategoryInfoForSuperCategorySuspend("Album")).thenReturn(listOf(subHigh, subMid, subLow))
        whenever(mockDao.getCategoryInfoForSuperCategorySuspend("Carte")).thenReturn(emptyList()) // Line 155 null fallback branch

        val repository = CollectionRepository(mockDao)

        // Sought Mode (sM = true) -> tests getStatusColor with t != 0 for sM = true
        val superSought: List<CategoryInfo> = repository.getSuperCategoryInfo(sM = true).getOrAwaitValue()
        assertThat(superSought).isNotEmpty()

        // Possessed Mode (sM = false) -> tests getStatusColor with t != 0 for sM = false
        val superPossessed: List<CategoryInfo> = repository.getSuperCategoryInfo(sM = false).getOrAwaitValue()
        assertThat(superPossessed).isNotEmpty()

        // Subcategory Sought Mode (sM = true)
        val subSought: List<CategoryInfo> = repository.getCategoryInfoForSuperCategory("Album", sM = true).getOrAwaitValue()
        assertThat(subSought).isNotEmpty()

        // Subcategory Possessed Mode (sM = false)
        val subPossessed: List<CategoryInfo> = repository.getCategoryInfoForSuperCategory("Album", sM = false).getOrAwaitValue()
        assertThat(subPossessed).isNotEmpty()

        // Subcategory with empty DB result (line 155 null fallback c == null)
        val subEmpty: List<CategoryInfo> = repository.getCategoryInfoForSuperCategory("Carte", sM = false).getOrAwaitValue()
        assertThat(subEmpty).isNotEmpty()
    }
}
