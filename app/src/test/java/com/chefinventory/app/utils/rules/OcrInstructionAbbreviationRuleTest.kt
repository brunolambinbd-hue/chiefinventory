package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("L'abréviation 'c.' ne doit pas couper la phrase et doit être recollée en 'aux 4 c. à soupe de yaourt.'")
            .that(inst)
            .contains("aux 4 c. à soupe de yaourt.")

        assertWithMessage("La consigne ne doit pas être coupée après 'c.'")
            .that(inst)
            .doesNotContain("aux 4 c.\n")

        assertWithMessage("La majuscule 'À' de 'À soupe' doit passer en minuscule 'à'")
            .that(inst)
            .doesNotContain("À soupe")
    }
}
