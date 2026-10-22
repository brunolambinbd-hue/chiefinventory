package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrCuillereSoupeRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe should merge 1 cuillere a soupe and de moutarde into single full ingredient`() {
        val ocrText = """
            Salade de poisson
            INGRÉDIENTS
            1 cuillère à soupe
            de moutarde
            200 g de saumon
            sel et poivre
            • Mélangez la moutarde avec le saumon.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        // Validation : "1 cuillère à soupe" + "de moutarde" -> "1 cuillère à soupe de moutarde"
        assertThat(ing).ignoringCase().contains("1 cuillère à soupe de moutarde")
        assertThat(ing).doesNotContain("1 cuillère à soupe\n")
    }
}
