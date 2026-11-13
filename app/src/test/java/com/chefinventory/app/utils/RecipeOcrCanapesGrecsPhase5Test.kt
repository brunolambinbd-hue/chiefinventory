package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 5 (Embellissement [Ocr5Beautifier])
 * pour la recette des Canapés grecs.
 */
class RecipeOcrCanapesGrecsPhase5Test {

    private val mergedIngredients = listOf(
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

    private val mergedInstructions = listOf(
        "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pincée de sel et placez-y le fromage découpé en tranches.",
        "Laissez mariner au réfrigérateur 1/2 heures.",
        "Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les d'ail, égouttez le fromage et placez-le sur le pain.",
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
    fun test5_Phase5_Beautification() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val finalIngredients = Ocr5BeautifierIngredient.beautifyIngredients(
            mergedIngredients,
            vocabulary.baseIngredientsExpectingVariety,
            vocabulary.ingredientVarietyQualifiers
        )

        val finalInstructions = Ocr5BeautifierInstruction.beautifyInstructions(
            mergedInstructions,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors,
            vocabulary.culinaryMeasurementUnits,
            vocabulary.ocrSpellingRepairs
        )

        val expectedFinalIngredients = listOf(
            "180 g de feta",
            "1/2 verre d'huile d'olive",
            "10 olives vertes dénoyautées",
            "10 olives noires dénoyautées",
            "1 petit piment",
            "1 gousse d'ail",
            "1 baguette de pain gris",
            "Du thym et du romarin frais",
            "Sel et poivre du moulin"
        )

        val expectedFinalInstructions = listOf(
            "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pincée de sel et placez-y le fromage découpé en tranches laissez mariner au réfrigérateur 1/2 heures.",
            "Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les d'ail, égouttez le fromage et placez-le sur le pain.",
            "Décorez d'olives coupées en deux et de romarin."
        )

        assertLinesMatch(finalIngredients, expectedFinalIngredients, "Étape 5 : Ingrédients embellis défaillants")
        assertLinesMatch(finalInstructions, expectedFinalInstructions, "Étape 5 : Instructions embellies défaillantes")
    }
}
