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
 * Test E2E de référence 16 sur 25 : 'Rillettes de saumon fumé aux chicons'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe16RillettesSaumonChiconsTest {

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
    fun testRecipe16_RillettesSaumonChicons_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 75, left = 70, bottom = 110, right = 337),
            OcrTextLine("POUR", top = 114, left = 151, bottom = 144, right = 255),
            OcrTextLine("4 PERSONNES", top = 150, left = 75, bottom = 178, right = 332),
            OcrTextLine("200 g + 4 tranches de", top = 197, left = 67, bottom = 229, right = 337),
            OcrTextLine("saumon fumé", top = 232, left = 122, bottom = 264, right = 284),
            OcrTextLine("300 g de crème fraiche", top = 273, left = 61, bottom = 307, right = 344),
            OcrTextLine("le jus de 1/2 citron", top = 318, left = 90, bottom = 349, right = 318),
            OcrTextLine("2 chicons", top = 364, left = 146, bottom = 387, right = 263),
            OcrTextLine("2 c. à café de persil haché", top = 400, left = 58, bottom = 437, right = 364),
            OcrTextLine("sel et poivre", top = 448, left = 129, bottom = 478, right = 277),
            OcrTextLine("Rillettes de saumon fumé aux chicons", top = 597, left = 343, bottom = 748, right = 1986),
            OcrTextLine("• Hachez à la moulinette les", top = 858, left = 40, bottom = 896, right = 510),
            OcrTextLine("200 g de saumon fumé.", top = 905, left = 40, bottom = 949, right = 433),
            OcrTextLine("Ajoutez-y ensuite la crème", top = 945, left = 38, bottom = 1001, right = 495),
            OcrTextLine("fraîche fouettée avec le jus de", top = 997, left = 40, bottom = 1041, right = 545),
            OcrTextLine("citron. Assaisonnez.", top = 1048, left = 39, bottom = 1084, right = 380),
            OcrTextLine("• Mélangez le tout et laissez", top = 1136, left = 39, bottom = 1186, right = 517),
            OcrTextLine("reposer l heure au", top = 1184, left = 38, bottom = 1235, right = 353),
            OcrTextLine("réfrigérateur.", top = 1235, left = 38, bottom = 1280, right = 259),
            OcrTextLine("•Prenez les feuilles des", top = 847, left = 635, bottom = 889, right = 1027),
            OcrTextLine("chicons et farcissez-les des", top = 892, left = 634, bottom = 935, right = 1084),
            OcrTextLine("rillettes de saumon.", top = 947, left = 635, bottom = 980, right = 961),
            OcrTextLine("Assiette de présentation disponible dans les Maxi GB", top = 552, left = 1342, bottom = 591, right = 1980),
            OcrTextLine("•Assemblez le tout de manière", top = 1029, left = 636, bottom = 1078, right = 1155),
            OcrTextLine("à obtenir de nouveau la forme", top = 1077, left = 655, bottom = 1123, right = 1137),
            OcrTextLine("des chicons et entourez-les", top = 1125, left = 636, bottom = 1165, right = 1086),
            OcrTextLine("de tranches de saumon fumé.", top = 1170, left = 636, bottom = 1212, right = 1122),
            OcrTextLine("• Trempez ensuite l'extrémité", top = 834, left = 1220, bottom = 886, right = 1703),
            OcrTextLine("la plus large des chicons dans", top = 880, left = 1220, bottom = 933, right = 1706),
            OcrTextLine("le persil.", top = 932, left = 1220, bottom = 980, right = 1360),
            OcrTextLine("• Servez comme entrée.", top = 1022, left = 1223, bottom = 1062, right = 1609),
            OcrTextLine("VIN CONSEILLÉ", top = 1164, left = 1424, bottom = 1223, right = 1761),
            OcrTextLine("Pinot gris", top = 1233, left = 1609, bottom = 1272, right = 1760),
            OcrTextLine("Vin de la Moselle luxembourgeoise", top = 1285, left = 1232, bottom = 1320, right = 1762),
            OcrTextLine("MO, 1995, 75 cl", top = 1328, left = 1515, bottom = 1363, right = 1762),
            OcrTextLine("PINOT GRIS", top = 1247, left = 1837, bottom = 1273, right = 1914),
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 16 doit valoir 'Rillettes de saumon fumé aux chicons'")
            .that(result.title)
            .isEqualTo("Rillettes de saumon fumé aux chicons")

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
        assertWithMessage("La recommandation de vin doit valoir 'Pinot gris De la Moselle luxembourgeoise M.O., 1995, 75 cl'")
            .that(result.wine)
            .isEqualTo("Pinot gris De la Moselle luxembourgeoise M.O., 1995, 75 cl")

        // 6. Source
        assertWithMessage("La source doit valoir 'Maxi GB'")
            .that(result.source)
            .isEqualTo("Maxi GB")

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "200 g 4 tranches de saumon fumé",
            "300 g de crème fraiche",
            "Le jus de 1/2 citron",
            "2 chicons",
            "2 c. à café de persil haché",
            "Sel et poivre",
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Hachez à la moulinette les 200 g de saumon fumé.",
            "Ajoutez-y ensuite la crème fraîche fouettée avec le jus de citron.",
            "Assaisonnez.",
            "Mélangez le tout et laissez reposer l heure au réfrigérateur.",
            "Prenez les feuilles des chicons et farcissez-les des rillettes de saumon.",
            "Assemblez le tout de manière à obtenir de nouveau la forme des chicons et entourez-les de tranches de saumon fumé.",
            "Trempez ensuite l'extrémité la plus large des chicons dans le persil.",
            "Servez comme entrée.",
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
