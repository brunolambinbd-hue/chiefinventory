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
 * Test unitaire TDD d'intégration Robolectric avec vraie base Room DB SQLite en mémoire RAM pour la recette Épinards / Langoustine.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrVinaigreDeVinBlancRoomDbTest {

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
    fun testVinaigreDeVinBlanc_WithRealRoomDbTaxonomy_CreatesSingleContiguousHyperlinkSpan() {
        runBlocking {
            // Alimentation de la vraie base Room DB depuis l'asset off_french_taxonomy.json
            OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(context, database)

            val roomDbNames = repository.getAllCleanNames()
            assertWithMessage("La base Room DB doit contenir le terme 'vinaigre de vin blanc'")
                .that(roomDbNames.map { OcrLevenshteinUtils.stripAccents(it) })
                .contains("VINAIGRE DE VIN BLANC")

            val line = "1 c. à soupe de vinaigre de vin blanc"
            val clickedWords = mutableListOf<String>()

            val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
                context = context,
                fullText = line,
                vocabulary = vocabulary,
                offTaxonomyList = roomDbNames,
                onIngredientClick = { clickedWords.add(it) }
            )

            val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

            assertWithMessage("Un seul lien hypertexte continu doit être créé pour 'vinaigre de vin blanc'")
                .that(spans.size)
                .isEqualTo(1)

            spans[0].onClick(android.view.View(context))

            assertWithMessage("Le clic sur le lien hypertexte doit capturer l'expression complète 'vinaigre de vin blanc'")
                .that(clickedWords)
                .containsExactly("vinaigre de vin blanc")
        }
    }
}
