package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrInstructionAbbreviationRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe should merge instruction broken after abbreviation c dot into single sentence`() {
        val ocrText = """
            Salade océane
            INGRÉDIENTS
            250 g de yaourt
            sel et poivre
            • Mélangez les légumes, la feta et l'estragon haché aux 4 c.
            À soupe de yaourt.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val inst = result.instructions ?: ""

        // Validation : "aux 4 c." + "À soupe de yaourt." doivent être recollés en "aux 4 c. à soupe de yaourt."
        assertThat(inst).contains("aux 4 c. à soupe de yaourt.")
        assertThat(inst).doesNotContain("aux 4 c.\n")
        assertThat(inst).doesNotContain("À soupe")
    }
}
