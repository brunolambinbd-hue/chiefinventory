package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr4MergerIngredientSplitter].
 */
class Ocr4MergerIngredientSplitterTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitCombinedIngredients_DefaultArgumentsOverloadsAndBlankLine() {
        val result1 = Ocr4MergerIngredientSplitter.splitCombinedIngredients("200 g de farine", listOf("farine"))
        assertWithMessage("Un ingrédient unique '200 g de farine' ne doit pas être scindé")
            .that(result1)
            .containsExactly("200 g de farine")

        val result2 = Ocr4MergerIngredientSplitter.splitCombinedIngredients("   ", emptyList())
        assertWithMessage("Une ligne d'ingrédients vide doit retourner une liste vide")
            .that(result2)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitCombinedIngredients_ParenthesisAndPrefixNumbers() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("200 g de beurre (doux) 100 g de sucre", listOf("beurre", "sucre"))
        assertWithMessage("Les deux ingrédients combinés avec parenthèses doivent être scindés proprement")
            .that(result)
            .containsExactly("200 g de beurre (doux)", "100 g de sucre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should use data driven modifiers and not split size adjectives`() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients(
            "1 petite salade frisée",
            listOf("salade"),
            preparationModifiers = listOf("petite", "frisée")
        )
        assertWithMessage("L'adjectif de taille 'petite' ne doit pas séparer le nom de l'ingrédient")
            .that(result)
            .containsExactly("1 petite salade frisée")
    }
}
