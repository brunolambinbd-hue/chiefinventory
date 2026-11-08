package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 2 (Dépollution [Ocr2Cleaner])
 * pour la recette des Salades de Langoustines au Curry.
 */
class RecipeOcrLangoustinePhase2Test {

    /** Capture brute réelle ML Kit du scan caméra (écrite une seule fois). */
    private val rawOcrScanText = listOf(
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
        "e t",
        "126",
        "alade de langoustines au curry",
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
            "r",
            "e",
            "e t",
            "ingrédients pour 4 personnes",
            "24 langoustines cuites",
            "1 mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "1 c. à café de curry, 1/2 citron",
            "1 yaourt nature (125 g)",
            "4 belles feuilles de salade",
            "cresson, sel et poivre",
            "e t",
            "alade de langoustines au curry",
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
            "Vous pouvez préparer une soupe avec les têtes et les carcasses des lan-",
            "goustines.",
            "Vin conseillé",
            "Gewurztraminer",
            "Caves de Pfaffenhein",
            "MO, 1992, 75 cl"
        )

        assertLinesMatch(cleanedLines, expectedCleaned, "Étape 2 : Dépollution défaillante")
    }
}
