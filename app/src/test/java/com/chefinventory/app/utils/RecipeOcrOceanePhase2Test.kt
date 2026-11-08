package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 2 (Dépollution [Ocr2Cleaner])
 * pour la recette des Salades Océanes.
 */
class RecipeOcrOceanePhase2Test {

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

    /**
     * Valide la Phase 2 : Dépollution du bruit de numérisation OCR et des bannières ([Ocr2Cleaner]).
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
            "250 g de crevettes",
            "grises décortiquées",
            "4 petites tomates",
            "(ou 2 grosses)",
            "1 beau chicon",
            "12 salade frisée",
            "1 orange",
            "250 g de yaourt nature",
            "2 c. à soupe de",
            "mayonnaise",
            "2 c. à soupe de ketchup",
            "1 c. à soupe de",
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
            "• Mélangez le yaourt, le ketchup",
            "la mayonnaise et la ciboulette.",
            "Ajoutez du Tabasco selon vos",
            "goûts, salez et poivrez. Réservez.",
            "• Lavez et essorez la salade, ajou-",
            "tez-y le chicon nettoyé et émincé.",
            "Disposez sur de grandes assiettes",
            "et saupoudrez de zeste d'orange.",
            "•Coupez les tomates en huit",
            "répartissez-les sur les assiettes et",
            "garnissez le centre de crevettes",
            "grises.",
            "• Servez frais (ne laissez pas trop",
            "longtemps en attente); présentez la",
            "sauce à part.",
            "VIN CONSEILLĖ",
            "Touraine",
            "Sauvignon blanc, 1995, 75 cl"
        )

        assertLinesMatch(cleanedLines, expectedCleaned, "Étape 2 : Dépollution défaillante")
    }
}
