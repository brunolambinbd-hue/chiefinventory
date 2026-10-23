package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et les règles de beautification d'ingrédients de [Ocr5IngredientBeautifier].
 */
class Ocr5IngredientBeautifierTest {

    /**
     * Vérifie que la première lettre de chaque ingrédient est forcée en majuscule.
     */
    @Test
    fun `beautifyIngredients should capitalize first letter of each ingredient`() {
        val input = listOf("sel", "poivre du moulin", "2 carottes")
        val expected = listOf("Sel", "Poivre du moulin", "2 carottes")

        val result = Ocr5IngredientBeautifier.beautifyIngredients(input)
        assertThat(result).containsExactlyElementsIn(expected).inOrder()
    }

    /**
     * Vérifie le filtrage et l'élimination des en-têtes résiduels de section ingrédients.
     */
    @Test
    fun `beautifyIngredients should filter out residual section headers`() {
        val input = listOf("INGRÉDIENTS:", "200 g de farine", "composition:")
        val expected = listOf("200 g de farine")

        val result = Ocr5IngredientBeautifier.beautifyIngredients(input)
        assertThat(result).containsExactlyElementsIn(expected)
    }

    /**
     * Vérifie la fusion des qualificatifs de variétés orphelines réparties sur deux lignes ("haricots" + "princesses").
     */
    @Test
    fun `beautifyIngredients should merge orphan variety qualifiers using vocabulary`() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val input = listOf("200 g de haricots", "princesses")

        val result = Ocr5IngredientBeautifier.beautifyIngredients(
            input,
            vocabulary.baseIngredientsExpectingVariety,
            vocabulary.ingredientVarietyQualifiers
        )

        assertThat(result).containsExactly("200 g de haricots princesses")
    }
}
