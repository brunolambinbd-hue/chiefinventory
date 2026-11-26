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
 * Test E2E de référence 07 sur 25 : 'Bûchettes de melon au jambon italien et au porto'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe07BuchettesMelonTest {

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
    fun testRecipe07_BuchettesMelon_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 86, left = 114, bottom = 122, right = 380),
            OcrTextLine("POUR", top = 126, left = 194, bottom = 155, right = 300),
            OcrTextLine("4 PERSONNES", top = 162, left = 119, bottom = 190, right = 378),
            OcrTextLine("I melon Galia", top = 208, left = 175, bottom = 238, right = 337),
            OcrTextLine("8 tranches de jambon", top = 255, left = 116, bottom = 283, right = 382),
            OcrTextLine("italien", top = 288, left = 212, bottom = 312, right = 289),
            OcrTextLine("4c. à soupe de porto", top = 333, left = 120, bottom = 363, right = 386),
            OcrTextLine("l c.à soupe de vinaigre de", top = 372, left = 101, bottom = 404, right = 416),
            OcrTextLine("framboise", top = 406, left = 185, bottom = 437, right = 311),
            OcrTextLine("4c. à soupe de yoghourt", top = 450, left = 100, bottom = 482, right = 407),
            OcrTextLine("4 c. à soupe d' huile", top = 491, left = 131, bottom = 523, right = 370),
            OcrTextLine("d'olive", top = 528, left = 210, bottom = 552, right = 295),
            OcrTextLine("l c.à café de confiture de", top = 569, left = 108, bottom = 603, right = 412),
            OcrTextLine("framboises", top = 604, left = 181, bottom = 635, right = 320),
            OcrTextLine("4 branches d' aneth", top = 645, left = 134, bottom = 674, right = 370),
            OcrTextLine("sel et poivre", top = 693, left = 181, bottom = 721, right = 329),
            OcrTextLine("Büchettes de melon", top = 608, left = 454, bottom = 778, right = 1392),
            OcrTextLine("au jambon italien et au porto", top = 740, left = 672, bottom = 865, right = 1827),
            OcrTextLine("• Pelez le melon, épépinez-le et", top = 910, left = 93, bottom = 951, right = 623),
            OcrTextLine("coupez la chair en fines", top = 954, left = 91, bottom = 1001, right = 489),
            OcrTextLine("lamelles.", top = 1009, left = 93, bottom = 1040, right = 243),
            OcrTextLine("•Coupez les tranches de", top = 1089, left = 93, bottom = 1142, right = 506),
            OcrTextLine("jambon en deux dans le sens de", top = 1132, left = 89, bottom = 1187, right = 626),
            OcrTextLine("la longueur et déposez-y les", top = 1181, left = 94, bottom = 1234, right = 565),
            OcrTextLine("lamelles de melon. Roulez les", top = 1227, left = 94, bottom = 1271, right = 598),
            OcrTextLine("tranches de façon à obtenir", top = 1272, left = 96, bottom = 1323, right = 551),
            OcrTextLine("4 petites bûches par personne.", top = 1323, left = 95, bottom = 1368, right = 600),
            OcrTextLine("• Mixez le porto, le vinaigre, le", top = 941, left = 686, bottom = 989, right = 1191),
            OcrTextLine("yoghourt, l'huile d'olive et la", top = 983, left = 682, bottom = 1037, right = 1165),
            OcrTextLine("confiture de framboises pour", top = 1036, left = 683, bottom = 1074, right = 1159),
            OcrTextLine("en faire une vinaigrette.", top = 1082, left = 683, bottom = 1123, right = 1070),
            OcrTextLine("Assaisonnez généreusement.", top = 1124, left = 684, bottom = 1171, right = 1154),
            OcrTextLine("Assiette de présentation disponible dans les Maxi GB", top = 565, left = 1375, bottom = 602, right = 1992),
            OcrTextLine("• Disposez les bûchettes sur", top = 1212, left = 698, bottom = 1263, right = 1140),
            OcrTextLine("une assiette avec la vinaigrette.", top = 1260, left = 686, bottom = 1305, right = 1195),
            OcrTextLine("Décorez de brins d'aneth.", top = 1304, left = 688, bottom = 1345, right = 1106),
            OcrTextLine("VIN CONSEILLE", top = 1169, left = 1454, bottom = 1217, right = 1781),
            OcrTextLine("Atinum, Bordeaux Graves", top = 1228, left = 1396, bottom = 1264, right = 1778),
            OcrTextLine("MO, 1995, 75 cl", top = 1271, left = 1541, bottom = 1308, right = 1780),
            OcrTextLine("EAt", top = 897, left = 1855, bottom = 910, right = 1903),
            OcrTextLine("ATINUM", top = 1052, left = 1843, bottom = 1073, right = 1931),
            OcrTextLine("GRAVES", top = 1146, left = 1843, bottom = 1164, right = 1937)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 07 doit valoir 'Bûchettes de melon au jambon italien et au porto'")
            .that(result.title)
            .isEqualTo("Bûchettes de melon au jambon italien et au porto")

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
        assertWithMessage("La recommandation de vin doit valoir 'Atinium, Bordeaux Graves M.O., 1995, 75 cl'")
            .that(result.wine)
            .isEqualTo("Atinium, Bordeaux Graves M.O., 1995, 75 cl")

        // 6. Source
        assertWithMessage("La source doit valoir 'Maxi GB'")
            .that(result.source)
            .isEqualTo("Maxi GB")

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "1 melon Galia",
            "8 tranches de jambon italien",
            "4 c. à soupe de porto",
            "1 c. à soupe de vinaigre de framboise",
            "4 c. à soupe de yoghourt",
            "4 c. à soupe d'huile d'olive",
            "1 c. à café de confiture de framboises",
            "4 branches d'aneth",
            "Sel et poivre"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste des ingrédients doit correspondre exactement aux éléments attendus")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Pelez le melon, épépinez-le et coupez la chair en fines lamelles.",
            "Coupez les tranches de jambon en deux dans le sens de la longueur et déposez-y les lamelles de melon.",
            "Roulez les tranches de façon à obtenir 4 petites bûches par personne.",
            "Mixez le porto, le vinaigre, le yoghourt, l'huile d'olive et la confiture de framboises pour en faire une vinaigrette.",
            "Assaisonnez généreusement.",
            "Disposez les bûchettes sur une assiette avec la vinaigrette.",
            "Décorez de brins d'aneth."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste des instructions doit correspondre exactement aux phrases attendues")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
