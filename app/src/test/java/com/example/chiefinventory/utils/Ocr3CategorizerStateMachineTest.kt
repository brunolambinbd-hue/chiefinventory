package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement global de la machine à états [Ocr3Categorizer].
 */
class Ocr3CategorizerStateMachineTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie la séparation propre d'une recette basique entre ingrédients, instructions et méta-données.
     */
    @Test
    fun `categorize should classify lines into ingredients and instructions sections`() {
        val input = listOf(
            "ingrédients pour 4 personnes",
            "200 g de farine",
            "100 g de sucre",
            "sel et poivre",
            "• Mélangez la farine et le sucre.",
            "• Enfournez pendant 20 min."
        )

        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertThat(result.detectedServings).isEqualTo("4")
        assertThat(result.rawIngredientsList).containsExactly("200 g de farine", "100 g de sucre", "sel et poivre").inOrder()
        assertThat(result.rawInstructionsList).containsExactly("Mélangez la farine et le sucre.", "Enfournez pendant 20 min.").inOrder()
    }
}
