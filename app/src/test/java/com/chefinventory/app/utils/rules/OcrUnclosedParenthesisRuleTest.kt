package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("L'ingrédient découpé avec parenthèse non refermée doit être fusionné")
            .that(ing)
            .contains("1 citron (1/2 pour le jus - 1/2 pour la décoration)")

        assertWithMessage("La suite de la parenthèse 'décoration' ne doit pas fuiter dans les instructions")
            .that(inst)
            .doesNotContain("décoration")
    }
}
