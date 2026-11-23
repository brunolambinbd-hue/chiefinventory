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
 * Suite de tests unitaires TDD d'isolation vérifiant que 'eaux' ne crée jamais de lien à l'intérieur du mot 'morceaux'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrEauxInMorceauxTest {

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
    fun testFormatIngredientHyperlinks_Morceaux_HasZeroLinkOnEauxAndOnlyLinksParmesan() {
        val fullText = "4 c. à soupe de parmesan (moulu ou en morceaux)"
        val offTaxonomy = listOf("eaux", "parmesan")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Le sous-mot 'eaux' dans 'morceaux' ne doit JAMAIS être un lien hypertexte")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le seul lien hypertexte cliquable doit être 'parmesan'")
            .that(clickedWords.last())
            .isEqualTo("parmesan")
    }
}
