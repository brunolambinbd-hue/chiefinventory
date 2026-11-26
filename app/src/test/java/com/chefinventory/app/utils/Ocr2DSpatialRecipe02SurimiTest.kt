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
 * Test E2E de référence 02 sur 25 : 'Salade de surimi'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe02SurimiTest {

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
    fun testRecipe02_SaladeSurimi_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 87, left = 90, bottom = 123, right = 349),
            OcrTextLine("POUR", top = 129, left = 169, bottom = 155, right = 272),
            OcrTextLine("4 PERSONNES", top = 158, left = 94, bottom = 189, right = 344),
            OcrTextLine("120 g de surimi (poisson", top = 210, left = 74, bottom = 238, right = 369),
            OcrTextLine("au got de crabe) ou", top = 238, left = 103, bottom = 271, right = 344),
            OcrTextLine("de véritable crabe", top = 273, left = 112, bottom = 299, right = 328),
            OcrTextLine("2 c.à soupe", top = 314, left = 150, bottom = 346, right = 296),
            OcrTextLine("de mayonnaise", top = 348, left = 133, bottom = 378, right = 311),
            OcrTextLine("Ic.à soupe de cậpres", top = 388, left = 100, bottom = 422, right = 354),
            OcrTextLine("2 c. à soupe", top = 430, left = 151, bottom = 460, right = 294),
            OcrTextLine("de persil haché", top = 464, left = 131, bottom = 497, right = 314),
            OcrTextLine("2 æufs durs", top = 505, left = 170, bottom = 538, right = 291),
            OcrTextLine("4 toasts", top = 552, left = 176, bottom = 574, right = 270),
            OcrTextLine("salade de blé", top = 590, left = 144, bottom = 617, right = 303),
            OcrTextLine("le jus de I/2 citron", top = 630, left = 112, bottom = 665, right = 335),
            OcrTextLine("sel et poivre", top = 676, left = 152, bottom = 706, right = 297),
            OcrTextLine("Salade de surimi", top = 609, left = 487, bottom = 746, right = 1256),
            OcrTextLine("• Hachez finement le surimi ou", top = 804, left = 66, bottom = 848, right = 578),
            OcrTextLine("le crabe ainsi que les œufs", top = 852, left = 67, bottom = 898, right = 501),
            OcrTextLine("durs.", top = 907, left = 66, bottom = 939, right = 140),
            OcrTextLine("• Mélangez avec la", top = 990, left = 67, bottom = 1037, right = 383),
            OcrTextLine("mayonnaise, le persil, le jus de", top = 1028, left = 67, bottom = 1085, right = 578),
            OcrTextLine("citron, les cấpres, du sel et du", top = 1073, left = 66, bottom = 1130, right = 558),
            OcrTextLine("poivre.", top = 1134, left = 66, bottom = 1178, right = 181),
            OcrTextLine("• Disposez un peu de salade de", top = 787, left = 646, bottom = 839, right = 1139),
            OcrTextLine("blé en forme de cercle sur une", top = 833, left = 646, bottom = 876, right = 1129),
            OcrTextLine("assiette et déposez-y la salade", top = 874, left = 646, bottom = 929, right = 1129),
            OcrTextLine("de surimi.", top = 930, left = 647, bottom = 966, right = 807),
            OcrTextLine("Assiette de présentation disponible dans les Maxi GB", top = 531, left = 1323, bottom = 577, right = 1934),
            OcrTextLine("• Servez avec les toasts.", top = 778, left = 1208, bottom = 815, right = 1581),
            OcrTextLine("VIN CONSEILLÉ", top = 1128, left = 1416, bottom = 1187, right = 1737),
            OcrTextLine("Atinum, Bordeaur blanc,", top = 1191, left = 1336, bottom = 1237, right = 1732),
            OcrTextLine("MO, 1996, 75 cl", top = 1232, left = 1499, bottom = 1279, right = 1739),
            OcrTextLine("4TINLIN", top = 1000, left = 1794, bottom = 1036, right = 1885)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 02 doit valoir 'Salade de surimi'")
            .that(result.title)
            .isEqualTo("Salade de surimi")

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
        assertWithMessage("La recommandation de vin doit capturer le Bordeaux blanc M.O. 1996")
            .that(result.wine)
            .contains("Bordeaux blanc")

        // 6. Source / Enseigne
        assertWithMessage("La source doit valoir 'Maxi GB'")
            .that(result.source)
            .isEqualTo("Maxi GB")

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "120 g de surimi (poisson au goût de crabe) ou de véritable crabe",
            "2 c. à soupe de mayonnaise",
            "1 c. à soupe de câpres",
            "2 c. à soupe de persil haché",
            "2 œufs durs",
            "4 toasts",
            "Salade de blé",
            "Le jus de 1/2 citron",
            "Sel et poivre"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Hachez finement le surimi ou le crabe ainsi que les œufs durs.",
            "Mélangez avec la mayonnaise, le persil, le jus de citron, les câpres, du sel et du poivre.",
            "Disposez un peu de salade de blé en forme de cercle sur une assiette et déposez-y la salade de surimi.",
            "Servez avec les toasts."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
