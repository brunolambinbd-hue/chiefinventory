package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'intégration anti-régression dédiée à la recette des Salades d'Avocat et Princesses.
 */
class RecipeOcrParserAvocatTest {

    /** Capture brute réelle ML Kit du scan caméra (écrite une seule fois). */
    private val rawOcrScanText = listOf(
        "INGRÉDIENTS",
        "POUR",
        "4 PERSONNES",
        "I avocat",
        "200 g de haricots",
        "princesses",
        "200 g de saumon cru",
        "aneth",
        "6c.à soupe",
        "d'huile d'olive",
        "le jus de l/2 citron",
        "lc.à café de miel",
        "lc.à soupe de vinaigre",
        "de vin blanc",
        "sel et poivre",
        "alade d'avocat et princesses",
        "•Faites cuire les haricots",
        "princesses 10 à 15 minutes jusqu'à",
        "ce que vous puissiez y piquer",
        "facilement une fourchette.",
        "Retirez le jus de cuisson et passez",
        "les haricots sous un jet d'eau",
        "froide.",
        "• Coupez les haricots en deux.",
        "•Coupez l'avocat en petits dés et",
        "hachez l'aneth.",
        "Coupez le saumon en dés de",
        "5 mm sur 5 mm.",
        "• Préparez une vinaigrette avec",
        "l'huile d'olive, le jus de citron, le",
        "miel, le vinaigre, du sel et du",
        "poivre.",
        "• Mélangez tous ces ingrédients",
        "dans un saladier jusqu'à ce que",
        "vous obteniez une belle salade",
        "mêlée.",
        "•Répartissez la salade sur",
        "4 assiettes et décorez avec des",
        "brins d'aneth.",
        "VIN CONSEILLÉ",
        "Alban de Saint-Pré",
        "Saumur blanc, sec,",
        "M.O., 1995, 75 cl"
    ).joinToString("\n")

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    private fun assertLinesMatch(actual: List<String>, expected: List<String>, description: String) {
        if (actual != expected) {
            val message = "$description :\n\n" +
                "${OcrTestStrings.HEADER_EXPECTED} (${expected.size} lines) :\n" +
                expected.mapIndexed { i, s -> "  [$i] $s" }.joinToString("\n") + "\n\n" +
                "${OcrTestStrings.HEADER_ACTUAL} (${actual.size} lines) :\n" +
                actual.mapIndexed { i, s -> "  [$i] $s" }.joinToString("\n")
            fail(message)
        }
    }

    @Test
    fun testAvocatRecipeExactOcrRawScanParse() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        // 1. Validation du Titre
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .ignoringCase()
            .isEqualTo("Salade d'avocat et princesses")

        // 2. Validation des Portions / Nombre de Personnes
        assertWithMessage(OcrTestStrings.SERVINGS_MISMATCH)
            .that(result.servings)
            .isEqualTo("4")

        // 3. Validation du Vin Conseillé
        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Alban de Saint-Pré")

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Saumur blanc")

        assertWithMessage(OcrTestStrings.WINE_VINTAGE_MISMATCH)
            .that(result.wine)
            .contains("1995")

        assertWithMessage(OcrTestStrings.WINE_VOLUME_MISMATCH)
            .that(result.wine)
            .contains("75 cl")

        // 4. Validation des Champs Sans Données (Modèle Neutre)
        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .isNull()

        assertWithMessage(OcrTestStrings.PREP_TIME_MISMATCH)
            .that(result.prepTime)
            .isNull()

        assertWithMessage(OcrTestStrings.COOK_TIME_MISMATCH)
            .that(result.cookTime)
            .isNull()

        assertWithMessage(OcrTestStrings.RESTING_TIME_MISMATCH)
            .that(result.restingTime)
            .isNull()

        assertWithMessage(OcrTestStrings.KCAL_MISMATCH)
            .that(result.kcalPerServing)
            .isNull()

        assertWithMessage(OcrTestStrings.DIFFICULTY_MISMATCH)
            .that(result.difficulty)
            .isNull()

        // 5. Validation des Ingrédients
        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()
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
        assertLinesMatch(ingredientLines, expectedIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        // 6. Validation des Instructions
        val instructionLines = result.instructions?.split("\n") ?: emptyList()
        val expectedInstructions = listOf(
            "Faites cuire les haricots princesses 10 à 15 minutes jusqu'à ce que vous puissiez y piquer facilement une fourchette.",
            "Retirez le jus de cuisson et passez les haricots sous un jet d'eau froide.",
            "Coupez les haricots en deux.",
            "Coupez l'avocat en petits dés et hachez l'aneth.",
            "Coupez le saumon en dés de 5 mm sur 5 mm.",
            "Préparez une vinaigrette avec l'huile d'olive, le jus de citron, le miel, le vinaigre, du sel et du poivre.",
            "Mélangez tous ces ingrédients dans un saladier jusqu'à ce que vous obteniez une belle salade mêlée.",
            "Répartissez la salade sur 4 assiettes et décorez avec des brins d'aneth."
        )
        assertLinesMatch(instructionLines, expectedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
