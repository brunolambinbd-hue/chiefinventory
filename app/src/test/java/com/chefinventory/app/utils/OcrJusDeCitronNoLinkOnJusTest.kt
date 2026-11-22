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
 * Suite de tests unitaires TDD d'isolation vérifiant que 'jus' dans 'le jus de 1/2 citron' ou 'le zeste d'une orange' ne crée pas de lien sur 'jus' ou 'zeste'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrJusDeCitronNoLinkOnJusTest {

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
    fun testFormatIngredientHyperlinks_JusDeCitron_HasZeroLinkOnJusAndOnlyLinksCitron() {
        val fullText = "le jus de 1/2 citron"
        val offTaxonomy = listOf("jus", "citron")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val allSpans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)
        val spanTexts = allSpans.map { 
            val start = spannable.getSpanStart(it)
            val end = spannable.getSpanEnd(it)
            "[$start..$end: '${spannable.substring(start, end)}']"
        }
        assertWithMessage("Tous les liens créés: $spanTexts")
            .that(allSpans.size)
            .isEqualTo(1)

        allSpans[0].onClick(android.view.View(context))

        assertWithMessage("Le seul lien hypertexte cliquable doit capturer uniquement l'aliment 'citron'")
            .that(clickedWords.last())
            .isEqualTo("citron")
    }
}
