package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 1 (Normalisation [Ocr1Corrector])
 * pour la recette des Salades d'Avocat et Princesses.
 */
class RecipeOcrAvocatPhase1Test {

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
     * Valide la Phase 1 : Normalisation des caractères, réparation des diacritiques et nombres ([Ocr1Corrector]).
     */
    @Test
    fun test1_Phase1_Normalization() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val rawLines = rawOcrScanText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val normalizedLines = rawLines.map { line ->
            Ocr1Corrector.correct(line, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs)
        }.filter { it.isNotBlank() }

        val expectedNormalized = listOf(
            "INGRÉDIENTS",
            "POUR",
            "4 PERSONNES",
            "1 avocat",
            "200 g de haricots",
            "princesses",
            "200 g de saumon cru",
            "aneth",
            "6 c. à soupe",
            "d'huile d'olive",
            "le jus de 1/2 citron",
            "1 c. à café de miel",
            "1 c. à soupe de vinaigre",
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
            "Saumur blanc, sec",
            "M.O., 1995, 75 cl"
        )

        assertLinesMatch(normalizedLines, expectedNormalized, "Étape 1 : Normalisation défaillante")
    }
}
