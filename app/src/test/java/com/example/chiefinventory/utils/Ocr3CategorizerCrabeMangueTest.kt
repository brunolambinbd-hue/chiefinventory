package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class Ocr3CategorizerCrabeMangueTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `categorize should capture des petites feuilles de chicon and 1 branche d aneth in rawIngredientsList`() {
        val input = listOf(
            "ingrédients pour 4 personnes",
            "100 g de chair de crabe",
            "2 mangues",
            "1 c. à café de moutarde",
            "2 c. à soupe de yaourt grec",
            "1 jaune d'oeuf",
            "5 cl d'huile",
            "1 pincée de curcuma",
            "1 c. à dessert de rhum",
            "poivre de Cayenne, sel, poivre",
            "des petites feuilles de chicon",
            "1 branche d'aneth"
        )

        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertThat(result.rawIngredientsList).contains("des petites feuilles de chicon")
        assertThat(result.rawIngredientsList).contains("1 branche d'aneth")
        assertThat(result.detectedServings).isEqualTo("4")
    }
}
