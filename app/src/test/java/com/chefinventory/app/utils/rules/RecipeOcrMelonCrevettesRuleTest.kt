package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("Les '4 tranches' et 'de jambon séché' doivent être fusionnées en '4 tranches de jambon séché'")
            .that(ing)
            .ignoringCase()
            .contains("4 tranches de jambon séché")
    }

}
