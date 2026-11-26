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
 * Test E2E de référence 06 sur 25 : 'Asperges vertes au parmesan'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe06AspergesTest {

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
    fun testRecipe06_AspergesParmesan_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("Cet te", top = 84, left = 72, bottom = 144, right = 332),
            OcrTextLine("INGRÉDIENTS", top = 271, left = 102, bottom = 305, right = 371),
            OcrTextLine("POUR", top = 312, left = 184, bottom = 339, right = 290),
            OcrTextLine("4 PERSONNES", top = 341, left = 106, bottom = 375, right = 364),
            OcrTextLine("1 kg d'asperges vertes", top = 397, left = 114, bottom = 426, right = 375),
            OcrTextLine("4 tranches", top = 439, left = 173, bottom = 461, right = 303),
            OcrTextLine("de jambon séché", top = 469, left = 134, bottom = 501, right = 340),
            OcrTextLine("4 c. à soupe", top = 512, left = 162, bottom = 546, right = 311),
            OcrTextLine("de parmesan", top = 549, left = 162, bottom = 578, right = 320),
            OcrTextLine("(moulu ou en morceaux)", top = 585, left = 88, bottom = 611, right = 388),
            OcrTextLine("6c. à soupe", top = 624, left = 164, bottom = 655, right = 312),
            OcrTextLine("d'huile d'olive", top = 661, left = 147, bottom = 685, right = 329),
            OcrTextLine("le jus de 12 citron", top = 700, left = 123, bottom = 733, right = 351),
            OcrTextLine("GB,", top = 114, left = 438, bottom = 155, right = 492),
            OcrTextLine("(20", top = 426, left = 460, bottom = 464, right = 515),
            OcrTextLine("• Etuvez les asperges pendant 8 à", top = 1159, left = 71, bottom = 1204, right = 588),
            OcrTextLine("10 minutes en veillant à les laisser", top = 1202, left = 80, bottom = 1247, right = 606),
            OcrTextLine("croquantes (il n'est pas nécessaire", top = 1246, left = 70, bottom = 1303, right = 602),
            OcrTextLine("d'éplucher les asperges vertes).", top = 1299, left = 70, bottom = 1347, right = 557),
            OcrTextLine("Assiette de présentation disponible dans les Maxi GB", top = 754, left = 1368, bottom = 796, right = 1993),
            OcrTextLine("sperges vertes au parmesan", top = 860, left = 694, bottom = 1004, right = 1933),
            OcrTextLine("• Répartissez-les sur 4 assiettes,", top = 1144, left = 672, bottom = 1190, right = 1148),
            OcrTextLine("déposez sur chacune d'elles une", top = 1188, left = 671, bottom = 1236, right = 1157),
            OcrTextLine("tranche de jambon séchế.", top = 1238, left = 672, bottom = 1282, right = 1051),
            OcrTextLine("Saupoudrez de parmesan.", top = 1287, left = 673, bottom = 1330, right = 1058),
            OcrTextLine("• Nappez d'huile d'olive et", top = 1128, left = 1254, bottom = 1177, right = 1652),
            OcrTextLine("arrosez de jus de citron.", top = 1178, left = 1255, bottom = 1220, right = 1602),
            OcrTextLine("VIN CONSEILLE", top = 1355, left = 1457, bottom = 1407, right = 1787),
            OcrTextLine("Muscat d'Alsace, caves de", top = 1419, left = 1391, bottom = 1456, right = 1788),
            OcrTextLine("Pfaffenheim, MO, 1996, 75 cl", top = 1462, left = 1344, bottom = 1509, right = 1790),
            OcrTextLine("ind", top = 1377, left = 1865, bottom = 1401, right = 1901)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 06 doit valoir 'Asperges vertes au parmesan'")
            .that(result.title)
            .isEqualTo("Asperges vertes au parmesan")

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
        assertWithMessage("La recommandation de vin doit valoir 'Muscat d'Alsace, caves de Pfaffenheim, M.O., 1996, 75 cl'")
            .that(result.wine)
            .isEqualTo("Muscat d'Alsace, caves de Pfaffenheim, M.O., 1996, 75 cl")

        // 6. Source
        assertWithMessage("La source doit valoir 'Maxi GB'")
            .that(result.source)
            .isEqualTo("Maxi GB")

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "1 kg d'asperges vertes",
            "4 tranches de jambon séché",
            "4 c. à soupe de parmesan (moulu ou en morceaux)",
            "6 c. à soupe d'huile d'olive",
            "Le jus de 1/2 citron"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste des ingrédients doit correspondre exactement aux éléments attendus")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Etuvez les asperges pendant 8 à 10 minutes en veillant à les laisser croquantes (il n'est pas nécessaire d'éplucher les asperges vertes).",
            "Répartissez-les sur 4 assiettes.",
            "Déposez sur chacune d'elles une tranche de jambon séché.",
            "Saupoudrez de parmesan.",
            "Nappez d'huile d'olive et arrosez de jus de citron."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste des instructions doit correspondre exactement aux phrases attendues")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
