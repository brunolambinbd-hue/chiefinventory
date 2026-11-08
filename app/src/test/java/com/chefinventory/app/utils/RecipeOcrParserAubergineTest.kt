package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'intégration anti-régression dédiée à la recette des Salades d'Aubergines.
 */
class RecipeOcrParserAubergineTest {

    /** Capture brute réelle ML Kit du scan caméra (écrite une seule fois). */
    private val rawOcrScanText = listOf(
        "CONRAD|",
        "BR",
        "Nahit YILMAZ",
        "Hôtel Conrad International",
        "avenue Louise 7I",
        "1050 BRUXELLES",
        "Tél. : 02/542.42.42",
        "saveur de",
        "alade d'aubergines",
        "1 Plongez les aubergines dans l'eau bouillante et retirez la peau. Hachez la chair et faites-la suer dans",
        "un peu de beurre avec l'ail haché et le piment émincé fin. Assaisonnez. Ajoutez le jus de citron et",
        "le persil haché. Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.",
        "23",
        "20",
        "Ingrédients pour 4 personnes",
        "6 aubergines moyennes (t 1 kg)",
        "1 ou 2 citrons",
        "150 g de beurre",
        "I piment rouge",
        "persil haché",
        "2 gousses d'ail",
        "sel, poivre",
        "Pour la décoration: tomates",
        "Notre vin conseillé",
        "Rousse Région Merlot",
        "(vìn rouge de Bulgarie)",
        "M.O."
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
    fun testAubergineRecipeExactOcrRawScanParse() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        // 1. Validation du Titre
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .ignoringCase()
            .isEqualTo("Salade d'aubergines")

        // 2. Validation des Portions / Nombre de Personnes
        assertWithMessage(OcrTestStrings.SERVINGS_MISMATCH)
            .that(result.servings)
            .isEqualTo("4")

        // 3. Validation de la Source / Établissement / Chef
        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .contains("Nahit YILMAZ")

        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .contains("Hôtel Conrad International")

        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .contains("02/542.42.42")

        // 4. Validation du Vin Conseillé
        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Rousse Région Merlot")

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("vin rouge de Bulgarie")

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
            "6 aubergines moyennes (1 kg)",
            "1 ou 2 citrons",
            "150 g de beurre",
            "1 piment rouge",
            "Persil haché",
            "2 gousses d'ail",
            "Sel",
            "Poivre"
        )
        assertLinesMatch(ingredientLines, expectedIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        // 7. Validation des Instructions
        val instructionLines = result.instructions?.split("\n") ?: emptyList()
        val expectedInstructions = listOf(
            "Plongez les aubergines dans l'eau bouillante et retirez la peau.",
            "Hachez la chair et faites-la suer dans un peu de beurre avec l'ail haché et le piment émincé fin.",
            "Assaisonnez.",
            "Ajoutez le jus de citron et le persil haché.",
            "Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.",
            "Pour la décoration: tomates."
        )
        assertLinesMatch(instructionLines, expectedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
