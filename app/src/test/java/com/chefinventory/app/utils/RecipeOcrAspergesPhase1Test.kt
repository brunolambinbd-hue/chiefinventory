package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 1 (Normalisation [Ocr1Corrector])
 * pour la recette des Asperges vertes au parmesan.
 */
class RecipeOcrAspergesPhase1Test {

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
            "cet te",
            "INGRÉDIENTS",
            "POUR",
            "4 PERSONNES",
            "1 kg d'asperges vertes",
            "4 tranches",
            "de jambon séché",
            "4 c. à soupe",
            "de parmesan",
            "(moulu ou en morceaux)",
            "6 c. à soupe",
            "d'huile d'olive",
            "le jus de 1/2 citron",
            "GB",
            "20",
            "• Etuvez les asperges pendant 8 à",
            "10 minutes en veillant à les laisser",
            "croquantes (il n'est pas nécessaire",
            "d'éplucher les asperges vertes).",
            "Assiette de présentation disponible dans les Maxi GB",
            "sperges vertes au parmesan",
            "• Répartissez-les sur 4 assiettes",
            "déposez sur chacune d'elles une",
            "tranche de jambon séché.",
            "Saupoudrez de parmesan.",
            "• Nappez d'huile d'olive et",
            "arrosez de jus de citron.",
            "VIN CONSEILLĖ",
            "Muscat d'Alsace, caves de",
            "Pfaffenheim, MO, 1996, 75 cl"
        )

        assertLinesMatch(normalizedLines, expectedNormalized, "Étape 1 : Normalisation défaillante")
    }
}
