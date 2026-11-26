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
 * Test E2E de référence 04 sur 25 : 'Salade d'avocat et princesses'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe04AvocatTest {

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
    fun testRecipe04_SaladeAvocatPrincesses_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 98, left = 92, bottom = 133, right = 355),
            OcrTextLine("POUR", top = 136, left = 171, bottom = 167, right = 274),
            OcrTextLine("4 PERSONNES", top = 172, left = 97, bottom = 200, right = 350),
            OcrTextLine("I avocat", top = 222, left = 188, bottom = 247, right = 276),
            OcrTextLine("200 g de haricots", top = 260, left = 118, bottom = 291, right = 327),
            OcrTextLine("princesses", top = 295, left = 158, bottom = 326, right = 285),
            OcrTextLine("200 g de saumon cru", top = 341, left = 96, bottom = 368, right = 349),
            OcrTextLine("aneth", top = 383, left = 190, bottom = 405, right = 257),
            OcrTextLine("6c.à soupe", top = 423, left = 161, bottom = 453, right = 297),
            OcrTextLine("d'huile d'olive", top = 455, left = 139, bottom = 482, right = 305),
            OcrTextLine("le jus de lI2 citron", top = 502, left = 111, bottom = 529, right = 335),
            OcrTextLine("lc. à café de miel", top = 541, left = 115, bottom = 573, right = 333),
            OcrTextLine("1c.à soupe de vinaigre", top = 586, left = 87, bottom = 617, right = 366),
            OcrTextLine("de vin blanc", top = 621, left = 149, bottom = 644, right = 298),
            OcrTextLine("sel et poivre", top = 664, left = 150, bottom = 692, right = 296),
            OcrTextLine("• Faites cuire les haricots", top = 830, left = 52, bottom = 863, right = 429),
            OcrTextLine("princesses 10 à 15 minutes jusqu'à", top = 877, left = 51, bottom = 915, right = 573),
            OcrTextLine("ce que vous puissiez y piquer", top = 922, left = 52, bottom = 962, right = 494),
            OcrTextLine("facilement une fourchette.", top = 969, left = 52, bottom = 1000, right = 445),
            OcrTextLine("Retirez le jus de cuisson et passez", top = 1015, left = 52, bottom = 1050, right = 565),
            OcrTextLine("les haricots sous un jet d'eau", top = 1057, left = 53, bottom = 1094, right = 480),
            OcrTextLine("froide.", top = 1107, left = 51, bottom = 1137, right = 149),
            OcrTextLine("•Coupez les haricots en deux.", top = 1195, left = 53, bottom = 1234, right = 504),
            OcrTextLine("• Coupez l'avocat en petits dés et", top = 1286, left = 64, bottom = 1326, right = 546),
            OcrTextLine("hachez 1aneth.", top = 1336, left = 51, bottom = 1371, right = 268),
            OcrTextLine("alade d'avocat et princesses", top = 658, left = 698, bottom = 787, right = 1900),
            OcrTextLine("Coupez le saumon en dés de", top = 825, left = 655, bottom = 864, right = 1080),
            OcrTextLine("5 mm sur 5 mm.", top = 879, left = 633, bottom = 904, right = 878),
            OcrTextLine("• Préparez une vinaigrette avec", top = 967, left = 634, bottom = 1003, right = 1100),
            OcrTextLine("T'huile d'olive, le jus de citron, le", top = 1012, left = 634, bottom = 1047, right = 1125),
            OcrTextLine("miel, le vinaigre, du sel et du", top = 1058, left = 633, bottom = 1093, right = 1071),
            OcrTextLine("poivre.", top = 1102, left = 633, bottom = 1139, right = 732),
            OcrTextLine("• Mélangez tous ces ingrédients", top = 1197, left = 634, bottom = 1233, right = 1122),
            OcrTextLine("dans un saladier jusqu'à ce que", top = 1240, left = 632, bottom = 1281, right = 1098),
            OcrTextLine("vous obteniez une belle salade", top = 1286, left = 633, bottom = 1321, right = 1092),
            OcrTextLine("mêlée.", top = 1336, left = 634, bottom = 1363, right = 731),
            OcrTextLine("•Répartissez la salade sur", top = 827, left = 1216, bottom = 862, right = 1610),
            OcrTextLine("4 assiettes et décorez avec des", top = 874, left = 1212, bottom = 901, right = 1673),
            OcrTextLine("brins d'aneth.", top = 920, left = 1213, bottom = 947, right = 1413),
            OcrTextLine("VIN CONSEILLÉ", top = 1199, left = 1420, bottom = 1247, right = 1762),
            OcrTextLine("Alban de Saint-Pré", top = 1262, left = 1460, bottom = 1295, right = 1760),
            OcrTextLine("Saumur blanc, sec", top = 1305, left = 1464, bottom = 1350, right = 1742),
            OcrTextLine("M.0., 1995,75 cl", top = 1355, left = 1489, bottom = 1394, right = 1763)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 04 doit valoir 'Salade d'avocat et princesses'")
            .that(result.title)
            .isEqualTo("Salade d'avocat et princesses")

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
        assertWithMessage("La recommandation de vin doit valoir 'Alban de Saint-Pré Saumur blanc, sec M.O., 1995,75 cl'")
            .that(result.wine)
            .isEqualTo("Alban de Saint-Pré Saumur blanc, sec M.O., 1995,75 cl")

        // 6. Source
        assertWithMessage("La source doit valoir null")
            .that(result.source)
            .isNull()

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "1 avocat",
            "200 g de haricots princesses",
            "200 g de saumon cru",
            "Aneth",
            "6 c. à soupe d'huile d'olive",
            "Le jus de 1/2 citron",
            "1 c. à café de miel",
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
            "Faites cuire les haricots princesses 10 à 15 minutes jusqu'à ce que vous puissiez y piquer facilement une fourchette.",
            "Retirez le jus de cuisson et passez les haricots sous un jet d'eau froide.",
            "Coupez les haricots en deux.",
            "Coupez l'avocat en petits dés et hachez 1 aneth.",
            "Coupez le saumon en dés de 5 mm sur 5 mm.",
            "Préparez une vinaigrette avec l'huile d'olive, le jus de citron, le miel, le vinaigre, du sel et du poivre.",
            "Mélangez tous ces ingrédients dans un saladier jusqu'à ce que vous obteniez une belle salade mêlée.",
            "Répartissez la salade sur 4 assiettes et décorez avec des brins d'aneth."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
