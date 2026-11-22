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
 * Suite de tests unitaires TDD d'isolation vérifiant que 'durs' (œufs durs) ne devient pas 'ours' et que les pièces/coupes (filets, cuisses, ailes, pilons, pattes) ne sont pas cliquables.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrOeufsDursFiletsPouletTest {

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
    fun testOeufsDurs_NeverReplacedByOursAndOnlyOeufsIsClickable() {
        val input = listOf("3 œufs durs")
        val offTaxonomy = listOf("oeufs", "œufs", "ours")

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("L'adjectif 'durs' de '3 œufs durs' ne doit JAMAIS être transformé en 'ours'")
            .that(beautified)
            .containsExactly("3 œufs durs")

        val fullText = "3 œufs durs"
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Seul le mot 'œufs' doit comporter un lien hypertexte cliquable (pas 'durs')")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le clic hypertexte doit capturer l'ingrédient 'œufs'")
            .that(clickedWords.last())
            .isEqualTo("œufs")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnimalCuts_FiletsAndCuissesAndAiles_NeverClickableAsStandaloneIngredients() {
        val fullText = """
            2 filets de saumon
            4 cuisses de poulet
            2 ailes de poulet
            4 pilons de poulet
        """.trimIndent()

        val offTaxonomy = listOf("saumon", "poulet", "filets", "cuisses", "ailes", "pilons")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Seuls les poissons/volailles noyaux (saumon, poulet, poulet, poulet) doivent être cliquables")
            .that(spans.size)
            .isEqualTo(4)

        spans[0].onClick(android.view.View(context))
        assertWithMessage("Le premier lien doit être 'saumon'")
            .that(clickedWords.last())
            .isEqualTo("saumon")

        spans[1].onClick(android.view.View(context))
        assertWithMessage("Le second lien doit être 'poulet'")
            .that(clickedWords.last())
            .isEqualTo("poulet")
    }
}
