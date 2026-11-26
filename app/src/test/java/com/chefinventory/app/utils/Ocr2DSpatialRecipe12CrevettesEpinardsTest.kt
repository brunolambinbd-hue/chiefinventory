package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test E2E de référence 12 sur 25 : 'Crevettes grises aux épinards'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe12CrevettesEpinardsTest {

    private lateinit var context: Context
    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        OcrRegexesDynamicHelpers.clearCache()
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRecipe12_CrevettesEpinards_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 83, left = 101, bottom = 116, right = 363),
            OcrTextLine("POUR", top = 123, left = 180, bottom = 151, right = 286),
            OcrTextLine("4 PERSONNES", top = 157, left = 105, bottom = 185, right = 359),
            OcrTextLine("400 g de crevettes grises", top = 206, left = 82, bottom = 235, right = 383),
            OcrTextLine("décortiquées", top = 238, left = 155, bottom = 269, right = 311),
            OcrTextLine("200 g d'épinards frais", top = 281, left = 98, bottom = 312, right = 369),
            OcrTextLine("Ic.à soupe de persil", top = 323, left = 106, bottom = 353, right = 366),
            OcrTextLine("haché", top = 357, left = 199, bottom = 382, right = 270),
            OcrTextLine("lc.à soupe de basilic", top = 396, left = 106, bottom = 429, right = 366),
            OcrTextLine("haché", top = 433, left = 199, bottom = 459, right = 268),
            OcrTextLine("Ic.à soupe de cerfeuil", top = 478, left = 105, bottom = 508, right = 376),
            OcrTextLine("haché", top = 513, left = 200, bottom = 534, right = 272),
            OcrTextLine("lc.à soupe d'estragon", top = 555, left = 103, bottom = 583, right = 375),
            OcrTextLine("haché", top = 586, left = 200, bottom = 611, right = 271),
            OcrTextLine("3 jaunes d' eufs", top = 631, left = 140, bottom = 659, right = 331),
            OcrTextLine("150 g de beurre", top = 671, left = 141, bottom = 701, right = 330),
            OcrTextLine("3c. à soupe d'eau", top = 716, left = 125, bottom = 744, right = 347),
            OcrTextLine("lc.à soupe de vinaigre de", top = 755, left = 74, bottom = 788, right = 397),
            OcrTextLine("vin blanc", top = 790, left = 181, bottom = 816, right = 291),
            OcrTextLine("sel et poivre", top = 836, left = 163, bottom = 863, right = 309),
            OcrTextLine("orevettes grises aux épinards", top = 644, left = 440, bottom = 770, right = 1818),
            OcrTextLine("•Lavez et coupez les épinards", top = 1053, left = 77, bottom = 1100, right = 576),
            OcrTextLine("en fines bandelettes. Mettez-les", top = 1099, left = 74, bottom = 1139, right = 599),
            OcrTextLine("dans une assiette creuse.", top = 1153, left = 75, bottom = 1184, right = 479),
            OcrTextLine("•RecouVrez les épinards avec", top = 1236, left = 78, bottom = 1279, right = 573),
            OcrTextLine("les crevettes. Salez et poivrez.", top = 1282, left = 79, bottom = 1324, right = 575),
            OcrTextLine("• Faites la mousseline aux her-", top = 823, left = 656, bottom = 861, right = 1158),
            OcrTextLine("bes: battez les jaunes d'eufs", top = 866, left = 655, bottom = 909, right = 1122),
            OcrTextLine("avec l'eau et le vinaigre à feu", top = 912, left = 656, bottom = 955, right = 1135),
            OcrTextLine("doux pour en faire une mousse.", top = 961, left = 656, bottom = 998, right = 1164),
            OcrTextLine("Assiette de présentation disponible dans les Maxi GB", top = 572, left = 1351, bottom = 607, right = 1980),
            OcrTextLine("• Dans un autre poêlon, faites", top = 1049, left = 669, bottom = 1090, right = 1139),
            OcrTextLine("fondre le beurreà feu doux et", top = 1091, left = 657, bottom = 1132, right = 1140),
            OcrTextLine("Incorporez-le progressivement", top = 1135, left = 659, bottom = 1187, right = 1155),
            OcrTextLine("aux jaunes d'eufs battus.", top = 1182, left = 659, bottom = 1225, right = 1067),
            OcrTextLine("Veillez à ne pas ajouter les", top = 1232, left = 659, bottom = 1269, right = 1094),
            OcrTextLine("résidus blancs.", top = 1279, left = 660, bottom = 1309, right = 895),
            OcrTextLine("• Ajoutez en dernier lieu les", top = 813, left = 1229, bottom = 860, right = 1674),
            OcrTextLine("herbes hachées.", top = 863, left = 1227, bottom = 897, right = 1475),
            OcrTextLine("• Nappez les épinards et les", top = 947, left = 1230, bottom = 996, right = 1670),
            OcrTextLine("crevettes de la mousseline aux", top = 997, left = 1229, bottom = 1032, right = 1719),
            OcrTextLine("fines herbes.", top = 1046, left = 1229, bottom = 1076, right = 1432),
            OcrTextLine("VIN CONSEILLÉ", top = 1159, left = 1426, bottom = 1208, right = 1754),
            OcrTextLine("Sauvignon de Touraine", top = 1231, left = 1414, bottom = 1271, right = 1756),
            OcrTextLine("Henry Marionnet, M.O., 1996, 75 cl", top = 1282, left = 1248, bottom = 1316, right = 1762),
            OcrTextLine("RAINE", top = 1218, left = 1864, bottom = 1242, right = 1923),
            OcrTextLine("1", top = 888, left = 2040, bottom = 900, right = 2043)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 12 doit valoir 'Crevettes grises aux épinards'")
            .that(result.title)
            .isEqualTo("Crevettes grises aux épinards")

        // 2. Méta-données de Temps
        assertWithMessage("Le temps de préparation doit valoir null")
            .that(result.prepTime)
            .isNull()

        assertWithMessage("Le temps de cuisson doit valoir null")
            .that(result.cookTime)
            .isNull()

        assertWithMessage("Le temps de repos doit valoir null")
            .that(result.restingTime)
            .isNull()

        // 3. Portions
        assertWithMessage("Le nombre de personnes doit valoir '4'")
            .that(result.servings)
            .isEqualTo("4")

        // 4. Méta-données Nutritionnelles & Difficulté
        assertWithMessage("Les kcal par portion doivent valoir null")
            .that(result.kcalPerServing)
            .isNull()

        assertWithMessage("La difficulté doit valoir null")
            .that(result.difficulty)
            .isNull()

        assertWithMessage("Le niveau de prix doit valoir null")
            .that(result.priceRating)
            .isNull()

        assertWithMessage("Le Nutri-Score doit valoir null")
            .that(result.nutriScore)
            .isNull()

        assertWithMessage("Les macronutriments doivent valoir null")
            .that(result.nutritionInfo)
            .isNull()

        // 5. Accord Vin
        assertWithMessage("La recommandation de vin doit valoir 'Sauvignon de Touraine Henry Marionnet, M.O., 1996, 75 cl'")
            .that(result.wine)
            .isEqualTo("Sauvignon de Touraine Henry Marionnet, M.O., 1996, 75 cl")

        // 6. Source
        assertWithMessage("La source doit valoir 'Maxi GB'")
            .that(result.source)
            .isEqualTo("Maxi GB")

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "400 g de crevettes grises décortiquées",
            "200 g d'épinards frais",
            "1 c. à soupe de persil haché",
            "1 c. à soupe de basilic haché",
            "1 c. à soupe de cerfeuil haché",
            "1 c. à soupe d'estragon haché",
            "3 jaunes d'œufs",
            "150 g de beurre",
            "3 c. à soupe d'eau",
            "1 c. à soupe de vinaigre de vin blanc",
            "Sel et poivre"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Lavez et coupez les épinards en fines bandelettes.",
            "Mettez-les dans une assiette creuse.",
            "Recouvrez les épinards avec les crevettes.",
            "Salez et poivrez.",
            "Faites la mousseline aux herbes: battez les jaunes d'œufs avec l'eau et le vinaigre à feu doux pour en faire une mousse.",
            "Dans un autre poêlon, faites fondre le beurre à feu doux et Incorporez-le progressivement aux jaunes d'œufs battus.",
            "Veillez à ne pas ajouter les résidus blancs.",
            "Ajoutez en dernier lieu les herbes hachées.",
            "Nappez les épinards et les crevettes de la mousseline aux fines herbes."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
