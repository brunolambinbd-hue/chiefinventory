package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'intégration anti-régression dédiée à la recette des Bûchettes de melon au jambon italien et au porto.
 */
class RecipeOcrParserMelonTest {

    /** Capture brute réelle ML Kit du scan caméra (écrite une seule fois). */
    private val rawOcrScanText = listOf(
        "INGRÉDIENTS",
        "POUR",
        "4 PERSONNES",
        "I melon Galia",
        "8 tranches de jambon",
        "italien",
        "4 c. à soupe de porto",
        "lc.à soupe de vinaigre de",
        "framboise",
        "4c.à soupe de yoghourt",
        "4c. à soupe d'huile",
        "d'olive",
        "l c.à café de confiture de",
        "framboises",
        "4 branches d' aneth",
        "sel et poivre",
        "30",
        "• Pelez le melon, épépinez-le et",
        "coupez la chair en fines",
        "lamelles.",
        "•Coupez les tranches de",
        "jambon en deux dans le sens de",
        "la longueur et déposez-y les",
        "lamelles de melon. Roulez les",
        "tranches de façon à obtenir",
        "4 petites bûches par personne.",
        "Büchettes de melon",
        "au jambon italien et au porto",
        "• Mixez le porto, le vinaigre, le",
        "yoghourt, l'huile d'olive et la",
        "confiture de framboises pour",
        "en faire une vinaigrette.",
        "Assaisonnez généreusement.",
        "Assiette de présentation disponible dans les Maxi GB",
        "• Disposez les bûchettes sur",
        "une assiette avec la vinaigrette.",
        "Décorez de brins d'aneth.",
        "VIN CONSEILLĖ",
        "Atinum, Bordeaux Graves",
        "MO, 1995, 75 cl",
        "AtN",
        "ATINUM",
        "GRAVES"
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
    fun testMelonRecipeExactOcrRawScanParse() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        // 1. Validation du Titre
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .ignoringCase()
            .isEqualTo("Bûchettes de melon au jambon italien et au porto")

        // 2. Validation des Portions / Nombre de Personnes
        assertWithMessage(OcrTestStrings.SERVINGS_MISMATCH)
            .that(result.servings)
            .isEqualTo("4")

        // 3. Validation de la Source / Établissement
        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .isEqualTo("VIN CONSEILLĖ")

        // 4. Validation du Vin Conseillé
        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Atinium")

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Bordeaux Graves")

        assertWithMessage(OcrTestStrings.WINE_VINTAGE_MISMATCH)
            .that(result.wine)
            .contains("1995")

        assertWithMessage(OcrTestStrings.WINE_VOLUME_MISMATCH)
            .that(result.wine)
            .contains("75 cl")

        // 5. Validation des Champs Sans Données (Modèle Neutre)
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

        // 6. Validation des Ingrédients
        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()
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
        assertLinesMatch(ingredientLines, expectedIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        // 7. Validation des Instructions
        val instructionLines = result.instructions?.split("\n") ?: emptyList()
        val expectedInstructions = listOf(
            "Pelez le melon, épépinez-le et coupez la chair en fines lamelles.",
            "Coupez les tranches de jambon en deux dans le sens de la longueur et déposez-y les lamelles de melon.",
            "Roulez les tranches de façon à obtenir 4 petites bûches par personne.",
            "Mixez le porto, le vinaigre, le yoghourt, l'huile d'olive et la confiture de framboises pour en faire une vinaigrette.",
            "Assaisonnez généreusement.",
            "Disposez les bûchettes sur une assiette avec la vinaigrette.",
            "Décorez de brins d'aneth."
        )
        assertLinesMatch(instructionLines, expectedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
