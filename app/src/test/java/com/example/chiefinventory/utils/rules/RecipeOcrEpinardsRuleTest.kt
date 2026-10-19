package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrEpinardsRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse real epinards recipe should extract title and preserve full ingredient vinaigre de vin blanc`() {
        val ocrText = """
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
            orevettes grises aux épinards
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
            VIN CONSEILLE
            Sauvignon de Touraine
            Henry Marionnet, M.0., 1996, 75 cl
            IRAINE
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        // 1. Validation du titre réparé (orevettes grises... -> Crevettes grises aux épinards)
        assertThat(result.title).ignoringCase().contains("Crevettes grises aux épinards")

        // 2. Validation des ingrédients (vinaigre de vin blanc doit être fusionné et intact)
        assertThat(ing).ignoringCase().contains("400 g de crevettes grises décortiquées")
        assertThat(ing).ignoringCase().contains("200 g d'épinards frais")
        assertThat(ing).ignoringCase().contains("1 c. à soupe de vinaigre de vin blanc")
        assertThat(ing).ignoringCase().contains("150 g de beurre")

        // 3. Validation des consignes
        assertThat(inst).ignoringCase().contains("Lavez et coupez les épinards")
        assertThat(inst).ignoringCase().doesNotContain("1 c. à soupe de vinaigre de vin blanc")
    }
}
