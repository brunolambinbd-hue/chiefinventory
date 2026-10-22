package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrCrabeMangueFullRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse full crabe mangue recipe should extract title ingredients and instructions cleanly`() {
        val ocrText = """
            Crabe mangue
            Epluchez et dénoyautez les mangues, taillez-en la moitié en lamelles.
            Dans un bol, mélangez le jaune d'oeuf et la moutarde, ajoutez l'huile au fouet pour obtenir une mayonnaise ferme, salez et poivrez, incorporez ensuite le yaourt.
            Mélangez le crabe égoutté et une partie de la mayonnaise.
            Placez une lamelle de mangue et un peu de crabe sur les petites feuilles de chicon.
            Décorez de feuilles d'aneth.
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

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        // 1. Validation du nombre de personnes
        assertThat(result.servings).isEqualTo("4")

        // 2. Validation des ingrédients (incluant des petites feuilles de chicon et 1 branche d'aneth)
        assertThat(ing).ignoringCase().contains("100 g de chair de crabe")
        assertThat(ing).ignoringCase().contains("des petites feuilles de chicon")
        assertThat(ing).ignoringCase().contains("1 branche d'aneth")

        // 3. Validation des instructions (les consignes du haut doivent être dans les instructions)
        assertThat(inst).ignoringCase().contains("Epluchez et dénoyautez les mangues")
        assertThat(inst).ignoringCase().doesNotContain("des petites feuilles de chicon")
    }
}
