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
 * Test E2E de référence 13 sur 25 : 'Pain frais au beurre d'avocat'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe13PainFraisBeurreAvocatTest {

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
    fun testRecipe13_PainFraisBeurreAvocat_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("rec ette", top = 73, left = 31, bottom = 123, right = 429),
            OcrTextLine("INGRÉDIENTS", top = 253, left = 112, bottom = 293, right = 379),
            OcrTextLine("POUR", top = 296, left = 193, bottom = 325, right = 299),
            OcrTextLine("4 PERSONNES", top = 331, left = 116, bottom = 362, right = 374),
            OcrTextLine("2 avocats bien mûrs", top = 380, left = 136, bottom = 409, right = 367),
            OcrTextLine("I citron (112 pour le jus -", top = 421, left = 107, bottom = 457, right = 398),
            OcrTextLine("1/2 pour la décoration)", top = 459, left = 101, bottom = 489, right = 386),
            OcrTextLine("100 g de beurre", top = 501, left = 148, bottom = 533, right = 340),
            OcrTextLine("4 tranches de pain", top = 542, left = 129, bottom = 576, right = 355),
            OcrTextLine("4 tranches de saumon", top = 585, left = 106, bottom = 618, right = 375),
            OcrTextLine("fumé", top = 625, left = 208, bottom = 652, right = 273),
            OcrTextLine("Ic. à soupe d'oignons", top = 664, left = 119, bottom = 700, right = 378),
            OcrTextLine("hachés", top = 702, left = 199, bottom = 725, right = 284),
            OcrTextLine("lc.à soupe de persil", top = 741, left = 111, bottom = 776, right = 371),
            OcrTextLine("haché", top = 780, left = 204, bottom = 803, right = 277),
            OcrTextLine("sel et poivre", top = 821, left = 164, bottom = 851, right = 313),
            OcrTextLine("20", top = 416, left = 474, bottom = 463, right = 519),
            OcrTextLine("JPain frais au beurre d'avocat", top = 830, left = 442, bottom = 967, right = 1888),
            OcrTextLine("• Faites une purée avec les", top = 1149, left = 65, bottom = 1187, right = 511),
            OcrTextLine("avocats, le beurre et le jus de", top = 1195, left = 63, bottom = 1232, right = 553),
            OcrTextLine("1/2 citron. Salez et poivrez.", top = 1235, left = 66, bottom = 1279, right = 525),
            OcrTextLine("•Coupez les tranches de pain", top = 1331, left = 63, bottom = 1373, right = 561),
            OcrTextLine("en petits carrés de 6 cm x 6 cm.", top = 1379, left = 62, bottom = 1415, right = 594),
            OcrTextLine("Tartinez-les de beurre d'avocat", top = 1424, left = 62, bottom = 1455, right = 585),
            OcrTextLine("et recouvrez-les de saumon fumé.", top = 1471, left = 63, bottom = 1502, right = 600),
            OcrTextLine("Assiette de présentation disponible dans les Maxi GB", top = 770, left = 1375, bottom = 804, right = 2019),
            OcrTextLine("•Parsemez le tout d'oignons et", top = 1148, left = 664, bottom = 1191, right = 1182),
            OcrTextLine("de persil et décorez d'une", top = 1198, left = 663, bottom = 1235, right = 1092),
            OcrTextLine("rondelle de citron.", top = 1244, left = 663, bottom = 1274, right = 964),
            OcrTextLine("VIN CONSEILLE", top = 1386, left = 1438, bottom = 1433, right = 1775),
            OcrTextLine("Marqués de Cáceres", top = 1446, left = 1460, bottom = 1485, right = 1773),
            OcrTextLine("Rioja blanc, M.0., 1996, 75 cl", top = 1489, left = 1309, bottom = 1529, right = 1773),
            OcrTextLine("Maques", top = 1404, left = 1855, bottom = 1426, right = 1931),
            OcrTextLine("eces", top = 1423, left = 1846, bottom = 1444, right = 1940)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 13 doit valoir 'Pain frais au beurre d'avocat'")
            .that(result.title)
            .isEqualTo("Pain frais au beurre d'avocat")

        // 2. Méta-données de Temps
        assertWithMessage("Le temps de préparation doit valoir '20'")
            .that(result.prepTime)
            .isEqualTo("20")

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
        assertWithMessage("La recommandation de vin doit valoir 'Marqués de Cáceres Rioja blanc, M.O., 1996, 75 cl'")
            .that(result.wine)
            .isEqualTo("Marqués de Cáceres Rioja blanc, M.O., 1996, 75 cl")

        // 6. Source
        assertWithMessage("La source doit valoir 'Maxi GB'")
            .that(result.source)
            .isEqualTo("Maxi GB")

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "2 avocats bien mûrs",
            "1 citron (1/2 pour le jus - 1/2 pour la décoration)",
            "100 g de beurre",
            "4 tranches de pain",
            "4 tranches de saumon fumé",
            "1 c. à soupe d'oignons hachés",
            "1 c. à soupe de persil haché",
            "Sel et poivre"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Faites une purée avec les avocats, le beurre et le jus de 1/2 citron.",
            "Salez et poivrez.",
            "Coupez les tranches de pain en petits carrés de 6 cm x 6 cm.",
            "Tartinez-les de beurre d'avocat et recouvrez-les de saumon fumé.",
            "Parsemez le tout d'oignons et de persil et décorez d'une rondelle de citron."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
