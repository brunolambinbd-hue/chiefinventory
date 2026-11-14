package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau B (Pipeline OCR Complet) vérifiant le traitement propre
 * de la recette 'Cocktail de crevettes aux concombres' et l'élimination des fuites d'instructions.
 */
class RecipeOcrParserCocktailCrevettesTest {

    private val rawOcrScanText = listOf(
        "INGRÉDIENTS",
        "POUR",
        "4 PERSONNES",
        "300 g de crevettes roses",
        "8 c. à soupe de yaourt",
        "demi-écrémé sucré",
        "2 tomates Roma",
        "1/2 concombre",
        "80 g de feta",
        "1 c. à soupe",
        "d'estragon haché",
        "1 c. à soupe de ketchup",
        "2 c. à soupe",
        "de mayonnaise",
        "1 c. à soupe",
        "de vinaigre de vin blanc",
        "quelques branches",
        "de cerfeuil",
        "sel et poivre",
        "Cocktail de crevettes aux concombres",
        "• Coupez le concombre dans la longueur et épépinez-le.",
        "Détaillez-le ensuite en petits dés de 0,5 x 0,5 cm. Épluchez et épépinez les tomates et découpez-les en menus morceaux de 0,5 x 0,5 cm.",
        "• Coupez la feta en morceaux.",
        "• Mélangez les légumes, la feta et l'estragon haché aux 4 c. à soupe de yaourt. Salez et poivrez.",
        "• Mélangez le reste du yaourt à la mayonnaise, au ketchup et au vinaigre. Assaisonnez.",
        "• Disposez la préparation de concombres sur les assiettes et garnissez de quelques crevettes roses. Nappez de la sauce au yaourt et à la mayonnaise et garnissez de quelques branches de cerfeuil."
    ).joinToString("\n")

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCocktailCrevettesRecipe_ParsesIngredientsAndInstructionsCleanly() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()
        val instructionLines = result.instructions?.split("\n") ?: emptyList()

        // 1. Ingrédients
        assertWithMessage("La recette doit contenir '1 c. à soupe d'estragon haché'")
            .that(ingredientLines)
            .contains("1 c. à soupe d'estragon haché")

        assertWithMessage("La recette doit contenir 'Quelques branches de cerfeuil'")
            .that(ingredientLines)
            .contains("Quelques branches de cerfeuil")

        assertWithMessage("La recette doit contenir 'Sel et poivre'")
            .that(ingredientLines)
            .contains("Sel et poivre")

        assertWithMessage("La liste d'ingrédients ne doit PAS contenir de phrase hybride parasite")
            .that(ingredientLines.joinToString("\n"))
            .doesNotContain("haché aux 4 c. à")

        // 2. Instructions
        assertWithMessage("Les instructions doivent contenir la consigne du cerfeuil")
            .that(instructionLines.joinToString("\n"))
            .contains("garnissez de quelques branches de cerfeuil")
    }
}
