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
 * Test E2E de référence 10 sur 25 : 'Cocktail de crevettes aux concombres'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe10CocktailCrevettesTest {

    private lateinit var context: Context
    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
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
    fun testRecipe10_CocktailCrevettes_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 113, left = 99, bottom = 152, right = 353),
            OcrTextLine("POUR", top = 154, left = 177, bottom = 183, right = 278),
            OcrTextLine("4 PERSONNES", top = 184, left = 103, bottom = 219, right = 352),
            OcrTextLine("300 g de crevettes roses", top = 235, left = 87, bottom = 266, right = 370),
            OcrTextLine("8c.à soupe de yaourt", top = 269, left = 110, bottom = 304, right = 362),
            OcrTextLine("demi-écrémé sucré", top = 301, left = 118, bottom = 332, right = 344),
            OcrTextLine("2 tomates Roma", top = 337, left = 136, bottom = 368, right = 327),
            OcrTextLine("I12 concombre", top = 376, left = 147, bottom = 403, right = 320),
            OcrTextLine("80 g de feta", top = 415, left = 165, bottom = 445, right = 305),
            OcrTextLine("lc.à soupe", top = 450, left = 177, bottom = 481, right = 311),
            OcrTextLine("d'estragon haché", top = 479, left = 136, bottom = 516, right = 344),
            OcrTextLine("lc.à soupe de ketchup", top = 516, left = 101, bottom = 553, right = 373),
            OcrTextLine("2c. à soupe", top = 558, left = 167, bottom = 587, right = 310),
            OcrTextLine("de mayonnaise", top = 590, left = 149, bottom = 622, right = 324),
            OcrTextLine("l c.à soupe", top = 627, left = 185, bottom = 655, right = 309),
            OcrTextLine("de vinaigre de vin blanc", top = 655, left = 96, bottom = 693, right = 383),
            OcrTextLine("quelques branches", top = 689, left = 129, bottom = 732, right = 350),
            OcrTextLine("de cerfeuil", top = 728, left = 178, bottom = 761, right = 304),
            OcrTextLine("sel et poivre", top = 769, left = 170, bottom = 797, right = 316),
            OcrTextLine("toocktail de crevettes aux concombres", top = 615, left = 440, bottom = 763, right = 1974),
            OcrTextLine("• Coupez le concombre dans la", top = 836, left = 84, bottom = 895, right = 591),
            OcrTextLine("longueur et épépinez-le.", top = 888, left = 86, bottom = 940, right = 477),
            OcrTextLine("Détaillez-le ensuite en petits", top = 931, left = 86, bottom = 979, right = 550),
            OcrTextLine("dés de 0,5 x 0,5 cm. Epluchez", top = 975, left = 86, bottom = 1028, right = 578),
            OcrTextLine("et épépinez les tomates et", top = 1022, left = 86, bottom = 1076, right = 508),
            OcrTextLine("découpez-les en menus", top = 1070, left = 87, bottom = 1121, right = 469),
            OcrTextLine("morceaux de 0,5 x 0,5 cm.", top = 1111, left = 88, bottom = 1164, right = 525),
            OcrTextLine("• Coupez la feta en morceaux.", top = 1201, left = 104, bottom = 1255, right = 585),
            OcrTextLine("• Mélangez les légumes, la feta", top = 1288, left = 96, bottom = 1345, right = 601),
            OcrTextLine("et l'estragon haché aux 4 c. à", top = 1334, left = 94, bottom = 1391, right = 575),
            OcrTextLine("soupe de yaourt. Salez et", top = 822, left = 658, bottom = 875, right = 1064),
            OcrTextLine("poivrez.", top = 878, left = 659, bottom = 921, right = 791),
            OcrTextLine("• Mélangez le reste du yaourt à", top = 953, left = 662, bottom = 1008, right = 1169),
            OcrTextLine("la mayonnaise, au ketchup et", top = 998, left = 664, bottom = 1056, right = 1136),
            OcrTextLine("au vinaigre. Assaisonnez.", top = 1045, left = 664, bottom = 1102, right = 1081),
            OcrTextLine("Assiette de présentation disponible dans les Maxi GB", top = 541, left = 1343, bottom = 592, right = 1971),
            OcrTextLine("• Disposez la préparation de", top = 1133, left = 670, bottom = 1193, right = 1126),
            OcrTextLine("concombres sur les assiettes et", top = 1179, left = 668, bottom = 1228, right = 1169),
            OcrTextLine("garnissez de quelques crevettes", top = 1223, left = 670, bottom = 1283, right = 1183),
            OcrTextLine("roses. Nappez de la sauce au", top = 1272, left = 672, bottom = 1324, right = 1139),
            OcrTextLine("yaourt et à la mayonnaise et", top = 1316, left = 673, bottom = 1372, right = 1131),
            OcrTextLine("garnissez de quelques branches", top = 796, left = 1228, bottom = 855, right = 1732),
            OcrTextLine("de cerfeuil.", top = 857, left = 1229, bottom = 892, right = 1406),
            OcrTextLine("VIN CONSEILLÉ", top = 1147, left = 1438, bottom = 1205, right = 1768),
            OcrTextLine("Chardonnay Chais Cuxac,", top = 1210, left = 1348, bottom = 1261, right = 1775),
            OcrTextLine("Vin de Pays d'Oc,", top = 1258, left = 1478, bottom = 1299, right = 1768),
            OcrTextLine("MO, 1996, 75 cl", top = 1302, left = 1504, bottom = 1347, right = 1774)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 10 doit valoir 'Cocktail de crevettes aux concombres'")
            .that(result.title)
            .isEqualTo("Cocktail de crevettes aux concombres")

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
        assertWithMessage("La recommandation de vin doit valoir 'Chardonnay Chais Cuxac De Pays d'Oc M.O., 1996, 75 cl'")
            .that(result.wine)
            .isEqualTo("Chardonnay Chais Cuxac De Pays d'Oc M.O., 1996, 75 cl")

        // 6. Source
        assertWithMessage("La source doit valoir 'Maxi GB'")
            .that(result.source)
            .isEqualTo("Maxi GB")

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "300 g de crevettes roses",
            "8 c. à soupe de yaourt demi-écrémé sucré",
            "2 tomates Romas",
            "1/2 concombre",
            "80 g de feta",
            "1 c. à soupe d'estragon haché",
            "1 c. à soupe de ketchup",
            "2 c. à soupe de mayonnaise",
            "1 c. à soupe de vinaigre de vin blanc",
            "Quelques branches de cerfeuil",
            "Sel et poivre"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Coupez le concombre dans la longueur et épépinez-le.",
            "Détaillez-le ensuite en petits dés de 0,5 x 0,5 cm.",
            "Epluchez et épépinez les tomates et découpez-les en menus morceaux de 0,5 x 0,5 cm.",
            "Coupez la feta en morceaux.",
            "Mélangez les légumes, la feta et l'estragon haché aux 4 c. à soupe de yaourt.",
            "Salez et poivrez.",
            "Mélangez le reste du yaourt à la mayonnaise, au ketchup et au vinaigre.",
            "Assaisonnez.",
            "Disposez la préparation de concombres sur les assiettes et garnissez de quelques crevettes roses.",
            "Nappez de la sauce au yaourt et à la mayonnaise et garnissez de quelques branches de cerfeuil."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
