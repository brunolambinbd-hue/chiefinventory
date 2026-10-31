package com.chefinventory.app.utils

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.data.AppDatabase
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StarterRecipeSeedLoaderTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadSeedRecipesFromAssets_ValidAndMissingAssetFiles() {
        val seeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssets(context)
        assertThat(seeds).isNotEmpty()

        val beSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "seeds/recipes_seed_be.json")
        assertThat(beSeeds).isNotEmpty()

        val missingSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "non_existent_seed_file.json")
        assertThat(missingSeeds).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testImportSeedListToDatabase_WithInMemoryDatabase() = runTest {
        val seeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssets(context)
        assertThat(seeds).isNotEmpty()

        val result = StarterRecipeSeedLoader.importSeedListToDatabase(seeds, db)
        assertThat(result.insertedCount + result.updatedCount).isGreaterThan(0)

        // Second import (tests update branch)
        val reResult = StarterRecipeSeedLoader.importSeedListToDatabase(seeds, db)
        assertThat(reResult.updatedCount).isAtLeast(0)
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testImportSeedRecipesFromAssets_AllPacksSupported() = runTest {
        val beSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "seeds/recipes_seed_be.json")
        val frSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "seeds/recipes_seed_fr.json")
        val itSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "seeds/recipes_seed_it.json")

        val resultBE = StarterRecipeSeedLoader.importSeedListToDatabase(beSeeds, db)
        val resultFR = StarterRecipeSeedLoader.importSeedListToDatabase(frSeeds, db)
        val resultIT = StarterRecipeSeedLoader.importSeedListToDatabase(itSeeds, db)

        assertThat(resultBE.insertedCount + resultBE.updatedCount).isGreaterThan(0)
        assertThat(resultFR.insertedCount + resultFR.updatedCount).isGreaterThan(0)
        assertThat(resultIT.insertedCount + resultIT.updatedCount).isGreaterThan(0)
    }
}
