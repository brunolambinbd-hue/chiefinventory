package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires TDD d'isolation vérifiant l'unification en un seul lien hypertexte continu de 'vin muscadet', 'vin de saumur', 'vin de bordeaux'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrVinMuscadetTest {

    private lateinit var context: Context
    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_VinMuscadet_CreatesSingleUnifiedSpan() {
        val testCases = mapOf(
            "2 dl de vin muscadet" to "vin muscadet",
            "1 verre de vin de saumur" to "vin de saumur",
            "15 cl de vin de bordeaux" to "vin de bordeaux"
        )
        val offTaxonomy = listOf("vin", "muscadet", "saumur", "bordeaux")

        testCases.forEach { (line, expectedCompound) ->
            val clickedWords = mutableListOf<String>()
            val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
                context = context,
                fullText = line,
                vocabulary = vocabulary,
                offTaxonomyList = offTaxonomy,
                onIngredientClick = { clickedWords.add(it) }
            )

            val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

            assertWithMessage("Sur la ligne '$line', il doit y avoir EXACTEMENT 1 seul lien hypertexte continu unifié '$expectedCompound'")
                .that(spans.size)
                .isEqualTo(1)

            spans[0].onClick(android.view.View(context))

            assertWithMessage("Le seul lien cliquable doit capturer l'expression complète '$expectedCompound'")
                .that(clickedWords.last())
                .isEqualTo(expectedCompound)
        }
    }
}
