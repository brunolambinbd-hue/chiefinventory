package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("Le millésime corrompu 'I996' doit être réparé en '1996'")
            .that(wine)
            .contains("1996")

        assertWithMessage("Le millésime corrompu 'I996' ne doit plus figurer")
            .that(wine)
            .doesNotContain("I996")

        assertWithMessage("L'accentuation corrompue 'vìn' doit être réparée en 'vin rouge'")
            .that(wine)
            .contains("vin rouge")

        assertWithMessage("Le mot fautif 'vìn' ne doit plus figurer")
            .that(wine)
            .doesNotContain("vìn")
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

        assertWithMessage("La faute d'unité '75 d' doit être réparée en '75 cl'")
            .that(wine)
            .contains("75 cl")

        assertWithMessage("L'unité fautive '75 d' ne doit plus figurer")
            .that(wine)
            .doesNotContain("75 d")
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

        assertWithMessage("Le terme 'Atinum' doit être réparé en 'Atinium'")
            .that(wine)
            .contains("Atinium")

        assertWithMessage("Le terme 'Bordeau' doit être réparé en 'Bordeaux'")
            .that(wine)
            .contains("Bordeaux")

        assertWithMessage("Le terme 'Chậteau' doit être réparé en 'Château'")
            .that(wine)
            .contains("Château")

        val wineTokens = wine.lowercase().split(Regex("[\\s,]+"))
        assertWithMessage("Le jeton 'atinum' ne doit plus figurer")
            .that(wineTokens)
            .doesNotContain("atinum")

        assertWithMessage("Le jeton 'bordeau' ne doit plus figurer")
            .that(wineTokens)
            .doesNotContain("bordeau")

        assertWithMessage("Le jeton 'chậteau' ne doit plus figurer")
            .that(wineTokens)
            .doesNotContain("chậteau")
    }
}
