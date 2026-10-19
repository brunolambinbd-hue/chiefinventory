package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrAubergineRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse aubergine recipe when instructions appear before ingredients without header should place sel poivre in ingredients`() {
        val ocrText = """
            CONRAD BR LS
            Nahit YILMAZ
            Hôtel Conrad International
            avenue Louise 71 1050 BRUXELLES
            Tél. : 02/542.42.42
            175 saveur de TURQUIE
            Palade d'aubergines
            Plongez les aubergines dans l'eau bouillante et retirez la peau. Hachez la chair et faites-la suer dans un peu de beurre avec l'ail haché et le piment émincé fin. Assaisonnez. Ajoutez le jus de citron et le persil haché. Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.
            23 20
            pour 4 personnes
            6 aubergines moyennes (t 1 kg)
            1 ou 2 citrons
            150 g de beurre
            1 piment rouge
            persil haché
            2 gousses d'ail
            sel, poivre
            Pour la décoration: tomates
            Notre vin conseillé
            Rousse Région Merlot (vin rouge de Bulgarie)
            M.O.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        assertThat(ing).ignoringCase().contains("sel")
        assertThat(ing).ignoringCase().contains("poivre")
        assertThat(inst).ignoringCase().doesNotContain("sel, poivre")
    }
}
