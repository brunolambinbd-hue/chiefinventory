package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrBuchettesWineRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse buchettes de melon recipe should capture full multi line wine recommendation Atinum Bordeaux Graves MO 1996 75 cl`() {
        val ocrText = """
            ingrédients
            POUR
            4 PERSONNES
            1 melon Galia
            sel et poivre
            Büchettes de melon
            au jambon italien et au porto
            • Mixez le porto, le vinaigre, le yoghourt, l'huile d'olive.
            VIN CONSEILLÉ
            Atinum, Bordeaux Graves
            MO, 1996, 75 cl
            AtN
            ATINUM
            GRAVES
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val wine = result.wine ?: ""

        // Validation de la recommandation Vin complète sur plusieurs lignes
        assertThat(wine).ignoringCase().contains("Atinium")
        assertThat(wine).ignoringCase().contains("Bordeaux Graves")
        assertThat(wine).ignoringCase().contains("1996")
        assertThat(wine).ignoringCase().contains("75 cl")
    }
}
