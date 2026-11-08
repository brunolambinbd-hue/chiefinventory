package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'isolation TDD démontrant le comportement du pipeline OCR
 * sur des fiches de recettes dépourvues de titre.
 */
class OcrTitleScoreThresholdTest {

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
    fun testParse_RecipeWithoutTitle_DoesNotStealIngredientsOrInstructions() {
        val exactOcrRawTextWithoutTitle = listOf(
            "r",
            "e",
            "e t",
            "Ingrédients pour 4 personnes",
            "24 langoustines cuites",
            "I mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "lc. à café de curry, I/2 citron",
            "I yaourt nature (125 g)",
            "4 belles feuilles de salade",
            "cresson, sel et poivre",
            "Délayez le curry avec la mayonnaise, puis ajoutez le yaourt et",
            "un trait de jus de citron. Salez et poivrez, réservez.",
            "Décortiquez les langoustines. Réservez 4 belles têtes.",
            "Pelez la mangue, taillez-la en fines lamelles. Lavez et essorez la sala-",
            "de; lavez le cresson.",
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des",
            "lamelles de mangue. Décorez de salade et de cresson, ajoutez les",
            "croûtons.",
            "Disposez une tête de langoustine sur chaque assiette, nappez les lan-",
            "goustines d'un peu de sauce au curry.",
            "Servez frais, en présentant le restant de la sauce à part.",
            "Vous poOuvez préparer une soupe avec les têtes et les carcasses des lan-",
            "goustines.",
            "ExprrtEANS",
            "Vin conseillé",
            "Gewurztraminer,",
            "Caves de Pfaffenhein",
            "MO, 1992, 75 cl"
        ).joinToString("\n")

        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(exactOcrRawTextWithoutTitle, vocabulary)

        // 1. Le titre doit être strictement nul (aucune ligne d'ingrédient ne doit être volée)
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .isNull()

        // 2. Validation des Portions / Nombre de Personnes
        assertWithMessage(OcrTestStrings.SERVINGS_MISMATCH)
            .that(result.servings)
            .isEqualTo("4")

        // 3. Validation des 10 Ingrédients Intacts
        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()
        val expectedIngredients = listOf(
            "24 langoustines cuites",
            "1 mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "1 c. à café de curry",
            "1/2 citron",
            "1 yaourt nature (125 g)",
            "4 belles feuilles de salade",
            "Cresson",
            "Sel et poivre"
        )
        assertLinesMatch(ingredientLines, expectedIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        // 4. Validation des 11 Instructions Intactes
        val instructionLines = result.instructions?.split("\n") ?: emptyList()
        val expectedInstructions = listOf(
            "Délayez le curry avec la mayonnaise, puis ajoutez le yaourt et un trait de jus de citron.",
            "Salez et poivrez, réservez.",
            "Décortiquez les langoustines.",
            "Réservez 4 belles têtes.",
            "Pelez la mangue, taillez-la en fines lamelles.",
            "Lavez et essorez la salade; lavez le cresson.",
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des lamelles de mangue.",
            "Décorez de salade et de cresson, ajoutez les croûtons.",
            "Disposez une tête de langoustine sur chaque assiette, nappez les langoustines d'un peu de sauce au curry.",
            "Servez frais, en présentant le restant de la sauce à part.",
            "Vous pouvez préparer une soupe avec les têtes et les carcasses des langoustines."
        )
        assertLinesMatch(instructionLines, expectedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
