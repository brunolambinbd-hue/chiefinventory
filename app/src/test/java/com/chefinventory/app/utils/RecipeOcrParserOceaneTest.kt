package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'intégration anti-régression dédiée à la recette des Salades Océanes.
 */
class RecipeOcrParserOceaneTest {

    /** Capture brute réelle ML Kit du scan caméra (écrite une seule fois). */
    private val rawOcrScanText = listOf(
        "INGRÉDIENTS",
        "POUR",
        "4 PERSONNES",
        "250 g de crevettes",
        "grises décortiquées",
        "4 petites tomates",
        "(ou 2 grosses)",
        "l beau chicon",
        "I2 salade frisée",
        "l orange",
        "250 g de yaourt nature",
        "2 c. à soupe de",
        "mayonnaise",
        "2 c. à soupe de ketchup",
        "lc.à soupe de",
        "ciboulette hachée",
        "Tabasco",
        "sel et poivre",
        "Salade océane",
        "• Prélevez finement le zeste de",
        "l'orange, taillez-le en fins bâton-",
        "nets que vous plongez 1 minute",
        "dans un fond d'eau bouillante.",
        "Rafraîchissez à l'eau froide et lais-",
        "sez bien égoutter.",
        "• Mélangez le yaourt, le ketchup,",
        "la mayonnaise et la ciboulette.",
        "Ajoutez du Tabasco selon vos",
        "goûts, salez et poivrez. Réservez.",
        "• Lavez et essorez la salade, ajou-",
        "tez-y le chicon nettoyé et émincé.",
        "Disposez sur de grandes assiettes",
        "et saupoudrez de zeste d'orange.",
        "•Coupez les tomates en huit,",
        "répartissez-les sur les assiettes et",
        "garnissez le centre de crevettes",
        "grises.",
        "• Servez frais (ne laissez pas trop",
        "longtemps en attente); présentez la",
        "sauce à part.",
        "VIN CONSEILLĖ",
        "Touraine,",
        "Sauvignon blanc, 1995, 75 cl",
        "TOCRAINE",
        "SAETICsox"
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
    fun testOceaneRecipeExactOcrRawScanParse() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        // 1. Validation du Titre
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .ignoringCase()
            .isEqualTo("Salade océane")

        // 2. Validation des Portions / Nombre de Personnes
        assertWithMessage(OcrTestStrings.SERVINGS_MISMATCH)
            .that(result.servings)
            .isEqualTo("4")

        // 3. Validation du Vin Conseillé
        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Touraine")

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Sauvignon blanc")

        assertWithMessage(OcrTestStrings.WINE_VINTAGE_MISMATCH)
            .that(result.wine)
            .contains("1995")

        assertWithMessage(OcrTestStrings.WINE_VOLUME_MISMATCH)
            .that(result.wine)
            .contains("75 cl")

        // 4. Validation des Champs Sans Données (Modèle Neutre)
        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .isEqualTo("VIN CONSEILLĖ")

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
            "250 g de crevettes grises décortiquées",
            "4 petites tomates (ou 2 grosses)",
            "1 beau chicon",
            "1/2 salade frisée",
            "1 orange",
            "250 g de yaourt nature",
            "2 c. à soupe de mayonnaise",
            "2 c. à soupe de ketchup",
            "1 c. à soupe de ciboulette hachée",
            "Tabasco",
            "Sel et poivre"
        )
        assertLinesMatch(ingredientLines, expectedIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        // 6. Validation des Instructions
        val instructionLines = result.instructions?.split("\n") ?: emptyList()
        val expectedInstructions = listOf(
            "Prélevez finement le zeste de l'orange, taillez-le en fins bâtonnets que vous plongez 1 minute dans un fond d'eau bouillante.",
            "Rafraîchissez à l'eau froide et laissez bien égoutter.",
            "Mélangez le yaourt, le ketchup la mayonnaise et la ciboulette.",
            "Ajoutez du Tabasco selon vos goûts, salez et poivrez.",
            "Réservez.",
            "Lavez et essorez la salade, ajoutez-y le chicon nettoyé et émincé.",
            "Disposez sur de grandes assiettes et saupoudrez de zeste d'orange.",
            "Coupez les tomates en huit.",
            "Répartissez-les sur les assiettes et garnissez le centre de crevettes grises.",
            "Servez frais (ne laissez pas trop longtemps en attente); présentez la sauce à part."
        )
        assertLinesMatch(instructionLines, expectedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
