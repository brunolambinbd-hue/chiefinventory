package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 4 (Fusion [Ocr4Merger])
 * pour la recette des Salades d'Aubergines.
 */
class RecipeOcrAuberginePhase4Test {

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

    /**
     * Valide la Phase 4 : Fusion des consignes tronquées et reconstruction des césures par tiret ([Ocr4Merger]).
     */
    @Test
    fun test4_Phase4_Merging() {
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

        val expectedMergedIngredients = listOf(
            "6 aubergines moyennes (1 kg)",
            "1 ou 2 citrons",
            "150 g de beurre",
            "1 piment rouge",
            "persil haché",
            "2 gousses d'ail",
            "sel",
            "poivre"
        )
        assertLinesMatch(mergedIngredients, expectedMergedIngredients, OcrTestStrings.STEP4_MERGE_MISMATCH)

        val mergedInstructions = Ocr4Merger.mergeInstructions(categorizationResult.rawInstructionsList)
        val expectedMergedInstructions = listOf(
            "1 Plongez les aubergines dans l'eau bouillante et retirez la peau.",
            "Hachez la chair et faites-la suer dans un peu de beurre avec l'ail haché et le piment émincé fin.",
            "Assaisonnez.",
            "Ajoutez le jus de citron et le persil haché.",
            "Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.",
            "Pour la décoration: tomates"
        )
        assertLinesMatch(mergedInstructions, expectedMergedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
