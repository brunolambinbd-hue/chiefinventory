package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 4 (Fusion des lignes [Ocr4Merger])
 * pour la recette des Canapés grecs.
 */
class RecipeOcrCanapesGrecsPhase4Test {

    private val rawIngredientsList = listOf(
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

    private val rawInstructionsList = listOf(
        "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-",
        "cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur 1/2 heures.",
        "Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les",
        "d'ail, égouttez le fromage et placez-le sur le pain.",
        "Décorez d'olives coupées en deux et de romarin."
    )

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    private fun assertLinesMatch(actual: List<String>, expected: List<String>, description: String) {
        if (actual != expected) {
            val message = "$description :\n\n" +
                "${OcrTestStrings.HEADER_EXPECTED} (${expected.size} lines) :\n" +
                expected.mapIndexed { i, s -> "  [$i] $s" }.joinToString("\n") + "\n\n" +
                "${OcrTestStrings.HEADER_ACTUAL} (${actual.size} lines) :\n" +
                actual.mapIndexed { i, s -> "  [$i] $s" }.joinToString("\n")
            fail(message)
        }
    }

    @Test
    fun test4_Phase4_Merging() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val mergedIngredients = Ocr4Merger.mergeIngredients(
            rawIngredientsList,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )

        val mergedInstructions = Ocr4Merger.mergeInstructions(rawInstructionsList)

        val expectedMergedIngredients = listOf(
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

        val expectedMergedInstructions = listOf(
            "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pincée de sel et placez-y le fromage découpé en tranches.",
            "Laissez mariner au réfrigérateur 1/2 heures.",
            "Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les d'ail, égouttez le fromage et placez-le sur le pain.",
            "Décorez d'olives coupées en deux et de romarin."
        )

        assertLinesMatch(mergedIngredients, expectedMergedIngredients, "Étape 4 : Ingrédients fusionnés défaillants")
        assertLinesMatch(mergedInstructions, expectedMergedInstructions, "Étape 4 : Instructions fusionnées défaillantes")
    }
}
