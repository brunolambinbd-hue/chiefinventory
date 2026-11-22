package com.chefinventory.app.utils

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.data.OpenFoodFactsTaxonomyLoader
import com.chefinventory.app.repository.OpenFoodFactsRepository
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
 * Suite de tests unitaires TDD d'intégration Robolectric avec vraie base Room DB SQLite en mémoire RAM.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrFromageStiltonRoomDbTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: OpenFoodFactsRepository
    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OpenFoodFactsRepository(database.openFoodFactsDao())
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        database.close()
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCeleriAuStilton_WithRealRoomDbTaxonomy_PreservesFromageStiltonOnSingleLine() = runBlocking {
        // Alimentation de la vraie base Room DB depuis l'asset du projet
        OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(context, database)

        val roomDbNames = repository.getAllCleanNames()
        assertWithMessage("La base Room DB doit contenir les noms d'aliments extraits du JSON")
            .that(roomDbNames)
            .isNotEmpty()

        val ocrText = """
            Céleri au Stilton
            INGRÉDIENTS
            6 branches de céleri blanc
            140 g de fromage stilton
            50 g de fromage blanc
            1 c. à café de porto blanc
            sel et poivre
            INSTRUCTIONS
            Préparer la recette.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary, roomDbNames)

        val ingredients = result.ingredients ?: ""

        assertWithMessage("L'ingrédient '140 g de fromage stilton' doit rester sur une seule et même ligne")
            .that(ingredients.lowercase())
            .contains("140 g de fromage stilton")
    }
}
