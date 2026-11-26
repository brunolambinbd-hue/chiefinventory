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
 * Test E2E de référence 08 sur 25 : 'Gre canapés grecs'.
 * Validation déterministe et exhaustive de tous les champs de sortie (Titre, Temps, Servings, Ingrédients, Instructions, Vin, Source).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialRecipe08CanapesGrecsTest {

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
    fun testRecipe08_CanapesGrecs_ExtractsAllFieldsExhaustively() {
        val lines2D = listOf(
            OcrTextLine("Saveur de", top = 130, left = 827, bottom = 238, right = 1039),
            OcrTextLine("GRE", top = 175, left = 810, bottom = 419, right = 1105),
            OcrTextLine("Danapés grecs", top = 507, left = 309, bottom = 623, right = 927),
            OcrTextLine("Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-", top = 786, left = 85, bottom = 831, right = 1234),
            OcrTextLine("cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur + 12 heures.", top = 821, left = 84, bottom = 866, right = 1233),
            OcrTextLine("Découpez la baguette en tranches d'environ 1 cm, faites-les légèrement griller puis frottez-les", top = 863, left = 85, bottom = 911, right = 1236),
            OcrTextLine("d'ail, égouttez le fromage et placez-le sur le pain.", top = 907, left = 85, bottom = 944, right = 674),
            OcrTextLine("Décorezd'olives coupées en deux et de romarin.", top = 951, left = 86, bottom = 986, right = 667),
            OcrTextLine("Ingrédients pour 4 personnes", top = 788, left = 1491, bottom = 820, right = 1884),
            OcrTextLine("180 g de feta", top = 848, left = 1492, bottom = 877, right = 1643),
            OcrTextLine("1/2 verre d'huile d'olive", top = 880, left = 1493, bottom = 908, right = 1774),
            OcrTextLine("10 olives vertes dénoyautées", top = 920, left = 1493, bottom = 947, right = 1826),
            OcrTextLine("10 olives noires dénoyautées", top = 954, left = 1494, bottom = 984, right = 1830),
            OcrTextLine("I petit pimnent", top = 995, left = 1494, bottom = 1023, right = 1656),
            OcrTextLine("I gousse d'ail", top = 1027, left = 1507, bottom = 1060, right = 1656),
            OcrTextLine("I baguette de pain gris", top = 1065, left = 1508, bottom = 1098, right = 1764),
            OcrTextLine("du thym et du romarin frais", top = 1101, left = 1494, bottom = 1132, right = 1826),
            OcrTextLine("sel et poivre du moulin", top = 1137, left = 1495, bottom = 1171, right = 1767)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        // 1. Titre Exact
        assertWithMessage("Le titre de la recette 08 doit valoir 'Gre canapés grecs'")
            .that(result.title)
            .isEqualTo("Gre canapés grecs")

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
            "180 g de feta",
            "1/2 verre d'huile d'olive",
            "10 olives vertes dénoyautées",
            "10 olives noires dénoyautées",
            "1 petit pimnent",
            "1 gousse d'ail",
            "1 baguette de pain gris",
            "Du thym et du romarin frais",
            "Sel et poivre du moulin"
        )
        val actualIngredients = result.ingredients?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste des ingrédients doit correspondre exactement aux éléments attendus")
            .that(actualIngredients)
            .containsExactlyElementsIn(expectedIngredients)
            .inOrder()

        // 8. Liste Complète des Instructions
        val expectedInstructions = listOf(
            "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pincée de sel et placez-y le fromage découpé en tranches laissez mariner au réfrigérateur 1/2 heures.",
            "Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les d'ail, égouttez le fromage et placez-le sur le pain.",
            "Décorez d'olives coupées en deux et de romarin."
        )
        val actualInstructions = result.instructions?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        assertWithMessage("La liste des instructions doit correspondre exactement aux phrases attendues")
            .that(actualInstructions)
            .containsExactlyElementsIn(expectedInstructions)
            .inOrder()
    }
}
