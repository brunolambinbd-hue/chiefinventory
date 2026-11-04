package com.chefinventory.app.data

import android.content.Context
import android.content.res.AssetManager
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.dao.OpenFoodFactsDao
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.IOException

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [OpenFoodFactsTaxonomyLoader].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OpenFoodFactsTaxonomyLoaderTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(context)
    }

    @After
    fun tearDown() {
        AppDatabase.closeInstance()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseTaxonomyJson_ValidAndInvalidJson() {
        // 1. Valid JSON
        val validJson = """
            [
              {"cleanName": "haricots verts", "category": "Légumes", "synonyms": "princesses"},
              {"cleanName": "pommes de terre", "category": "", "synonyms": ""}
            ]
        """.trimIndent()
        val items = OpenFoodFactsTaxonomyLoader.parseTaxonomyJson(validJson)
        assertThat(items).hasSize(2)
        assertThat(items[0].cleanName).isEqualTo("haricots verts")
        assertThat(items[0].category).isEqualTo("Légumes")
        assertThat(items[1].category).isNull()

        // 2. Blank or empty JSON
        assertThat(OpenFoodFactsTaxonomyLoader.parseTaxonomyJson("")).isEmpty()
        assertThat(OpenFoodFactsTaxonomyLoader.parseTaxonomyJson("   ")).isEmpty()

        // 3. Malformed JSON -> catches exception gracefully
        assertThat(OpenFoodFactsTaxonomyLoader.parseTaxonomyJson("{ invalid json }")).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseTaxonomyJson_BlankItemAndNullCategoryAndSynonyms() {
        val json = """
            [
              {"cleanName": "   "},
              {"cleanName": "sucre", "category": null, "synonyms": null},
              {"cleanName": "sel"}
            ]
        """.trimIndent()
        val items = OpenFoodFactsTaxonomyLoader.parseTaxonomyJson(json)
        assertThat(items).hasSize(2)
        assertThat(items[0].cleanName).isEqualTo("sucre")
        assertThat(items[0].category).isNull()
        assertThat(items[0].synonyms).isNull()
        assertThat(items[1].cleanName).isEqualTo("sel")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSeedTaxonomyIfEmpty_FullFlowAndAlreadySeeded() = runBlocking {
        val insertedCount = OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(context, database)
        assertThat(insertedCount).isGreaterThan(0)

        val countInDb = database.openFoodFactsDao().getTaxonomyCount()
        assertThat(countInDb).isEqualTo(insertedCount)

        // Second call should return 0 because count > 0 and legumesCodeCount >= 100
        val secondCallCount = OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(context, database)
        assertThat(secondCallCount).isEqualTo(0)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSeedTaxonomyIfEmpty_ReseedsWhenLegumesCodeLow() = runBlocking {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()
        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("off_french_taxonomy.json")).thenReturn("[]".byteInputStream())

        val mockDb = mock<AppDatabase>()
        val mockDao = mock<OpenFoodFactsDao>()
        whenever(mockDb.openFoodFactsDao()).thenReturn(mockDao)
        whenever(mockDao.getTaxonomyCount()).thenReturn(5000)
        whenever(mockDao.getLegumesCodeCount()).thenReturn(0)

        val result = OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(mockContext, mockDb)
        assertThat(result).isEqualTo(0)
        org.mockito.kotlin.verify(mockDao).clearTaxonomy()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSeedTaxonomyIfEmpty_AssetNotFoundException_CatchesExceptionAndReturnsZero() = runBlocking {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()
        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("off_french_taxonomy.json")).thenThrow(IOException("Asset not found"))

        val mockDb = mock<AppDatabase>()
        val mockDao = mock<OpenFoodFactsDao>()
        whenever(mockDb.openFoodFactsDao()).thenReturn(mockDao)
        whenever(mockDao.getTaxonomyCount()).thenReturn(0)
        whenever(mockDao.getLegumesCodeCount()).thenReturn(0)

        val result = OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(mockContext, mockDb)
        assertThat(result).isEqualTo(0)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSeedTaxonomyIfEmpty_EmptyItemsParsed_ReturnsZero() = runBlocking {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()
        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("off_french_taxonomy.json")).thenReturn("[]".byteInputStream())

        val mockDb = mock<AppDatabase>()
        val mockDao = mock<OpenFoodFactsDao>()
        whenever(mockDb.openFoodFactsDao()).thenReturn(mockDao)
        whenever(mockDao.getTaxonomyCount()).thenReturn(0)
        whenever(mockDao.getLegumesCodeCount()).thenReturn(0)

        val result = OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(mockContext, mockDb)
        assertThat(result).isEqualTo(0)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSeedTaxonomyIfEmpty_InputStreamCloseException_CatchesException() = runBlocking {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()
        whenever(mockContext.assets).thenReturn(mockAssets)

        val failingCloseInputStream = object : ByteArrayInputStream("[]".toByteArray()) {
            override fun close() {
                throw IOException("Close failed")
            }
        }
        whenever(mockAssets.open("off_french_taxonomy.json")).thenReturn(failingCloseInputStream)

        val mockDb = mock<AppDatabase>()
        val mockDao = mock<OpenFoodFactsDao>()
        whenever(mockDb.openFoodFactsDao()).thenReturn(mockDao)
        whenever(mockDao.getTaxonomyCount()).thenReturn(0)
        whenever(mockDao.getLegumesCodeCount()).thenReturn(0)

        val result = OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(mockContext, mockDb)
        assertThat(result).isEqualTo(0)
    }
}
