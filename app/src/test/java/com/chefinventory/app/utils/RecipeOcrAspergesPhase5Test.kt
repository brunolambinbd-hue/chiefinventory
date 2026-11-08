package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 5 (Embellissement [Ocr5Beautifier])
 * pour la recette des Asperges vertes au parmesan.
 */
class RecipeOcrAspergesPhase5Test {

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

    /**
     * Valide la Phase 5 : Embellissement final, scission d'ingrédients et élimination des scories visuelles ([Ocr5Beautifier]).
     */
    @Test
    fun test5_Phase5_Beautification() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val rawLines = rawOcrScanText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val normalizedLines = rawLines.map { Ocr1Corrector.correct(it, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs) }.filter { it.isNotBlank() }
        val cleaningResult = Ocr2Cleaner.cleanWithVocabulary(normalizedLines, vocabulary)

        val titleResult = Ocr3CategorizerTitleExtractor.extractTitle(cleaningResult.cleanedLines, vocabulary)
        val categorizationResult = Ocr3Categorizer.categorize(titleResult.cleanedLines, vocabulary)

        val mergedIngredients = Ocr4Merger.mergeIngredients(
            categorizationResult.rawIngredientsList,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )
        val mergedInstructions = Ocr4Merger.mergeInstructions(categorizationResult.rawInstructionsList)

        val beautifiedIngredients = Ocr5Beautifier.beautifyIngredients(
            mergedIngredients,
            baseIngredients = vocabulary.baseIngredientsExpectingVariety,
            varietyQualifiers = vocabulary.ingredientVarietyQualifiers
        )

        val finalIngredientList = beautifiedIngredients.flatMap { line ->
            Ocr4MergerIngredientSplitter.splitCombinedIngredients(
                line,
                vocabulary.commonIngredientsNoQty,
                vocabulary.mandatoryComplementIngredients,
                vocabulary.ingredientPreparationModifiers,
                vocabulary.ingredientConnectors,
                vocabulary.culinaryMeasurementUnits
            )
        }

        val expectedFinalIngredients = listOf(
            "1 kg d'asperges vertes",
            "4 tranches de jambon séché",
            "4 c. à soupe de parmesan (moulu ou en morceaux)",
            "6 c. à soupe d'huile d'olive",
            "Le",
            "jus de 1/2 citron"
        )
        assertLinesMatch(finalIngredientList, expectedFinalIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        val beautifiedInstructions = Ocr5Beautifier.beautifyInstructions(
            mergedInstructions,
            stepActionKeywords = vocabulary.stepActionKeywords,
            hangingConnectors = vocabulary.hangingConnectors,
            culinaryUnits = vocabulary.culinaryMeasurementUnits,
            spellingRepairs = vocabulary.ocrSpellingRepairs
        )

        val expectedBeautifiedInstructions = listOf(
            "Etuvez les asperges pendant 8 à 10 minutes en veillant à les laisser croquantes (il n'est pas nécessaire d'éplucher les asperges vertes).",
            "Répartissez-les sur 4 assiettes.",
            "Déposez sur chacune d'elles une tranche de jambon séché.",
            "Saupoudrez de parmesan.",
            "Nappez d'huile d'olive et arrosez de jus de citron."
        )
        assertLinesMatch(beautifiedInstructions, expectedBeautifiedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
