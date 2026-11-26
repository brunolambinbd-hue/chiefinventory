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
 * Test E2E de référence 09 sur 25 : 'Céleri au stilton'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe09CeleriStiltonTest {

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
    fun testRecipe09_CeleriStilton_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("Déleri", top = 469, left = 198, bottom = 587, right = 455),
            OcrTextLine("au stilton", top = 593, left = 291, bottom = 714, right = 666),
            OcrTextLine("saveur du", top = 131, left = 730, bottom = 234, right = 921),
            OcrTextLine("ROYAUIME", top = 176, left = 747, bottom = 315, right = 958),
            OcrTextLine("UNI", top = 242, left = 729, bottom = 409, right = 996),
            OcrTextLine("Réduisez le stilton en grumeaux, mélangez au fromage blanc et au porto.", top = 805, left = 41, bottom = 864, right = 888),
            OcrTextLine("Lavez et découpez les branches de céleri en tronçons de 3 cm. Mettez une petite", top = 844, left = 41, bottom = 907, right = 974),
            OcrTextLine("cuillère de fromage dans chaque tronçon. Décorez d'une feuille de cresson de", top = 876, left = 43, bottom = 942, right = 977),
            OcrTextLine("fontaine.", top = 944, left = 44, bottom = 969, right = 139),
            OcrTextLine("17", top = 1347, left = 977, bottom = 1391, right = 1022),
            OcrTextLine("< Ingrédients pour 4 personnes", top = 791, left = 1265, bottom = 836, right = 1782),
            OcrTextLine("6 branches de céleri blanc", top = 850, left = 1394, bottom = 885, right = 1703),
            OcrTextLine("140 g de fromage stilton", top = 889, left = 1396, bottom = 928, right = 1681),
            OcrTextLine("50 g de fromage blanc", top = 925, left = 1397, bottom = 964, right = 1662),
            OcrTextLine("lc. à café de porto blanc", top = 959, left = 1400, bottom = 1000, right = 1697),
            OcrTextLine("pour la décoration: cresson de fontaine", top = 988, left = 1399, bottom = 1038, right = 1881)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 09 doit valoir 'Céleri au stilton'")
            .that(result.title)
            .isEqualTo("Céleri au stilton")

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
            "< Ingrédients",
            "6 branches de céleri blanc",
            "140 g de fromage stilton",
            "50 g de fromage blanc",
            "1 c. à café de porto blanc"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des ingrédients doit correspondre exactement")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Réduisez le stilton en grumeaux, mélangez au fromage blanc et au porto.",
            "Lavez et découpez les branches de céleri en tronçons de 3 cm.",
            "Mettez une petite cuillère de fromage dans chaque tronçon.",
            "Décorez d'une feuille de cresson de fontaine.",
            "Pour la décoration: cresson de fontaine."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste complète des instructions doit correspondre exactement")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
