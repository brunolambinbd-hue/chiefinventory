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
 * Suite de tests unitaires TDD d'isolation vérifiant que '2 avocats bien mûrs' conserve l'adjectif 'mûrs' intact sans déviation Levenshtein en 'mûres' et sans lien sur 'mûrs'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrAvocatsMursTest {

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
    fun testBeautifyAndHyperlinks_PreservesAvocatsBienMursIntactAndOnlyLinksAvocats() {
        val input = listOf("2 avocats bien mûrs")
        val offTaxonomy = listOf("mûres", "avocats")

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("L'adjectif 'mûrs' de '2 avocats bien mûrs' ne doit JAMAIS être dévié par Levenshtein en 'mûres'")
            .that(beautified)
            .containsExactly("2 avocats bien mûrs")

        val clickedWords = mutableListOf<String>()
        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = beautified[0],
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Seul l'aliment 'avocats' doit recevoir un lien hypertexte (0 lien sur l'adjectif 'mûrs')")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le seul lien cliquable doit capturer 'avocats'")
            .that(clickedWords.last())
            .isEqualTo("avocats")
    }
}
