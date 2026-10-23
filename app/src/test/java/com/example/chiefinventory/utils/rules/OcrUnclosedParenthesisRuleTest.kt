package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrUnclosedParenthesisRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe with unclosed parenthesis across two lines should keep ingredient in ingredients section and merge lines`() {
        val ocrText = """
            RECETTE DU CITRON
            INGRÉDIENTS
            1 citron (1/2 pour le jus -
            1/2 pour la décoration)
            200 g de farine
            INSTRUCTIONS
            Pressez le demi citron.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        // Validation A : L'ingrédient doit être fusionné sur une seule ligne complète
        assertThat(ing).contains("1 citron (1/2 pour le jus - 1/2 pour la décoration)")

        // Validation B : L'ingrédient ne doit pas baver dans les instructions
        assertThat(inst).doesNotContain("décoration")
    }
}
