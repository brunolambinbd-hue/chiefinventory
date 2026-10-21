package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrMelonCrevettesRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse melon crevettes recipe should merge 4 tranches and de jambon seche into single full ingredient`() {
        val ocrText = """
            Melon aux crevettes
            INGRÉDIENTS
            1 petite salade frisée
            1 melon galia
            4 tranches
            de jambon séché
            200 g de crevettes
            2 dl de vin muscat
            2 c. à soupe d'huile d'olive
            1 c. à soupe de vinaigre
            le jus de 1/2 citron
            sel et poivre
            • Coupez le melon en deux, retirez les pépins.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        // Validation : "4 tranches" + "de jambon séché" doivent être fusionnés en "4 tranches de jambon séché"
        assertThat(ing).ignoringCase().contains("4 tranches de jambon séché")
    }
}
