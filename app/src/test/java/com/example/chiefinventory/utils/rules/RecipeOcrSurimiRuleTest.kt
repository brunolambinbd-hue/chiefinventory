package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrSurimiRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `exhaustive reference test for recipe 02 salade de surimi`() {
        val ocrText = """
            ingrédients
            POUR
            4 PERSONNES
            120 g de surimi (poisson
            au goût de crabe) ou
            de véritable crabe
            2 c. à soupe
            de mayonnaise
            1 c. à soupe de câpres
            2 c. à soupe
            de persil haché
            2 œufs durs
            4 toasts
            salade de blé
            le jus de 1/2 citron
            sel et poivre
            Salade de surimi
            • Hachez finement le surimi ou
            le crabe ainsi que les œufs
            durs.
            • Mélangez avec la
            mayonnaise, le persil, le jus de
            citron, les câpres, du sel et du
            poivre.
            • Disposez un peu de salade de
            blé en forme de cercle sur une
            assiette et déposez-y la salade
            de surimi.
            Assiette de présentation disponible dans les Maxi GB
            • Servez avec les toasts.
            VIN CONSEILLÉ
            Atinum, Bordeaux blanc
            MO, 1996, 75 cl
            N
            4 tinlin
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val title = result.title ?: ""
        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""
        val wine = result.wine ?: ""

        // ==========================================
        // 1. VALIDATION ET EXCLUSIVITÉ DU TITRE
        // ==========================================
        assertThat(title).ignoringCase().contains("Salade de surimi")
        assertThat(ing).ignoringCase().doesNotContain("Salade de surimi")
        assertThat(wine).ignoringCase().doesNotContain("Salade de surimi")

        // ==========================================
        // 2. VALIDATION ET EXCLUSIVITÉ DES INGRÉDIENTS (Lignes avec quantité)
        // ==========================================
        val expectedIngredientsWithQty = listOf(
            "120 g de surimi (poisson au goût de crabe) ou de véritable crabe",
            "2 c. à soupe de mayonnaise",
            "1 c. à soupe de câpres",
            "2 c. à soupe de persil haché",
            "2 œufs durs",
            "4 toasts"
        )

        for (item in expectedIngredientsWithQty) {
            assertThat(ing).ignoringCase().contains(item)
            assertThat(inst).ignoringCase().doesNotContain(item) // 👈 Aucune ligne avec quantité ne fuit vers les instructions !
            assertThat(title).ignoringCase().doesNotContain(item)
            assertThat(wine).ignoringCase().doesNotContain(item)
        }

        assertThat(ing).ignoringCase().contains("Salade de blé")
        assertThat(ing).ignoringCase().contains("jus de 1/2 citron")
        assertThat(ing).ignoringCase().contains("sel et poivre")

        // ==========================================
        // 3. VALIDATION ET EXCLUSIVITÉ DES INSTRUCTIONS
        // ==========================================
        val expectedInstructions = listOf(
            "Hachez finement le surimi ou le crabe ainsi que les œufs durs.",
            "Mélangez avec la mayonnaise, le persil, le jus de citron, les câpres, du sel et du poivre.",
            "Disposez un peu de salade de blé en forme de cercle sur une assiette et déposez-y la salade de surimi.",
            "Servez avec les toasts."
        )

        for (line in expectedInstructions) {
            assertThat(inst).ignoringCase().contains(line)
            assertThat(ing).ignoringCase().doesNotContain(line)
        }

        // ==========================================
        // 4. METADONNÉES (Portions, Vin)
        // ==========================================
        assertThat(result.servings).isEqualTo("4")

        assertThat(wine).ignoringCase().contains("Atinium")
        assertThat(wine).ignoringCase().contains("Bordeaux blanc")
        assertThat(ing).ignoringCase().doesNotContain("Bordeaux")
        assertThat(inst).ignoringCase().doesNotContain("Bordeaux")
    }
}
