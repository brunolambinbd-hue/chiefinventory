package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("La consigne tronquée doit être fusionnée en 'Nappez les épinards et les crevettes de la mousseline aux fines herbes.'")
            .that(inst)
            .ignoringCase()
            .contains("Nappez les épinards et les crevettes de la mousseline aux fines herbes.")

        assertWithMessage("La consigne ne doit pas être tronquée en 'mousseline aux.'")
            .that(inst)
            .ignoringCase()
            .doesNotContain("mousseline aux.")

        // 2. Validation que fines herbes n'apparaît pas comme ingrédient seul
        assertWithMessage("Le complément 'fines herbes' ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("fines herbes\n")

        assertWithMessage("Le complément 'fines herbes' ne doit pas fuiter comme ligne d'ingrédient isolée")
            .that(ing)
            .ignoringCase()
            .doesNotContain("\nfines herbes")
    }
}
