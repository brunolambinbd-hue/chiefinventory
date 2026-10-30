package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrInstructionConjunctionRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe should merge instruction sentence ending with et dot with next line`() {
        val ocrText = """
            Cocktail de crevettes
            INGRÉDIENTS
            200 g de crevettes
            sel et poivre
            • Nappez de la sauce au yaourt et à la mayonnaise et.
            Garnissez de quelques branches de cerfeuil.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val inst = result.instructions ?: ""

        // Validation : "et." + "Garnissez" doivent être recollés en "et garnissez"
        assertThat(inst).contains("et à la mayonnaise et garnissez de quelques branches de cerfeuil.")
        assertThat(inst).doesNotContain("mayonnaise et.")
    }
}
