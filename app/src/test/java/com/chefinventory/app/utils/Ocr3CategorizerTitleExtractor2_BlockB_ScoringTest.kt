package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("La ligne 'Salade de tomates' doit être qualifiée comme candidat titre valide")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates", vocabulary, null))
            .isTrue()

        // Disqualified by length/single-word without title keyword (< 2 words)
        assertWithMessage("Le mot unique 'Petit' (< 2 mots) doit être disqualifié")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Petit", vocabulary, null))
            .isFalse()

        // Disqualified by ending punctuation (., !, ?)
        assertWithMessage("Une ligne se terminant par un point '.' doit être disqualifiée")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates.", vocabulary, null))
            .isFalse()

        assertWithMessage("Une ligne se terminant par un point d'exclamation '!' doit être disqualifiée")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates!", vocabulary, null))
            .isFalse()

        assertWithMessage("Une ligne se terminant par un point d'interrogation '?' doit être disqualifiée")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates?", vocabulary, null))
            .isFalse()

        // Disqualified by invalid characters "("
        assertWithMessage("Une ligne contenant une parenthèse '(' doit être disqualifiée")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade (de tomates)", vocabulary, null))
            .isFalse()

        // Disqualified by quantity
        assertWithMessage("Une ligne contenant une quantité '200 g de tomates' doit être disqualifiée")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("200 g de tomates", vocabulary, null))
            .isFalse()

        // Disqualified by excluded keywords (length >= 5 and >= 2 words so not caught by length/single-word check)
        val customVocab = vocabulary.copy(excludedOcrKeywords = listOf("publicité commerciale"))
        assertWithMessage("Une ligne contenant le mot d'exclusion 'publicité commerciale' doit être disqualifiée")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Publicité commerciale", customVocab, null))
            .isFalse()

        // Empty excluded keywords
        val emptyExcludedVocab = vocabulary.copy(excludedOcrKeywords = emptyList())
        assertWithMessage("Un candidat valide sans mots d'exclusion doit être conservé")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade de tomates", emptyExcludedVocab, null))
            .isTrue()

        // Disqualified by personnes/portions (not starting with "pour ")
        assertWithMessage("La mention 'Recette 4 personnes' doit être disqualifiée des candidats titres")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Recette 4 personnes", vocabulary, null))
            .isFalse()

        assertWithMessage("La mention 'Recette 4 portions' doit être disqualifiée des candidats titres")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Recette 4 portions", vocabulary, null))
            .isFalse()

        // Disqualified by letter/digit counts
        assertWithMessage("Une ligne 'Ab 12' avec trop peu de lettres doit être disqualifiée")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Ab 12", vocabulary, null))
            .isFalse()

        assertWithMessage("Une ligne 'Salade 123' avec des nombres parasites doit être disqualifiée")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Salade 123", vocabulary, null))
            .isFalse()
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

        assertWithMessage("Le score d'un candidat titre complet doit être supérieur à 50")
            .that(score)
            .isGreaterThan(50)

        assertWithMessage("Le détail explicatif du score doit mentionner les mots-clés de titre")
            .that(details)
            .contains("mots_clés_titre")

        assertWithMessage("Le détail explicatif du score doit mentionner les connecteurs de titre")
            .that(details)
            .contains("connecteurs_titre")

        assertWithMessage("Le détail explicatif du score doit mentionner le bonus de premier ingrédient")
            .that(details)
            .contains("premier_ingrédient")
    }
}
