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
 * Suite de tests unitaires TDD d'isolation vérifiant la recherche dans les synonymes Open Food Facts ('sweet chili sauce') et l'exclusion du mot 'sauce' seul.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrSweetChiliSauceSynonymTest {

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
    fun testFormatIngredientHyperlinks_MatchesSweetChiliSauceFromSynonyms_AndExcludesSauceAlone() {
        runBlocking {
            val unifiedVocab = OcrVocabularyLoader.loadFromProjectResources(context, database)

            val fullText = """
                Pour la sauce
                2 c. à soupe de sweet chili sauce
            """.trimIndent()

            val clickedWords = mutableListOf<String>()

            val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
                context = context,
                fullText = fullText,
                vocabulary = unifiedVocab,
                offTaxonomyList = unifiedVocab.offTaxonomyList,
                onIngredientClick = { clickedWords.add(it) }
            )

            val allSpans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

            assertWithMessage("Le sous-titre 'Pour la sauce' ne doit comporter AUCUN lien cliquable, seul 'sweet chili sauce' doit l'être")
                .that(allSpans.size)
                .isEqualTo(1)

            allSpans[0].onClick(android.view.View(context))

            assertWithMessage("Le lien hypertexte doit capturer l'expression complète 'sweet chili sauce'")
                .that(clickedWords.last())
                .isEqualTo("sweet chili sauce")
        }
    }
}
