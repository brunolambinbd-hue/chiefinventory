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
 * Suite de tests unitaires TDD d'isolation vérifiant que '1 c. à café de curry' ne crée un lien que sur 'curry' (0 lien sur 'café'),
 * tandis que '100 g de café' crée bien un lien sur 'café'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrCafeUnitNoLinkTest {

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
    fun testFormatIngredientHyperlinks_CafeInUnitContext_DoesNotCreateLinkOnCafe() {
        val lineTeaspoon = "1 c. à café de curry"
        val offTaxonomy = listOf("café", "curry")
        val clickedWordsTeaspoon = mutableListOf<String>()

        val  spannableTeaspoon = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = lineTeaspoon,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWordsTeaspoon.add(it) }
        )

        val spansTeaspoon =  spannableTeaspoon.getSpans(0,  spannableTeaspoon.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Sur la ligne '1 c. à café de curry', il ne doit y avoir qu'UN SEUL lien cliquable (sur 'curry') et 0 lien sur 'café'")
            .that(spansTeaspoon.size)
            .isEqualTo(1)

        spansTeaspoon[0].onClick(android.view.View(context))

        assertWithMessage("Le seul lien cliquable doit être 'curry'")
            .that(clickedWordsTeaspoon.last())
            .isEqualTo("curry")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_CafeAsMainIngredient_CreatesLinkOnCafe() {
        val lineCoffee = "100 g de café"
        val offTaxonomy = listOf("café", "curry")
        val clickedWordsCoffee = mutableListOf<String>()

        val  spannableCoffee = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = lineCoffee,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWordsCoffee.add(it) }
        )

        val spansCoffee =  spannableCoffee.getSpans(0,  spannableCoffee.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Sur la ligne '100 g de café', 'café' est l'ingrédient principal et doit recevoir un lien cliquable")
            .that(spansCoffee.size)
            .isEqualTo(1)

        spansCoffee[0].onClick(android.view.View(context))

        assertWithMessage("Le lien cliquable doit capturer 'café'")
            .that(clickedWordsCoffee.last())
            .isEqualTo("café")
    }
}
