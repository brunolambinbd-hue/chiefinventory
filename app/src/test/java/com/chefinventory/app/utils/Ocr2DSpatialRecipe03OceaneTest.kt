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
 * Test E2E de référence 03 sur 25 : 'Salade océane'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe03OceaneTest {

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
    fun testRecipe03_SaladeOceane_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 39, left = 120, bottom = 75, right = 379),
            OcrTextLine("POUR", top = 77, left = 197, bottom = 107, right = 298),
            OcrTextLine("4 PERSONNES", top = 112, left = 123, bottom = 140, right = 374),
            OcrTextLine("250 g de crevettes", top = 161, left = 139, bottom = 188, right = 357),
            OcrTextLine("grises décortiquées", top = 194, left = 131, bottom = 222, right = 365),
            OcrTextLine("4 petites tomates", top = 235, left = 145, bottom = 262, right = 347),
            OcrTextLine("(ou 2 grosses)", top = 266, left = 170, bottom = 297, right = 331),
            OcrTextLine("l beau chicon", top = 307, left = 182, bottom = 335, right = 331),
            OcrTextLine("112 salade frisée", top = 348, left = 148, bottom = 379, right = 345),
            OcrTextLine("I orange", top = 392, left = 210, bottom = 422, right = 300),
            OcrTextLine("250 g de yaourt nature", top = 436, left = 108, bottom = 463, right = 383),
            OcrTextLine("2 c. à soupe de", top = 477, left = 155, bottom = 505, right = 336),
            OcrTextLine("mayonnaise", top = 512, left = 174, bottom = 538, right = 317),
            OcrTextLine("2 c. à soupe de ketchup", top = 553, left = 103, bottom = 581, right = 384),
            OcrTextLine("lc.à soupe de", top = 595, left = 165, bottom = 623, right = 335),
            OcrTextLine("ciboulette hachée", top = 625, left = 139, bottom = 651, right = 348),
            OcrTextLine("Tabasco", top = 670, left = 195, bottom = 692, right = 293),
            OcrTextLine("sel et poivre", top = 713, left = 170, bottom = 740, right = 316),
            OcrTextLine("Salade océane", top = 589, left = 484, bottom = 746, right = 1185),
            OcrTextLine("•Prélevez finement le zeste de", top = 810, left = 70, bottom = 842, right = 530),
            OcrTextLine("l'orange, taillez-le en fins bâton-", top = 850, left = 68, bottom = 892, right = 550),
            OcrTextLine("nets que vous plongez 1 minute", top = 896, left = 66, bottom = 941, right = 540),
            OcrTextLine("dans un fond d'eau bouillante.", top = 946, left = 66, bottom = 975, right = 516),
            OcrTextLine("Rafraîchissez à l'eau froide et lais-", top = 980, left = 63, bottom = 1027, right = 584),
            OcrTextLine("sez bien égoutter.", top = 1040, left = 63, bottom = 1074, right = 327),
            OcrTextLine("• Mélangez le yaourt, le ketchup,", top = 1125, left = 62, bottom = 1168, right = 556),
            OcrTextLine("la mayonnaise et la ciboulette.", top = 1169, left = 60, bottom = 1215, right = 516),
            OcrTextLine("Ajoutez du Tabasco selon vos", top = 1217, left = 58, bottom = 1261, right = 509),
            OcrTextLine("goûts, salez et poivrez. Réservez.", top = 1260, left = 57, bottom = 1309, right = 559),
            OcrTextLine("•Lavez et essorez la salade, ajou-", top = 807, left = 646, bottom = 841, right = 1146),
            OcrTextLine("tez-y le chicon nettoyé et émincé.", top = 847, left = 645, bottom = 891, right = 1141),
            OcrTextLine("Disposez sur de grandes assiettes", top = 893, left = 645, bottom = 934, right = 1137),
            OcrTextLine("et saupoudrez de zeste d'orange.", top = 944, left = 645, bottom = 979, right = 1123),
            OcrTextLine("• Coupez les tomates en huit,", top = 1035, left = 645, bottom = 1070, right = 1077),
            OcrTextLine("répartissez-les sur les assiettes et", top = 1081, left = 643, bottom = 1116, right = 1134),
            OcrTextLine("garnissez le centre de crevettes", top = 1127, left = 643, bottom = 1160, right = 1106),
            OcrTextLine("grises.", top = 1170, left = 642, bottom = 1209, right = 732),
            OcrTextLine("• Servez frais (ne laissez pas trop", top = 803, left = 1217, bottom = 842, right = 1707),
            OcrTextLine("longtemps en attente); présentez la", top = 847, left = 1217, bottom = 891, right = 1732),
            OcrTextLine("sauce à part.", top = 896, left = 1217, bottom = 930, right = 1401),
            OcrTextLine("VIN CONSEILLE", top = 1127, left = 1418, bottom = 1170, right = 1755),
            OcrTextLine("Touraine,", top = 1202, left = 1608, bottom = 1236, right = 1752),
            OcrTextLine("Sauvignon blanc, 1995, 75 cl", top = 1249, left = 1311, bottom = 1284, right = 1754),
            OcrTextLine("TOLRAINE", top = 1177, left = 1824, bottom = 1204, right = 1932),
            OcrTextLine("SAETICox", top = 1209, left = 1846, bottom = 1227, right = 1913)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 03 doit valoir 'Salade océane'")
            .that(result.title)
            .isEqualTo("Salade océane")

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
        assertWithMessage("La recommandation de vin doit capturer le Touraine Sauvignon blanc")
            .that(result.wine)
            .contains("Touraine")

        // 6. Source
        assertWithMessage("La source doit valoir null")
            .that(result.source)
            .isNull()

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "250 g de crevettes grises décortiquées",
            "4 petites tomates (ou 2 grosses)",
            "1 beau chicon",
            "1/2 salade frisée",
            "1 orange",
            "250 g de yaourt nature",
            "2 c. à soupe de mayonnaise",
            "2 c. à soupe de ketchup",
            "1 c. à soupe de ciboulette hachée",
            "Tabasco",
            "Sel et poivre"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Prélevez finement le zeste de l'orange, taillez-le en fins bâtonnets que vous plongez 1 minute dans un fond d'eau bouillante.",
            "Rafraîchissez à l'eau froide et laissez bien égoutter.",
            "Mélangez le yaourt, le ketchup la mayonnaise et la ciboulette.",
            "Ajoutez du Tabasco selon vos goûts, salez et poivrez.",
            "Réservez.",
            "Lavez et essorez la salade, ajoutez-y le chicon nettoyé et émincé.",
            "Disposez sur de grandes assiettes et saupoudrez de zeste d'orange.",
            "Coupez les tomates en huit.",
            "Répartissez-les sur les assiettes et garnissez le centre de crevettes grises.",
            "Servez frais (ne laissez pas trop longtemps en attente); présentez la sauce à part."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
