package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrCrabeMangueRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse crabe mangue recipe should repair Drabe mangue to Crabe mangue using recipe ingredients`() {
        val ocrText = """
            Drabe mangue
            Epluchez et dénoyautez les mangues, taillez-en la moitié en lamelles, le reste en
            Dans un bol, mélangez le jaune d'oeuf et la moutarde, ajoutez l'huile au fouet
            pour obtenir une mayonnaise ferme, salez et poivrez, incorporez ensuite le yaourt
            le rhum, le curcuma, les dés de mangue et du poivre de Cayenne selon le goût.
            Mélangez le crabe égoutté et une partie de la mayonnaise.
            Placez une lamelle de mangue et un peu de crabe sur les petites feuilles de chicon
            rz de mayonnaise restante et décorez de feuilles d'aneth.
            ingrédients pour 4 personnes
            100 g de chair de crabe
            2 mangues
            1 c. à café de moutarde
            2 c. à soupe de yaourt grec
            1 jaune d'oeuf
            5 cl d'huile
            1 pincée de curcuma
            1 c. à dessert de rhum
            poivre de Cayenne, sel, poivre
            des petites feuilles de chicon
            1 branche d'aneth
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        // Validation du titre réparé : "Drabe mangue" -> "Crabe mangue"
        assertThat(result.title).ignoringCase().contains("Crabe mangue")
    }
}
