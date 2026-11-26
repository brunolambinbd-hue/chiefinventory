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
 * Test E2E de référence 15 sur 25 : 'Pommes de terre farcies aux radis et aux herbes'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe15PommesDeTerreRadisHerbesTest {

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
    fun testRecipe15_PommesDeTerreRadisHerbes_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 85, left = 111, bottom = 123, right = 380),
            OcrTextLine("POUR", top = 125, left = 192, bottom = 155, right = 299),
            OcrTextLine("4 PERSONNES", top = 162, left = 116, bottom = 191, right = 377),
            OcrTextLine("4 nouvelles pommes de", top = 209, left = 101, bottom = 240, right = 389),
            OcrTextLine("terre", top = 253, left = 220, bottom = 270, right = 277),
            OcrTextLine("4 noix de beurre", top = 291, left = 145, bottom = 313, right = 350),
            OcrTextLine("8c.", top = 333, left = 113, bottom = 361, right = 157),
            OcrTextLine("à soupe de crème", top = 333, left = 186, bottom = 361, right = 383),
            OcrTextLine("fouettée", top = 369, left = 196, bottom = 397, right = 299),
            OcrTextLine("4 radis", top = 412, left = 206, bottom = 434, right = 293),
            OcrTextLine("2 gousses d'ail", top = 449, left = 156, bottom = 483, right = 342),
            OcrTextLine("lc. à soupe de persil", top = 496, left = 132, bottom = 529, right = 386),
            OcrTextLine("haché", top = 529, left = 215, bottom = 554, right = 286),
            OcrTextLine("Ic.à soupe de ciboulette", top = 570, left = 107, bottom = 605, right = 401),
            OcrTextLine("hachée", top = 610, left = 209, bottom = 632, right = 295),
            OcrTextLine("l c. à soupe de cerfeuil", top = 650, left = 124, bottom = 682, right = 396),
            OcrTextLine("haché", top = 685, left = 215, bottom = 711, right = 288),
            OcrTextLine("lc.à soupe d'estragon", top = 731, left = 122, bottom = 760, right = 396),
            OcrTextLine("haché", top = 765, left = 217, bottom = 788, right = 291),
            OcrTextLine("cressonnette", top = 811, left = 177, bottom = 832, right = 326),
            OcrTextLine("sel et poivre", top = 850, left = 178, bottom = 879, right = 328),
            OcrTextLine("40", top = 246, left = 474, bottom = 278, right = 522),
            OcrTextLine("• Faites cuire les pommes de", top = 927, left = 85, bottom = 968, right = 567),
            OcrTextLine("terre en robe des champs (non", top = 973, left = 84, bottom = 1014, right = 592),
            OcrTextLine("épluchées, dans 1'eau)", top = 1020, left = 84, bottom = 1062, right = 460),
            OcrTextLine("et laissez-les ensuite refroidir.", top = 1062, left = 84, bottom = 1104, right = 587),
            OcrTextLine("• Pendant ce temps, hachez fin", top = 1156, left = 86, bottom = 1201, right = 604),
            OcrTextLine("les radis et les gousses d'ail et", top = 1202, left = 86, bottom = 1247, right = 597),
            OcrTextLine("mélangez-les à la crème", top = 1247, left = 86, bottom = 1297, right = 496),
            OcrTextLine("fouettée et aux herbes.", top = 1295, left = 87, bottom = 1335, right = 465),
            OcrTextLine("Pomnes de terre farcies", top = 616, left = 620, bottom = 748, right = 1660),
            OcrTextLine("aux radis et aux berbes", top = 750, left = 670, bottom = 861, right = 1753),
            OcrTextLine("• Quand les pommes de terre", top = 919, left = 679, bottom = 960, right = 1154),
            OcrTextLine("sont refroidies, coupez-leur un", top = 964, left = 679, bottom = 1005, right = 1182),
            OcrTextLine("chapeau et videz-les.", top = 1006, left = 678, bottom = 1056, right = 1006),
            OcrTextLine("Ecrasez la chair avec le beurre", top = 1053, left = 679, bottom = 1092, right = 1175),
            OcrTextLine("et assaisonnez.", top = 1104, left = 680, bottom = 1139, right = 925),
            OcrTextLine("Assiette de présentation disponible dans les Maxi GB", top = 582, left = 1378, bottom = 621, right = 2007),
            OcrTextLine("• Farcissez les pommes de terre", top = 1193, left = 682, bottom = 1234, right = 1202),
            OcrTextLine("de cette purée et mettez-les", top = 1236, left = 682, bottom = 1282, right = 1131),
            OcrTextLine("10 minutes au four à 170°C.", top = 1279, left = 687, bottom = 1326, right = 1149),
            OcrTextLine("• Disposez-les ensuite sur un lit", top = 906, left = 1260, bottom = 951, right = 1766),
            OcrTextLine("de cresSonnette avec, par-", top = 962, left = 1259, bottom = 993, right = 1678),
            OcrTextLine("dessus, la crème aux radis et", top = 999, left = 1259, bottom = 1041, right = 1718),
            OcrTextLine("aux herbes qui les rendra bien", top = 1040, left = 1260, bottom = 1087, right = 1743),
            OcrTextLine("fondantes.", top = 1097, left = 1260, bottom = 1127, right = 1425),
            OcrTextLine("VIN CONSEILLE", top = 1176, left = 1456, bottom = 1225, right = 1784),
            OcrTextLine("Bourgueil Les Chaînaies", top = 1247, left = 1415, bottom = 1287, right = 1785),
            OcrTextLine("Loire rouge, M.0., 1995, 7S cl", top = 1294, left = 1330, bottom = 1335, right = 1787),
            OcrTextLine("BOURGUEIL", top = 1239, left = 1840, bottom = 1270, right = 1961),
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 15 doit valoir 'Pommes de terre farcies aux radis et aux herbes'")
            .that(result.title)
            .isEqualTo("Pommes de terre farcies aux radis et aux herbes")

        // 2. Méta-données de Temps
        assertWithMessage("Le temps de préparation doit valoir '40'")
            .that(result.prepTime)
            .isEqualTo("40")

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
        assertWithMessage("La recommandation de vin doit valoir 'Bourgueil les Chaînaies Loire rouge, M.O., 1995, 7 s cl'")
            .that(result.wine)
            .isEqualTo("Bourgueil les Chaînaies Loire rouge, M.O., 1995, 7 s cl")

        // 6. Source
        assertWithMessage("La source doit valoir 'Maxi GB'")
            .that(result.source)
            .isEqualTo("Maxi GB")

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "4 nouvelles pommes de terre",
            "4 noix de beurre",
            "8 c. à soupe de crème fouettée",
            "4 radis",
            "2 gousses d'ail",
            "1 c. à soupe de persil haché",
            "1 c. à soupe de ciboulette hachée",
            "1 c. à soupe de cerfeuil haché",
            "1 c. à soupe d'estragon haché",
            "Cressonnette",
            "Sel et poivre",
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Faites cuire les pommes de terre en robe des champs (non épluchées, dans l'eau) et laissez-les ensuite refroidir.",
            "Pendant ce temps, hachez fin les radis et les gousses d'ail et mélangez-les à la crème fouettée et aux herbes.",
            "Quand les pommes de terre sont refroidies, coupez-leur un chapeau et videz-les.",
            "Ecrasez la chair avec le beurre et assaisonnez.",
            "Farcissez les pommes de terre de cette purée et mettez-les 10 minutes au four à 170°C.",
            "Disposez-les ensuite sur un lit de cressonnette avec, par-dessus, la crème aux radis et aux herbes qui les rendra bien fondantes.",
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
