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
 * Test E2E de référence 11 sur 25 : 'Crabe mangue'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe11CrabeMangueTest {

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
    fun testRecipe11_CrabeMangue_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("Saveur d'", top = 165, left = 750, bottom = 259, right = 945),
            OcrTextLine("Drabe mangue", top = 504, left = 214, bottom = 640, right = 846),
            OcrTextLine("Epluchez et dénoyautez les mangues, taillez-en la moitié en lamelles, le reste en", top = 794, left = 46, bottom = 831, right = 985),
            OcrTextLine("cubes.", top = 836, left = 44, bottom = 860, right = 114),
            OcrTextLine("Dans un bol, mélangez le jaune d'œuf et la moutarde, ajoutez 1'huile au fouet", top = 871, left = 45, bottom = 909, right = 985),
            OcrTextLine("pour obtenir une mayonnaise ferme, salez et poivrez, incorporez ensuite le yaourt,", top = 905, left = 45, bottom = 942, right = 986),
            OcrTextLine("le rhum, le curcuma, les dés de mangue et du poivre de Cayenne selon le goût.", top = 938, left = 45, bottom = 976, right = 984),
            OcrTextLine("Mélangez le crabe égoutté et une partie de la mayonnaise.", top = 986, left = 45, bottom = 1019, right = 727),
            OcrTextLine("Placez une lamelle de mangue et un peu de crabe sur les petites feuilles de chicon,", top = 1021, left = 44, bottom = 1063, right = 984),
            OcrTextLine("rz de mayonnaise restante et décorez de feuilles d'aneth.", top = 1057, left = 104, bottom = 1096, right = 768),
            OcrTextLine("Ingrédients pour 4 personnes", top = 798, left = 1399, bottom = 827, right = 1785),
            OcrTextLine("100 g de chair de crabe", top = 851, left = 1400, bottom = 881, right = 1671),
            OcrTextLine("2 mangues", top = 893, left = 1399, bottom = 919, right = 1527),
            OcrTextLine("l c. à café de moutarde", top = 925, left = 1418, bottom = 954, right = 1671),
            OcrTextLine("2 c. à soupe de yaourt grec", top = 965, left = 1401, bottom = 990, right = 1715),
            OcrTextLine("1 jaune d'euf", top = 998, left = 1402, bottom = 1028, right = 1568),
            OcrTextLine("5 cl d'huile", top = 1035, left = 1402, bottom = 1060, right = 1534),
            OcrTextLine("I pincée de curcuma", top = 1070, left = 1420, bottom = 1102, right = 1643),
            OcrTextLine("lc. à dessert de rhum", top = 1107, left = 1404, bottom = 1135, right = 1661),
            OcrTextLine("poivre de Cayenne, sel, poivre", top = 1141, left = 1402, bottom = 1175, right = 1755),
            OcrTextLine("des petites feuilles de chicon", top = 1176, left = 1405, bottom = 1210, right = 1737),
            OcrTextLine("1 branche d'aneth", top = 1214, left = 1420, bottom = 1243, right = 1619)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 11 doit valoir 'Crabe mangue'")
            .that(result.title)
            .isEqualTo("Crabe mangue")

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
        assertWithMessage("La recommandation de vin doit valoir null")
            .that(result.wine)
            .isNull()

        // 6. Source
        assertWithMessage("La source doit valoir null")
            .that(result.source)
            .isNull()

        // 7. Liste Complète des Ingrédients
        val expectedIngredients = listOf(
            "100 g de chair de crabe",
            "2 mangues",
            "1 c. à café de moutarde",
            "2 c. à soupe de yaourt grec",
            "1 jaune d'oeuf",
            "5 cl d'huile",
            "1 pincée de curcuma",
            "1 c. à dessert de rhum",
            "Poivre de Cayenne, sel, poivre",
            "Des petites feuilles de chicon",
            "1 branche d'aneth"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Epluchez et dénoyautez les mangues, taillez-en la moitié en lamelles, le reste en cubes.",
            "Dans un bol, mélangez le jaune d'œuf et la moutarde, ajoutez l'huile au fouet pour obtenir une mayonnaise ferme, salez et poivrez, incorporez ensuite le yaourt le rhum, le curcuma, les dés de mangue et du poivre de Cayenne selon le goût.",
            "Mélangez le crabe égoutté et une partie de la mayonnaise.",
            "Placez une lamelle de mangue et un peu de crabe sur les petites feuilles de chicon rz de mayonnaise restante et décorez de feuilles d'aneth."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
