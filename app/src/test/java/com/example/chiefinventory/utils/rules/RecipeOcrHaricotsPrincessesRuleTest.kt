package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrHaricotsPrincessesRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe should merge base ingredient haricots and variety princesses into single line`() {
        val ocrText = """
            Salade d'avocat et princesses
            INGRÉDIENTS
            1 avocat
            200 g de haricots
            princesses
            200 g de saumon cru
            aneth
            6 c. à soupe d'huile d'olive
            le jus de 1/2 citron
            1 c. à café de miel
            1 c. à soupe de vinaigre
            sel et poivre
            • Faites cuire les haricots princesses 10 à 15 minutes.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        // Validation : "200 g de haricots" + "princesses" doivent être fusionnés en "200 g de haricots princesses"
        assertThat(ing).ignoringCase().contains("200 g de haricots princesses")
    }
}
