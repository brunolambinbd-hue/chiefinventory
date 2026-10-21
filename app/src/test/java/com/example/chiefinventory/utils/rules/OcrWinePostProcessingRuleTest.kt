package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrWinePostProcessingRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe should repair wine millesime I996 to 1996 and vin accent in wine string`() {
        val ocrText = """
            Salade d'aubergines
            INGRÉDIENTS
            6 aubergines
            sel et poivre
            • Préparation de la recette.
            Notre vin conseillé
            Rousse Région Merlot (vìn rouge de Bulgarie) MDC, I996, 75 cl
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val wine = result.wine ?: ""

        // 1. Validation de la correction du millésime (I996 -> 1996)
        assertThat(wine).contains("1996")
        assertThat(wine).doesNotContain("I996")

        // 2. Validation de la correction de l'accent ("vìn" -> "vin")
        assertThat(wine).contains("vin rouge")
        assertThat(wine).doesNotContain("vìn")
    }

    @Test
    fun `parse recipe should repair wine volume unit typos like 75 d to 75 cl`() {
        val ocrText = """
            Crevettes grises
            INGRÉDIENTS
            200 g de crevettes
            sel et poivre
            • Préparation.
            VIN CONSEILLÉ
            Sauvignon de Touraine 1995, 75 d
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val wine = result.wine ?: ""

        assertThat(wine).contains("75 cl")
        assertThat(wine).doesNotContain("75 d")
    }

    @Test
    fun `parse recipe should repair oenological spelling typos like Chateau Atinum and Bordeau`() {
        val ocrText = """
            Salade de surimi
            INGRÉDIENTS
            120 g de surimi
            sel et poivre
            • Préparation.
            VIN CONSEILLÉ
            Atinum, Bordeau, Chậteau
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val wine = result.wine ?: ""

        assertThat(wine).contains("Atinium")
        assertThat(wine).contains("Bordeaux")
        assertThat(wine).contains("Château")

        val wineTokens = wine.lowercase().split(Regex("[\\s,]+"))
        assertThat(wineTokens).doesNotContain("atinum")
        assertThat(wineTokens).doesNotContain("bordeau")
        assertThat(wineTokens).doesNotContain("chậteau")
    }
}
