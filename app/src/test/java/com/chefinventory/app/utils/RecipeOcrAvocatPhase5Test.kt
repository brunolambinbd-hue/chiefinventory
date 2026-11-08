package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 5 (Embellissement [Ocr5Beautifier])
 * pour la recette des Salades d'Avocat et Princesses.
 */
class RecipeOcrAvocatPhase5Test {

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
            "1 avocat",
            "200 g de haricots princesses",
            "200 g de saumon cru",
            "Aneth",
            "6 c. à soupe d'huile d'olive",
            "Le",
            "jus de 1/2 citron",
            "1 c. à café de miel",
            "1 c. à soupe de vinaigre de vin blanc",
            "Sel et poivre"
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
            "Faites cuire les haricots princesses 10 à 15 minutes jusqu'à ce que vous puissiez y piquer facilement une fourchette.",
            "Retirez le jus de cuisson et passez les haricots sous un jet d'eau froide.",
            "Coupez les haricots en deux.",
            "Coupez l'avocat en petits dés et hachez l'aneth.",
            "Coupez le saumon en dés de 5 mm sur 5 mm.",
            "Préparez une vinaigrette avec l'huile d'olive, le jus de citron, le miel, le vinaigre, du sel et du poivre.",
            "Mélangez tous ces ingrédients dans un saladier jusqu'à ce que vous obteniez une belle salade mêlée.",
            "Répartissez la salade sur 4 assiettes et décorez avec des brins d'aneth."
        )
        assertLinesMatch(beautifiedInstructions, expectedBeautifiedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
