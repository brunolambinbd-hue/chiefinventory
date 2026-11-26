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
 * Test E2E de référence 14 sur 25 : 'Puits de concombre aux œufs de truite'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe14ConcombreOeufsTruiteTest {

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
    fun testRecipe14_ConcombreOeufsTruite_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("Cuits de concombre aux eus de truite", top = 50, left = 354, bottom = 185, right = 1441),
            OcrTextLine("1) Découpez deux grands tron-", top = 222, left = 96, bottom = 261, right = 586),
            OcrTextLine("Çons de 12 cm dans le", top = 261, left = 105, bottom = 304, right = 589),
            OcrTextLine("concombre. Creusez des sillons", top = 303, left = 91, bottom = 340, right = 590),
            OcrTextLine("tout autour à l'aide d'un zesteur.", top = 349, left = 91, bottom = 383, right = 589),
            OcrTextLine("Découpez chaque tronçon en 4:", top = 393, left = 94, bottom = 429, right = 590),
            OcrTextLine("vous obtenez 8 petits tronçons", top = 435, left = 92, bottom = 469, right = 592),
            OcrTextLine("d'environ 3 cm de haut. Creusez-", top = 473, left = 93, bottom = 509, right = 593),
            OcrTextLine("les à l'aide d'une petite cuillère.", top = 512, left = 95, bottom = 551, right = 588),
            OcrTextLine("Salez l'intérieur et retournez-les", top = 557, left = 94, bottom = 590, right = 594),
            OcrTextLine("sur une grille pour les faire dé-", top = 595, left = 94, bottom = 640, right = 594),
            OcrTextLine("gorger.", top = 651, left = 94, bottom = 683, right = 193),
            OcrTextLine("2) Pendant ce temps, préparez la", top = 716, left = 95, bottom = 754, right = 595),
            OcrTextLine("farce: mélangez le fromage", top = 759, left = 94, bottom = 798, right = 594),
            OcrTextLine("blanc avec les herbes et les oi-", top = 798, left = 95, bottom = 836, right = 596),
            OcrTextLine("gnons, ainsi que la moitié des", top = 838, left = 94, bottom = 885, right = 594),
            OcrTextLine("eufs de truite. Rectifiez l'assai-", top = 881, left = 95, bottom = 923, right = 598),
            OcrTextLine("sonnement. Versez cette prépara-", top = 926, left = 94, bottom = 965, right = 596),
            OcrTextLine("tion dans une grande poche à", top = 969, left = 94, bottom = 1009, right = 594),
            OcrTextLine("douille.", top = 1017, left = 95, bottom = 1046, right = 205),
            OcrTextLine("3) Farcissez les puits de", top = 1085, left = 95, bottom = 1127, right = 596),
            OcrTextLine("concombre bien égouttés avec", top = 1128, left = 95, bottom = 1170, right = 597),
            OcrTextLine("cette préparation. Décorez le", top = 1169, left = 95, bottom = 1216, right = 598),
            OcrTextLine("dessus avec le restant des eufs", top = 1212, left = 95, bottom = 1252, right = 598),
            OcrTextLine("de truite. Mettez au frais jusqu'au", top = 1258, left = 95, bottom = 1296, right = 597),
            OcrTextLine("moment de servir.", top = 1304, left = 97, bottom = 1337, right = 364),
            OcrTextLine("4) Décorez les assiettes de la-", top = 1372, left = 96, bottom = 1417, right = 604),
            OcrTextLine("melles de tomates et de salade.", top = 1419, left = 99, bottom = 1455, right = 560),
            OcrTextLine("POUR", top = 230, left = 1702, bottom = 259, right = 1812),
            OcrTextLine("4 PERSONNES:", top = 272, left = 1613, bottom = 302, right = 1899),
            OcrTextLine("1 grand concombre", top = 312, left = 1632, bottom = 357, right = 1900),
            OcrTextLine("1 petit pot d'eufs de", top = 355, left = 1616, bottom = 402, right = 1911),
            OcrTextLine("truite (50g)", top = 396, left = 1679, bottom = 432, right = 1843),
            OcrTextLine("250 g de fromage blanc", top = 435, left = 1590, bottom = 479, right = 1933),
            OcrTextLine("1c.à soupe de cerfeuil", top = 476, left = 1602, bottom = 521, right = 1936),
            OcrTextLine("haché", top = 515, left = 1721, bottom = 547, right = 1807),
            OcrTextLine("1 c. à soupe de", top = 561, left = 1670, bottom = 597, right = 1877),
            OcrTextLine("ciboulette hachée", top = 589, left = 1638, bottom = 628, right = 1894),
            OcrTextLine("1 c. à soupe d'aneth", top = 637, left = 1636, bottom = 674, right = 1912),
            OcrTextLine("haché", top = 674, left = 1725, bottom = 704, right = 1813),
            OcrTextLine("1 c. à soupe d'oignons", top = 720, left = 1624, bottom = 757, right = 1934),
            OcrTextLine("frais hachés", top = 750, left = 1689, bottom = 787, right = 1854),
            OcrTextLine("quelques feuilles de", top = 791, left = 1628, bottom = 838, right = 1916),
            OcrTextLine("salade", top = 832, left = 1726, bottom = 862, right = 1817),
            OcrTextLine("1 tomate", top = 882, left = 1715, bottom = 906, right = 1839),
            OcrTextLine("sel et poivre", top = 920, left = 1686, bottom = 957, right = 1864),
            OcrTextLine("|20 mn", top = 1012, left = 1890, bottom = 1030, right = 1933),
            OcrTextLine("Notre", top = 1068, left = 1789, bottom = 1092, right = 1883),
            OcrTextLine("conseil vin", top = 1106, left = 1748, bottom = 1134, right = 1926),
            OcrTextLine("BOURGOGNE", top = 1166, left = 1728, bottom = 1193, right = 1952),
            OcrTextLine("CHARDONNAY", top = 1211, left = 1730, bottom = 1242, right = 1951),
            OcrTextLine("Michel Laroche", top = 1259, left = 1727, bottom = 1290, right = 1959),
            OcrTextLine("MO, 1993, 75 cl", top = 1311, left = 1734, bottom = 1344, right = 1956),
            OcrTextLine("Prix indicatif au", top = 1366, left = 1743, bottom = 1392, right = 1954),
            OcrTextLine("14/12/94: 209 F", top = 1405, left = 1743, bottom = 1434, right = 1957),
            OcrTextLine("LUIre TOugp MO 100s", top = 1465, left = 1391, bottom = 1485, right = 1747),
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 14 doit valoir 'Puits de concombre aux œufs de truite'")
            .that(result.title)
            .isEqualTo("Puits de concombre aux œufs de truite")

        // 2. Méta-données de Temps
        assertWithMessage("Le temps de préparation doit valoir '120'")
            .that(result.prepTime)
            .isEqualTo("120")

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
        assertWithMessage("La recommandation de vin doit valoir 'BOURGOGNE CHARDONNAY Michel Laroche M.O., 1993, 75 cl'")
            .that(result.wine)
            .isEqualTo("BOURGOGNE CHARDONNAY Michel Laroche M.O., 1993, 75 cl")

        // 6. Source
        assertWithMessage("La source doit valoir null")
            .that(result.source)
            .isNull()

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "1 grand concombre",
            "1 petit pot d'œufs de truite (50 g)",
            "250 g de fromage blanc",
            "1 c. à soupe de cerfeuil haché",
            "1 c. à soupe de ciboulette hachée",
            "1 c. à soupe d'aneth haché",
            "1 c. à soupe d'oignons frais hachés",
            "Quelques feuilles de salade",
            "1 tomate",
            "Sel et poivre",
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Découpez deux grands tronÇons de 1/2 cm dans le concombre.",
            "Creusez des sillons tout autour à l'aide d'un zesteur.",
            "Découpez chaque tronçon en 4 vous obtenez 8 petits tronçons d'3 cm de haut.",
            "Creusez-les à l'aide d'une petite cuillère salez l'intérieur et retournez-les sur une grille pour les faire dégorger.",
            "Pendant ce temps, préparez la farce: mélangez le fromage blanc avec les herbes et les oignons, ainsi que la moitié des œufs de truite.",
            "Rectifiez l'assaisonnement.",
            "Versez cette préparation dans une grande poche à douille.",
            "Farcissez les puits de concombre bien égouttés avec cette préparation.",
            "Décorez le dessus avec le restant des œufs de truite.",
            "Mettez au frais jusqu'au moment de servir.",
            "Décorez les assiettes de lamelles de tomates et de salade.",
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
