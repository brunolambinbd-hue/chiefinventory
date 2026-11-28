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

        // Disqualified by sesame without accent and category header without connectors
        assertWithMessage("Le mot 'sesame' sans accent doit être disqualifié")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("sesame", vocabulary, null))
            .isFalse()

        val categoryVocab = vocabulary.copy(recipeCategoryHeaders = listOf("entrée | 1"))
        assertWithMessage("L'en-tête de catégorie sans connecteur 'Entrée froide' doit être disqualifié")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Entrée froide", categoryVocab, null))
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

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateScore_2DFontHeightBranchesAndFuzzyTitleKeywords() {
        val line2DLarge = OcrTextLine("Salade de tomates", top = 100, bottom = 250, left = 50, right = 400)
        val (_, detailsLarge) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(
            candidate = "Salade de tomates",
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("tomates"),
            firstIngredientWord = "tomates",
            lines2D = listOf(line2DLarge)
        )
        assertWithMessage("Large font 2D line (150px) gives +60 pts").that(detailsLarge).contains("hauteur_police_titre_2d")

        val line2DSmall = OcrTextLine("Salade de tomates", top = 100, bottom = 150, left = 50, right = 400)
        val (_, detailsSmall) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(
            candidate = "Salade de tomates",
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("tomates"),
            firstIngredientWord = "tomates",
            lines2D = listOf(line2DSmall)
        )
        assertWithMessage("Small font 2D line (50px) does not give font height bonus").that(detailsSmall).doesNotContain("hauteur_police_titre_2d")

        val line2DUnmatched = OcrTextLine("Unrelated 2D Line", top = 100, bottom = 250, left = 50, right = 400)
        val (_, detailsUnmatched) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(
            candidate = "Salade de tomates",
            vocabulary = vocabulary,
            recipeIngredientWords = setOf("tomates"),
            firstIngredientWord = "tomates",
            lines2D = listOf(line2DUnmatched)
        )
        assertWithMessage("Unmatched 2D line does not give 2D bonus").that(detailsUnmatched).doesNotContain("hauteur_police_titre_2d")

        val customVocab = vocabulary.copy(titleKeywords = listOf("tarte"))
        val (_, detailsFuzzy) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(
            candidate = "Tartes aux pommes",
            vocabulary = customVocab,
            recipeIngredientWords = setOf("pommes"),
            firstIngredientWord = "pommes"
        )
        assertWithMessage("Fuzzy title keyword 'tartes' -> 'tarte' gives +20 pts").that(detailsFuzzy).contains("mots_clés_titre_flou_lettrine")

        val categoryVocab = vocabulary.copy(recipeCategoryHeaders = listOf("entrée | 1"))
        assertWithMessage("Category header with > 3 words is not disqualified")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("Entrée froide de salade de tomates", categoryVocab, null))
            .isTrue()

        assertWithMessage("Disqualifying number start '10. Etape' is disqualified")
            .that(Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate("10. Etape 1", vocabulary, null))
            .isFalse()
    }
}
