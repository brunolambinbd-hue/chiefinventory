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
 * Test unitaire TDD d'isolation vérifiant le formatage hypertexte des mots d'ingrédients dans [OcrIngredientSpannableFormatter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrIngredientSpannableFormatterTest {

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
    fun testFormatIngredientHyperlinks_AttachesClickableSpansToCoreIngredientWords() {
        val fullText = """
            250 g de crevettes grises décortiquées
            4 petites tomates Roma
            1/2 verre d'huile d'olive
        """.trimIndent()

        val offTaxonomy = listOf("crevettes", "tomates", "huile d'olive")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        assertWithMessage("Le résultat formaté ne doit pas être vide")
            .that(spannable.toString())
            .isEqualTo(fullText)

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)
        assertWithMessage("Des liens hypertextes cliquables doivent être créés pour les mots noyaux d'ingrédients")
            .that(spans.size)
            .isAtLeast(3)

        // Simuler le clic sur le premier lien hypertexte
        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le premier clic hypertexte doit capturer le mot noyau d'ingrédient 'crevettes'")
            .that(clickedWords)
            .containsExactly("crevettes")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_IgnoresAdjectivesAndPluralizesMultipleIngredientsOnSameLine() {
        val fullText = """
            200 g d'épinards frais
            3 jaunes d'œufs
            Sel et poivre
        """.trimIndent()

        val offTaxonomy = listOf("épinards", "oeufs", "œufs", "sel", "poivre")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        spans[0].onClick(android.view.View(context))
        assertWithMessage("Le mot noyau 'épinards' (pas l'adjectif 'frais') doit être capturé sur la ligne 1")
            .that(clickedWords.last())
            .isEqualTo("épinards")

        spans[1].onClick(android.view.View(context))
        assertWithMessage("Le mot noyau 'œufs' (pas l'adjectif 'jaunes') doit être capturé sur la ligne 2")
            .that(clickedWords.last())
            .isEqualTo("œufs")

        spans[2].onClick(android.view.View(context))

        assertWithMessage("La ligne 'Sel et poivre' (présente dans le dictionnaire des assaisonnements) doit générer un lien unifié 'Sel et poivre'")
            .that(clickedWords.last())
            .isEqualTo("Sel et poivre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_CreatesSingleContiguousSpanForCompoundIngredients() {
        val fullText = "1 c. à soupe de vinaigre de vin blanc"
        val offTaxonomy = listOf("vinaigre de vin blanc", "vinaigre de vin", "vinaigre")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Un seul lien hypertexte continu doit être créé pour l'ingrédient composé 'vinaigre de vin blanc'")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))
        assertWithMessage("Le clic hypertexte doit capturer l'expression complète 'vinaigre de vin blanc'")
            .that(clickedWords.last())
            .isEqualTo("vinaigre de vin blanc")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_ExcludesAdjectivesCréméAndSucréFromBeingClickable() {
        val fullText = "8 c. à soupe de yaourt demi-écrémé sucré"
        val offTaxonomy = listOf("yaourt", "écrémé", "sucré", "demi-écrémé")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Seul l'aliment noyau 'yaourt' doit être cliquable (pas 'demi-écrémé' ni 'sucré')")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le lien hypertexte doit capturer uniquement l'ingrédient 'yaourt'")
            .that(clickedWords.last())
            .isEqualTo("yaourt")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_ExcludesContainerUnitBoiteFromBeingConvertedToBoniteOrClickable() {
        val fullText = "1 boîte de crabe"
        val offTaxonomy = listOf("crabe", "bonite")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("L'unité conteneur 'boîte' ne doit JAMAIS être cliquable ni convertie en 'bonite'")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le lien hypertexte doit capturer uniquement l'aliment noyau 'crabe'")
            .that(clickedWords.last())
            .isEqualTo("crabe")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_ExcludesIndefiniteArticleUneFromBeingClickable() {
        val fullText = "une gousse d'ail"
        val offTaxonomy = listOf("ail")
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("L'article indéfini 'une' et le conteneur 'gousse' ne doivent JAMAIS être cliquables")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le lien hypertexte doit capturer uniquement l'aliment noyau 'ail'")
            .that(clickedWords.last())
            .isEqualTo("ail")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_PluralTolerantMatching_UnifiesGrainesDeSesameIntoSingleContiguousSpan() {
        val fullText = "1 c. à soupe de graines de sésame"
        val offTaxonomy = listOf("graine de sésame") // Entrée canonique singulier Open Food Facts
        val clickedWords = mutableListOf<String>()

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy,
            onIngredientClick = { clickedWords.add(it) }
        )

        val spans = spannable.getSpans(0, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("L'entrée singulier 'graine de sésame' d'Open Food Facts doit fusionner le pluriel 'graines de sésame' en 1 seul lien hypertexte continu")
            .that(spans.size)
            .isEqualTo(1)

        spans[0].onClick(android.view.View(context))

        assertWithMessage("Le lien hypertexte doit capturer l'expression complète 'graines de sésame'")
            .that(clickedWords.last())
            .isEqualTo("graines de sésame")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatIngredientHyperlinks_IngredientSubHeaderPourLaSauce_HasZeroClickableLinks() {
        val fullText = """
            Pour la sauce
            4 c. à soupe de jus d'orange
        """.trimIndent()
        val offTaxonomy = listOf("sauce", "jus d'orange", "orange")

        val spannable = OcrIngredientSpannableFormatter.formatIngredientHyperlinks(
            context = context,
            fullText = fullText,
            vocabulary = vocabulary,
            offTaxonomyList = offTaxonomy
        )

        val lines = fullText.split("\n")
        val line0 = lines[0] // "Pour la sauce"

        val spans0 = spannable.getSpans(0, line0.length, android.text.style.ClickableSpan::class.java)
        val spans1 = spannable.getSpans(line0.length + 1, spannable.length, android.text.style.ClickableSpan::class.java)

        assertWithMessage("Le sous-titre d'ingrédients 'Pour la sauce' ne doit comporter AUCUN lien hypertexte cliquable")
            .that(spans0.size)
            .isEqualTo(0)

        assertWithMessage("La ligne d'ingrédient '4 c. à soupe de jus d'orange' doit comporter son lien hypertexte 'jus d'orange'")
            .that(spans1.size)
            .isEqualTo(1)
    }
}
