package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant que le symbole '+' de tolérance/approximation devant une durée ou quantité
 * ("+ 1/2 heures", "+ 10 min", "+ 100 g") est 100% purgé à l'Étape 1.
 */
class Ocr1PlusSymbolToleranceTest {

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
    fun testCorrect_PlusSymbolBeforeFractionOrDuration_CleansPlusSymbol() {
        val input = "Laissez mariner au réfrigérateur + 1/2 heures"
        val corrected = Ocr1Corrector.correct(input, vocabulary.ocrSpellingRepairs)

        assertWithMessage("Le symbole '+' devant '1/2 heures' doit être nettoyé")
            .that(corrected)
            .isEqualTo("Laissez mariner au réfrigérateur 1/2 heures")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_FullRecipeWithPlusSymbolTolerance_CleansInstructions() {
        val rawOcrText = """
            Brochettes de poulet
            INGRÉDIENTS
            200 g de poulet
            INSTRUCTIONS
            • Laissez mariner au réfrigérateur + 1/2 heures.
            • Cuisez + 10 min au barbecue.
        """.trimIndent()

        val parsed = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("L'instruction ne doit PAS contenir le symbole '+' de tolérance")
            .that(parsed.instructions.orEmpty())
            .doesNotContain("+ 1/2")

        assertWithMessage("L'instruction doit contenir '1/2 heures' propre")
            .that(parsed.instructions.orEmpty())
            .contains("1/2 heures.")
    }
}
