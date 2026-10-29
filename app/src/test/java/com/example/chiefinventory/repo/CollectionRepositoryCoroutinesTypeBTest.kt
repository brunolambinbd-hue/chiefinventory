package com.example.chiefinventory.repo

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.chiefinventory.dao.CategoryInfoFromDb
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SearchCriteria
import com.example.chiefinventory.model.SignatureStats
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)
    Shadows.shadowOf(Looper.getMainLooper()).idle()

    if (!latch.await(time, timeUnit)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CollectionRepositoryCoroutinesTypeBTest {

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

    @Test
    fun testGetSignatureStats_EmitsStats() = runTest {
        val mockDao = mock<CollectionDao>()
        val itemMissing = CollectionItem(id = 1L, titre = "A", imageEmbedding = null)
        val itemEmpty = CollectionItem(id = 2L, titre = "B", imageEmbedding = byteArrayOf())
        val itemValid = CollectionItem(id = 3L, titre = "C", imageEmbedding = byteArrayOf(1, 2, 3))

        whenever(mockDao.getAllSuspend()).thenReturn(listOf(itemMissing, itemEmpty, itemValid))

        val repository = CollectionRepository(mockDao)
        val stats: SignatureStats = repository.getSignatureStats().getOrAwaitValue()

        assertThat(stats).isNotNull()
        assertThat(stats.totalCount).isEqualTo(3)
        assertThat(stats.validCount).isEqualTo(1)
        assertThat(stats.emptyCount).isEqualTo(1)
        assertThat(stats.missingCount).isEqualTo(1)
    }

    @Test
    fun testGetSuperCategoryInfo_EmitsCategoryInfo() = runTest {
        val mockDao = mock<CollectionDao>()
        val dbCategoryInfo = CategoryInfoFromDb(name = "Bande Dessinée", possessedCount = 10, totalCount = 20)
        whenever(mockDao.getSuperCategoryInfoSuspend()).thenReturn(listOf(dbCategoryInfo))

        val repository = CollectionRepository(mockDao)
        val list: List<CategoryInfo> = repository.getSuperCategoryInfo(sM = true).getOrAwaitValue()

        assertThat(list).isNotNull()
        assertThat(list).isNotEmpty()
    }

    @Test
    fun testGetCategoryInfoForSuperCategory_EmitsSubCategoryInfo() = runTest {
        val mockDao = mock<CollectionDao>()
        val dbCategoryInfo = CategoryInfoFromDb(name = "Spirou", possessedCount = 5, totalCount = 10)
        whenever(mockDao.getCategoryInfoForSuperCategorySuspend("Bande Dessinée")).thenReturn(listOf(dbCategoryInfo))

        val repository = CollectionRepository(mockDao)
        val list: List<CategoryInfo> = repository.getCategoryInfoForSuperCategory("Bande Dessinée", sM = false).getOrAwaitValue()

        assertThat(list).isNotNull()
    }

    @Test
    fun testFindMostSimilarItems_ComputesVectorSimilarity() = runTest {
        val mockDao = mock<CollectionDao>()
        val item1 = CollectionItem(id = 1L, titre = "Spirou 1", imageEmbedding = byteArrayOf(10, 20, 30))
        val item2 = CollectionItem(id = 2L, titre = "Spirou 2", imageEmbedding = byteArrayOf(10, 20, 30))

        whenever(mockDao.getAllItemsWithEmbeddings()).thenReturn(listOf(item1, item2))

        val repository = CollectionRepository(mockDao)
        val criteria = SearchCriteria()
        val queryEmbedding = floatArrayOf(1.0f, 2.0f, 3.0f)

        val result = repository.advancedSearch(criteria, qE = queryEmbedding, qOcr = null)

        assertThat(result).isNotNull()
    }
}
