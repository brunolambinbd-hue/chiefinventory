package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation (Correction 1) vérifiant que toute ligne contenant des lettres hors A-E
 * (ex: '•h céleri-rave') est STRICTEMENT EXCLUE du filtrage Nutri-Score et conservée comme ingrédient.
 */
class OcrNutriScoreNonGradeLetterTest {

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
    fun testNutriScoreFilter_LineWithNonGradeLetters_IsNotEvaluatedAndKeptAsIngredient() {
        val rawOcrText = """
            SOUPE AU CÉLERI-RAVE
            NUTRI-SCORE
            INGRÉDIENTS
            •h céleri-rave
            • 2 oignons
            INSTRUCTIONS
            • Épluchez le céleri-rave.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)
        val ingredients = result.ingredients?.split("\n") ?: emptyList()

        assertWithMessage("La ligne '•h céleri-rave' contient la lettre 'h' (hors A-E) et DOIT figurer dans les ingrédients")
            .that(ingredients)
            .contains("1/2 céleri-rave")
    }
}
