package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'intégration anti-régression de niveau A (Pipeline Complet [RecipeOcrParser])
 * dédiée spécifiquement à la recette des Canapés grecs.
 */
class RecipeOcrParserCanapesGrecsTest {

    /** Capture brute réelle ML Kit du scan caméra. */
    private val rawOcrScanText = listOf(
        "Saveur de",
        "Danapés grecs",
        "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-",
        "cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur + 12 heures.",
        "Découpez la baguette en tranches d'environ 1 cm, faites-les légèrement griller puis frottez-les",
        "d'ail, égouttez le fromage et placez-le sur le pain.",
        "Décorezd'olives coupées en deux et de romarin.",
        "Ingrédients pour 4 personnes",
        "180 g de feta",
        "1/2 verre d'huile d'olive",
        "10 olives vertes dénoyautées",
        "10 olives noires dénoyautées",
        "I petit piment",
        "I gousse d'ail",
        "I baguette de pain gris",
        "du thym et du romarin frais",
        "sel et poivre du moulin"
    ).joinToString("\n")

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
    fun testCanapesGrecsRecipeExactOcrRawScanParse() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        // 1. Validation du Titre
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .ignoringCase()
            .isEqualTo("Canapés grecs")

        // 2. Validation des Portions / Nombre de Personnes
        assertWithMessage(OcrTestStrings.SERVINGS_MISMATCH)
            .that(result.servings)
            .isEqualTo("4")

        // 3. Validation des Ingrédients (9 éléments complets)
        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()
        val expectedIngredients = listOf(
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
        assertLinesMatch(ingredientLines, expectedIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        // 4. Validation des Instructions (3 consignes réassemblées)
        val instructionLines = result.instructions?.split("\n") ?: emptyList()
        val expectedInstructions = listOf(
            "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pincée de sel et placez-y le fromage découpé en tranches laissez mariner au réfrigérateur 1/2 heures.",
            "Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les d'ail, égouttez le fromage et placez-le sur le pain.",
            "Décorez d'olives coupées en deux et de romarin."
        )
        assertLinesMatch(instructionLines, expectedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
