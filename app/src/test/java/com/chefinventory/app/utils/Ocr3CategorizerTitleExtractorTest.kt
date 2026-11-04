package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement, le scoring et l'extraction
 * du titre principal des recettes par [Ocr3CategorizerTitleExtractor].
 */
class Ocr3CategorizerTitleExtractorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExhaustiveOcrTitleExtractorBranchCoverage() {
        // 1. Empty stepActionKeywords and no-ingredient lines (L59, L63)
        val emptyActionsVocab = vocabulary.copy(stepActionKeywords = emptyList())
        val noIngLines = listOf("Salade grecque aux olives", "Mélanger et servir")
        val resNoIng = Ocr3CategorizerTitleExtractor.extractTitle(noIngLines, emptyActionsVocab)
        assertThat(resNoIng.title).contains("Salade grecque aux olives")

        // 2. Attached title after "sel et poivre" with connector " de" (L80)
        val attachedTitleLines = listOf(
            "1 oignon sel et poivre Tarte de",
            "pommes fraîches"
        )
        val resAttached = Ocr3CategorizerTitleExtractor.extractTitle(attachedTitleLines, vocabulary)
        assertThat(resAttached.title).contains("Tarte de pommes fraîches")

        // 3. Orphan noun completions with "petite", "grande", "façon" (L165-174)
        val fasonLines = listOf("Poissons façon", "meunière", "200 g de poisson")
        val resFason = Ocr3CategorizerTitleExtractor.extractTitle(fasonLines, vocabulary)
        assertThat(resFason.title).contains("meunière")

        // 4. Score penalty for ending period (L366-367)
        val periodLines = listOf("Tarte aux pommes.", "Tarte aux pommes fraîches")
        val resPeriod = Ocr3CategorizerTitleExtractor.extractTitle(periodLines, vocabulary)
        assertThat(resPeriod.title).isEqualTo("Tarte aux pommes fraîches")

        // 5. Disqualifying guards: "portions", trailing stop words, digit limits (L472, L478, L480)
        val disqLines = listOf(
            "Pour 4 portions",
            "Tarte de",
            "Recette 123456",
            "Tarte Tatin"
        )
        val resDisq = Ocr3CategorizerTitleExtractor.extractTitle(disqLines, vocabulary)
        assertThat(resDisq.title).isEqualTo("Tarte Tatin")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOrphanNounCompletion_FeminineAndMasculineVariations() {
        // 1. Feminine adjective + orphan noun
        val linesFeminine = listOf(
            "Tarte aux pommes et sa grande",
            "salade",
            "200 g de farine"
        )
        val resultFem = Ocr3CategorizerTitleExtractor.extractTitle(linesFeminine, vocabulary)
        assertThat(resultFem.title).contains("salade")

        // 2. Disqualified lines with parentheses or bullets
        val linesDisqualified = listOf(
            "Salade (4 personnes)",
            "• Salade grecque",
            "- Soupe à l'oignon",
            "Tarte aux cerises"
        )
        val resultDisq = Ocr3CategorizerTitleExtractor.extractTitle(linesDisqualified, vocabulary)
        assertThat(resultDisq.title).isEqualTo("Tarte aux cerises")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFindFirstIngredientWord_DirectAndDefaultParameterOverloads() {
        // 1. Default parameter overload (vocabulary = null)
        val word1 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(listOf("250 g de farine"))
        assertThat(word1).isEqualTo("farine")

        // 2. Explicit vocabulary overload
        val word2 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(listOf("1/2 l d'eau"), vocabulary)
        assertThat(word2).isEqualTo("eau")

        // 3. Empty list -> returns null
        val word3 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(emptyList(), vocabulary)
        assertThat(word3).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsMultiLineTitleConnector_ExhaustiveOverloads() {
        // 1. Default parameter overload (vocabulary = null)
        assertThat(Ocr3CategorizerTitleExtractor.isMultiLineTitleConnector("au jambon")).isTrue()
        assertThat(Ocr3CategorizerTitleExtractor.isMultiLineTitleConnector("salade verte")).isFalse()

        // 2. Explicit vocabulary overload
        assertThat(Ocr3CategorizerTitleExtractor.isMultiLineTitleConnector("aux crevettes", vocabulary)).isTrue()
        assertThat(Ocr3CategorizerTitleExtractor.isMultiLineTitleConnector("soupe à l'oignon", vocabulary)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_AttachedTitleAfterSelEtPoivreMultiLine() {
        val lines = listOf(
            "2 gousses d'ail sel et poivre Salade d'avocat et",
            "princesses aux crevettes"
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary)

        assertThat(result.title).contains("Salade d'avocat et princesses aux crevettes")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_TitleWithEndingPeriodScorePenalty() {
        val lines = listOf(
            "Salade de crevettes.",
            "200 g de crevettes"
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary)

        assertThat(result.title).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_EmptyMultilineConnectorsFallback() {
        val emptyConnectorsVocab = vocabulary.copy(titleMultilineConnectors = emptyList())
        val lines = listOf(
            "Salade de fruits",
            "au sirop d'érable",
            "100 g de pommes"
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, emptyConnectorsVocab)

        assertThat(result.title).isEqualTo("Salade de fruits au sirop d'érable")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_EmptyLinesAndNoValidCandidates() {
        // 1. List with empty strings
        val linesWithEmpty = listOf("", "Salade de tomates", "")
        val resEmpty = Ocr3CategorizerTitleExtractor.extractTitle(linesWithEmpty, vocabulary)
        assertThat(resEmpty.title).isEqualTo("Salade de tomates")

        // 2. Candidates with score <= 0
        val linesLowScore = listOf("10. Etape 1")
        val resLowScore = Ocr3CategorizerTitleExtractor.extractTitle(linesLowScore, vocabulary)
        assertThat(resLowScore.title).isNull()

        // 3. Completely empty list
        val resNoCandidates = Ocr3CategorizerTitleExtractor.extractTitle(emptyList(), vocabulary)
        assertThat(resNoCandidates.title).isNull()
    }

    /**
     * Vérifie l'extraction complète d'un titre multi-lignes relié par une préposition et réparé orthographiquement.
     */
    @Test
    @CoveredRequirement("6.1.1.9")
    fun `extractTitle should extract two line title connected by au and post process spelling repairs`() {
        val lines = listOf(
            "INGRÉDIENTS",
            "1 melon Galia",
            "8 tranches de jambon",
            "sel et poivre",
            "Büchettes de melon",
            "au jambon italien et au porto",
            "• Pelez le melon et servez."
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary)

        assertThat(result.title).isEqualTo("Bûchettes de melon au jambon italien et au porto")
    }

    /**
     * Vérifie que les lignes de consignes d'instructions contenant des verbes d'action ou se terminant par des connecteurs suspendus (ex: "Maintenez... puis") sont disqualifiées.
     */
    @Test
    @CoveredRequirement("6.1.1.9")
    fun `extractTitle should disqualify lines containing action verbs or ending with hanging connectors like puis`() {
        val lines = listOf(
            "Maintenez une petite ébullition pendant 3 mn puis",
            "Fondue à la bière",
            "1. Verser la bière et mélanger."
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary)

        assertThat(result.title).isEqualTo("Fondue à la bière")
    }
}
