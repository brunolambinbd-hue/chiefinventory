package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("L'unité seule '1 cuillère à soupe' doit fusionner avec 'de moutarde'")
            .that(ing)
            .ignoringCase()
            .contains("1 cuillère à soupe de moutarde")

        assertWithMessage("L'ingrédient ne doit pas rester tronqué sur une ligne séparée")
            .that(ing)
            .doesNotContain("1 cuillère à soupe\n")
    }
}
