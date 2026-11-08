package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'intégration anti-régression dédiée à la recette des Asperges vertes au parmesan.
 */
class RecipeOcrParserAspergesTest {

    /** Capture brute réelle ML Kit du scan caméra (écrite une seule fois). */
    private val rawOcrScanText = listOf(
        "cet te",
        "INGRÉDIENTS",
        "POUR",
        "4 PERSONNES",
        "1 kg d'asperges vertes",
        "4 tranches",
        "de jambon séché",
        "4 C. à soupe",
        "de parmesan",
        "(moulu ou en morceaux)",
        "6c. à soupe",
        "d' huile dolive",
        "le jus de 112 citron",
        "GB,",
        "20",
        "• Etuvez les asperges pendant 8 à",
        "10 minutes en veillant à les laisser",
        "croquantes (il n'est pas nécessaire",
        "d'éplucher les asperges vertes).",
        "Assiette de présentation disponible dans les Maxi GB",
        "sperges vertes au parmesan",
        "• Répartissez-les sur 4 assiettes,",
        "déposez sur chacune d'elles une",
        "tranche de jambon séché.",
        "Saupoudrez de parmesan.",
        "• Nappez d'huile d'olive et",
        "arrosez de jus de citron.",
        "VIN CONSEILLĖ",
        "Muscat d'Alsace, caves de",
        "Pfaffenheim, MO, 1996, 75 cl"
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
    fun testAspergesRecipeExactOcrRawScanParse() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        // 1. Validation du Titre
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .ignoringCase()
            .isEqualTo("Asperges vertes au parmesan")

        // 2. Validation des Portions / Nombre de Personnes
        assertWithMessage(OcrTestStrings.SERVINGS_MISMATCH)
            .that(result.servings)
            .isEqualTo("4")

        // 3. Validation de la Source / Établissement / Enseigne
        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .isEqualTo("VIN CONSEILLĖ")

        // 4. Validation du Vin Conseillé
        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Muscat d'Alsace")

        assertWithMessage(OcrTestStrings.WINE_PRODUCER_MISMATCH)
            .that(result.wine)
            .contains("caves de Pfaffenheim")

        assertWithMessage(OcrTestStrings.WINE_VINTAGE_MISMATCH)
            .that(result.wine)
            .contains("1996")

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
            "1 kg d'asperges vertes",
            "4 tranches de jambon séché",
            "4 c. à soupe de parmesan (moulu ou en morceaux)",
            "6 c. à soupe d'huile d'olive",
            "Le jus de 1/2 citron"
        )
        assertLinesMatch(ingredientLines, expectedIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        // 7. Validation des Instructions
        val instructionLines = result.instructions?.split("\n") ?: emptyList()
        val expectedInstructions = listOf(
            "Etuvez les asperges pendant 8 à 10 minutes en veillant à les laisser croquantes (il n'est pas nécessaire d'éplucher les asperges vertes).",
            "Répartissez-les sur 4 assiettes.",
            "Déposez sur chacune d'elles une tranche de jambon séché.",
            "Saupoudrez de parmesan.",
            "Nappez d'huile d'olive et arrosez de jus de citron."
        )
        assertLinesMatch(instructionLines, expectedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
