package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 2 (Dépollution [Ocr2Cleaner])
 * pour la recette des Bûchettes de melon au jambon italien.
 */
class RecipeOcrMelonPhase2Test {

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

    /**
     * Valide la Phase 2 : Dépollution du bruit de numérisation OCR, pages et bannières ([Ocr2Cleaner]).
     */
    @Test
    fun test2_Phase2_Cleaning() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val rawLines = rawOcrScanText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val normalizedLines = rawLines.map { Ocr1Corrector.correct(it, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs) }.filter { it.isNotBlank() }

        val cleaningResult = Ocr2Cleaner.cleanWithVocabulary(normalizedLines, vocabulary)
        val cleanedLines = cleaningResult.cleanedLines

        val expectedCleaned = listOf(
            "INGRÉDIENTS",
            "POUR",
            "4 PERSONNES",
            "1 melon Galia",
            "8 tranches de jambon",
            "italien",
            "4 c. à soupe de porto",
            "1 c. à soupe de vinaigre de",
            "framboise",
            "4 c. à soupe de yoghourt",
            "4 c. à soupe d'huile",
            "d'olive",
            "1 c. à café de confiture de",
            "framboises",
            "4 branches d'aneth",
            "sel et poivre",
            "• Pelez le melon, épépinez-le et",
            "coupez la chair en fines",
            "lamelles.",
            "•Coupez les tranches de",
            "jambon en deux dans le sens de",
            "la longueur et déposez-y les",
            "lamelles de melon. Roulez les",
            "tranches de façon à obtenir",
            "4 petites bûches par personne.",
            "Bûchettes de melon",
            "au jambon italien et au porto",
            "• Mixez le porto, le vinaigre, le",
            "yoghourt, l'huile d'olive et la",
            "confiture de framboises pour",
            "en faire une vinaigrette.",
            "Assaisonnez généreusement.",
            "• Disposez les bûchettes sur",
            "une assiette avec la vinaigrette.",
            "Décorez de brins d'aneth.",
            "VIN CONSEILLĖ",
            "Atinum, Bordeaux Graves",
            "MO, 1995, 75 cl",
            "ATINUM",
            "GRAVES"
        )

        assertLinesMatch(cleanedLines, expectedCleaned, "Étape 2 : Dépollution défaillante")
    }
}
