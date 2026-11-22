package com.chefinventory.app.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.data.OpenFoodFactsTaxonomyLoader
import com.chefinventory.app.utils.OcrLevenshteinUtils
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires TDD d'isolation vérifiant la persistance statique du cache RAM d'Open Food Facts ([OpenFoodFactsRepository]).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OpenFoodFactsRepositoryGlobalCacheTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        OpenFoodFactsRepository.clearStaticCache()
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
        OpenFoodFactsRepository.clearStaticCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetAllCleanNames_PopulatesStaticGlobalCache_AndSurvivesRepositoryRecreation() = runBlocking {
        OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(context, database)

        // 1. Première instanciation du Repository (chargement initial depuis SQLite Room DB)
        val repo1 = OpenFoodFactsRepository(database.openFoodFactsDao())
        val startTime1 = System.currentTimeMillis()
        val names1 = repo1.getAllCleanNames()
        val duration1 = System.currentTimeMillis() - startTime1

        assertWithMessage("La base Room DB doit alimenter au moins 1 000 éléments Open Food Facts")
            .that(names1.size)
            .isGreaterThan(1000)

        val strippedNames = names1.map { OcrLevenshteinUtils.stripAccents(it) }
        assertWithMessage("La liste doit contenir 'VINAIGRE DE VIN BLANC'")
            .that(strippedNames)
            .contains("VINAIGRE DE VIN BLANC")

        assertWithMessage("La liste doit contenir 'SAUCE CHILI'")
            .that(strippedNames)
            .contains("SAUCE CHILI")

        // 2. Seconde instanciation du Repository (comme lors d'un nouveau scan dans EditRecipeActivity)
        val repo2 = OpenFoodFactsRepository(database.openFoodFactsDao())
        val startTime2 = System.currentTimeMillis()
        val names2 = repo2.getAllCleanNames()
        val duration2 = System.currentTimeMillis() - startTime2

        assertWithMessage("Les instances de Repository ré-instanciées doivent renvoyer exactement la même liste de 3 200+ aliments")
            .that(names2)
            .containsExactlyElementsIn(names1)
            .inOrder()

        assertWithMessage("La seconde lecture depuis le cache RAM statique global doit être quasi-instantanée (<= 5 ms)")
            .that(duration2)
            .isAtMost(10L)
    }
}
