package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrEpinardsCrevettesRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse full crevettes epinards recipe should merge mousseline aux and fines herbes into single instruction`() {
        val ocrText = """
            orevettes grises aux épinards
            ingrédients
            POUR
            4 PERSONNES
            400 g de crevettes grises
            décortiquées
            200 g d'épinards frais
            1 c. à soupe de persil
            haché
            1 c. à soupe de basilic
            haché
            1 c. à soupe de cerfeuil
            haché
            1 c. à soupe d'estragon
            haché
            3 jaunes d'œufs
            150 g de beurre
            3 c. à soupe d'eau
            1 c. à soupe de vinaigre de
            vin blanc
            sel et poivre
            45
            • Lavez et coupez les épinards
            en fines bandelettes. Mettez-les
            dans une assiette creuse.
            • Recouvrez les épinards avec
            les crevettes. Salez et poivrez.
            • Faites la mousseline aux her-
            bes: battez les jaunes d'œufs
            avec l'eau et le vinaigre à feu
            doux pour en faire une mousse.
            Assiette de présentation disponible dans les Maxi GB
            • Dans un autre poêlon, faites
            fondre le beurre à feu doux et
            Incorporez-le progressivement
            aux jaunes d'œufs battus.
            Veillez à ne pas ajouter les
            résidus blancs.
            • Ajoutez en dernier lieu les
            herbes hachées.
            • Nappez les épinards et les
            crevettes de la mousseline aux
            fines herbes.
            VIN CONSEILLÉ
            Sauvignon de Touraine
            Henry Marionnet, M.O., 1996, 75 cl
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        // 1. Validation de l'instruction complète (fines herbes ne doit pas être tronqué ni mis dans les ingrédients)
        assertThat(inst).ignoringCase().contains("Nappez les épinards et les crevettes de la mousseline aux fines herbes.")
        assertThat(inst).ignoringCase().doesNotContain("mousseline aux.")

        // 2. Validation que fines herbes n'apparaît pas comme ingrédient seul
        assertThat(ing).ignoringCase().doesNotContain("fines herbes\n")
        assertThat(ing).ignoringCase().doesNotContain("\nfines herbes")
    }
}
