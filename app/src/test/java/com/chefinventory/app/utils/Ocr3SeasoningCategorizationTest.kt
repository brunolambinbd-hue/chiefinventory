package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau B (Phase A - Étape 3 Categorizer) vérifiant que les tournures
 * d'assaisonnements brutes sans quantité ('du thym et du romarin frais', 'poivre du moulin', 'sel de Guérande')
 * sont correctement classées dans rawIngredientsList dès l'Étape 3.
 */
class Ocr3SeasoningCategorizationTest {

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
    fun testCategorize_SeasoningPhrases_AreCategorizedIntoRawIngredientsListAtStep3() {
        val cleanedLines = listOf(
            "Mélangez l'huile d'olive, le piment, le thym et le romarin.",
            "Ingrédients pour 4 personnes",
            "180 g de feta",
            "1/2 verre d'huile d'olive",
            "10 olives vertes dénoyautées",
            "10 olives noires dénoyautées",
            "1 petit piment",
            "1 gousse d'ail",
            "1 baguette de pain gris",
            "du thym et du romarin frais",
            "sel et poivre du moulin"
        )

        val result = Ocr3Categorizer.categorize(cleanedLines, vocabulary)

        assertWithMessage("La liste des ingrédients bruts à l'Étape 3 doit contenir 'du thym et du romarin frais'")
            .that(result.rawIngredientsList)
            .contains("du thym et du romarin frais")

        assertWithMessage("La liste des ingrédients bruts à l'Étape 3 doit contenir 'sel et poivre du moulin'")
            .that(result.rawIngredientsList)
            .contains("sel et poivre du moulin")

        assertWithMessage("La ligne 'du thym et du romarin frais' ne doit pas fuiter dans les instructions brutes")
            .that(result.rawInstructionsList)
            .doesNotContain("du thym et du romarin frais")
    }
}
