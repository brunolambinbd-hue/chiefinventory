package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant le rattrapage prioritaire Data-Driven des phrases d'ingrédients
 * définies dans [ocr_step3_priority_ingredients.xml] (ex: 'du thym et du romarin frais', 'beurre pour le moule').
 */
class Ocr3PriorityIngredientsTest {

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
    fun testCategorize_PriorityIngredientPhrases_ArePulledBackToIngredientsSection() {
        val cleanedLines = listOf(
            "Mélangez le thym et le romarin dans la poêle.",
            "INGRÉDIENTS",
            "200 g de farine",
            "sel et poivre",
            "du thym et du romarin frais",
            "beurre pour le moule",
            "INSTRUCTIONS",
            "• Versez la pâte dans le moule."
        )

        val result = Ocr3Categorizer.categorize(cleanedLines, vocabulary)

        assertWithMessage("La section ingrédients doit contenir 'du thym et du romarin frais'")
            .that(result.rawIngredientsList)
            .contains("du thym et du romarin frais")

        assertWithMessage("La section ingrédients doit contenir 'beurre pour le moule'")
            .that(result.rawIngredientsList)
            .contains("beurre pour le moule")

        assertWithMessage("La ligne 'beurre pour le moule' ne doit pas fuiter dans les instructions")
            .that(result.rawInstructionsList)
            .doesNotContain("beurre pour le moule")
    }
}
