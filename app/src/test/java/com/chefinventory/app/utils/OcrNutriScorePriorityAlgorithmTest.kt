package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant l'algorithme Nutri-Score en 3 priorités :
 * Priorité 1 (Analyse Bitmap 2D), Priorité 2 (Fallback Texte "NUTRI-SCORE B"), Priorité 3 (Null).
 */
class OcrNutriScorePriorityAlgorithmTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPriority2_ExplicitTextGrade_ExtractsGradeAndPurgesText() {
        val rawOcrText = """
            TORTILLAS DE QUINOA
            15 MIN DE PRÉPARATION + 15 MIN DE CUISSON FACILE
            NUTRI-SCORE B
            INGRÉDIENTS
            100 g de quinoa
            INSTRUCTIONS
            • Préparez la pâte.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("Le Nutri-Score texte 'NUTRI-SCORE B' doit extraire le grade 'B'")
            .that(result.nutriScore)
            .isEqualTo("B")

        val instructions = result.instructions.orEmpty()
        assertWithMessage("Les instructions ne doivent PAS contenir le texte Nutri-Score 'NUTRI-SCORE B'")
            .that(instructions)
            .doesNotContain("NUTRI-SCORE")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testStandaloneNutriScoreHeader_IsPurgedFromInstructions() {
        val rawOcrText = """
            SOUPE VERTE
            15 MIN FACILE
            NUTRI-SCORE
            INGRÉDIENTS
            1 poireau
            INSTRUCTIONS
            • Épluchez le poireau.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        val instructions = result.instructions.orEmpty()
        assertWithMessage("L'en-tête 'NUTRI-SCORE' seul doit être purgé et ne pas figurer dans les instructions")
            .that(instructions)
            .doesNotContain("NUTRI-SCORE")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPriority3_NoNutriScoreGrade_ReturnsNull() {
        val rawOcrText = """
            SALADE DE TOMATES
            10 MIN FACILE
            INGRÉDIENTS
            4 tomates
            INSTRUCTIONS
            • Coupez les tomates.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("En l'absence de Nutri-Score, le champ doit être null")
            .that(result.nutriScore)
            .isNull()
    }
}
