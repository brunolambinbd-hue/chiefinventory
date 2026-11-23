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
 * Suite de tests unitaires TDD d'isolation vérifiant que 'Des petites feuilles de chicon' ne crée un lien que sur 'feuilles de chicon' (0 lien sur 'des' et 'petites').
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrFeuillesDeChiconTest {

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
    fun testFormatIngredientHyperlinks_DesPetitesFeuillesDeChicon_OnlyLinksFeuillesDeChicon() {
        val fullText = "Des petites feuilles de chicon"
        val offTaxonomy = listOf("chicon", "endive")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("L'article 'Des' et l'adjectif 'petites' ne doivent pas faire partie du lien hypertexte (1 seul lien sur 'feuilles de chicon')")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le seul lien cliquable doit capturer l'ingrédient 'chicon'")
            .that(clickedWords.last())
            .isEqualTo("chicon")
    }
}
