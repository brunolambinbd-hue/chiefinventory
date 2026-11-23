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
 * Suite de tests unitaires TDD d'isolation vérifiant que '4 noix de beurre' extrait l'unité 'noix' et ne crée aucun lien hypertexte sur 'noix'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrNoixDeBeurreTest {

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
    fun testParseAndFormatHyperlinks_NoixDeBeurre_ExtractsUnitNoixAndLinksOnlyBeurre() {
        val line = "4 noix de beurre"
        val parsed = IngredientParser.parse(line, vocabulary.culinaryMeasurementUnits)

        assertWithMessage("Pour '4 noix de beurre', la quantité doit être 4.0, l'unité 'noix' et le nom 'beurre'")
            .that(parsed.quantity)
            .isEqualTo(4.0)

        assertWithMessage("L'unité culinaire extraite doit être 'noix'")
            .that(parsed.unit)
            .isEqualTo("noix")

        assertWithMessage("Le nom nettoyé de l'ingrédient doit être 'beurre'")
            .that(parsed.name)
            .isEqualTo("beurre")

        val offTaxonomy = listOf("noix", "beurre")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = line,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Sur '4 noix de beurre', seul l'ingrédient 'beurre' doit recevoir un lien hypertexte (0 lien sur l'unité 'noix')")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le seul lien cliquable doit capturer 'beurre'")
            .that(clickedWords.last())
            .isEqualTo("beurre")
    }
}
