package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerTitleExtractor2_BlockB_Scoring].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerTitleExtractor2_BlockB_ScoringTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsBaseCandidate_ValidAndDisqualifiedCases() {
        // Valid candidate
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates", vocabulary, null)).isTrue()

        // Disqualified by length/single-word without title keyword (< 2 words)
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Petit", vocabulary, null)).isFalse()

        // Disqualified by ending punctuation (., !, ?)
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates.", vocabulary, null)).isFalse()
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates!", vocabulary, null)).isFalse()
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates?", vocabulary, null)).isFalse()

        // Disqualified by invalid characters "("
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade (de tomates)", vocabulary, null)).isFalse()

        // Disqualified by quantity
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("200 g de tomates", vocabulary, null)).isFalse()

        // Disqualified by excluded keywords (length >= 5 and >= 2 words so not caught by length/single-word check)
        val customVocab = vocabulary.copy(excludedOcrKeywords = listOf("publicité commerciale"))
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Publicité commerciale", customVocab, null)).isFalse()

        // Empty excluded keywords
        val emptyExcludedVocab = vocabulary.copy(excludedOcrKeywords = emptyList())
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates", emptyExcludedVocab, null)).isTrue()

        // Disqualified by personnes/portions (not starting with "pour ")
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Recette 4 personnes", vocabulary, null)).isFalse()
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Recette 4 portions", vocabulary, null)).isFalse()

        // Disqualified by letter/digit counts
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Ab 12", vocabulary, null)).isFalse()
        assertThat(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade 123", vocabulary, null)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateScore_FullCriteriaCalculation() {
        val (score, details) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(
            candidate = "Salade de tomates aux crevettes",
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("tomates", "crevettes"),
            firstIngredientWord = "tomates"
        )

        assertThat(score).isGreaterThan(50)
        assertThat(details).contains("mots_clés_titre")
        assertThat(details).contains("connecteurs_titre")
        assertThat(details).contains("premier_ingrédient")
    }
}
