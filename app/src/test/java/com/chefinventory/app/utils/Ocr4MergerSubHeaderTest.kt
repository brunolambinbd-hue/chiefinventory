package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant que les sous-en-têtes d'ingrédients ("Pour la sauce", "Pour la vinaigrette")
 * ne sont JAMAIS fusionnés avec la ligne d'ingrédient précédente ("sel et poivre").
 */
class Ocr4MergerSubHeaderTest {

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
    fun testMergeIngredients_DoesNotMergeSubHeaderWithPreviousIngredient() {
        val inputLines = listOf(
            "sel et poivre",
            "Pour la sauce",
            "1 c. à soupe de moutarde"
        )

        val merged = Ocr4Merger.mergeIngredients(
            inputLines,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )

        assertWithMessage("Le sous-en-tête 'Pour la sauce' ne doit pas fusionner avec 'sel et poivre'")
            .that(merged)
            .containsExactly(
                "sel et poivre",
                "Pour la sauce",
                "1 c. à soupe de moutarde"
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_DoesNotMergeSubHeaderWithColonWithPreviousIngredient() {
        val inputLines = listOf(
            "200 g de farine",
            "Pour la garniture :",
            "100 g de sucre"
        )

        val merged = Ocr4Merger.mergeIngredients(
            inputLines,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )

        assertWithMessage("Le sous-en-tête 'Pour la garniture :' ne doit pas fusionner avec '200 g de farine'")
            .that(merged)
            .containsExactly(
                "200 g de farine",
                "Pour la garniture",
                "100 g de sucre"
            )
            .inOrder()
    }
}
