package com.chefinventory.app.utils

import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration dédié spécifiquement à la Phase 1 (Normalisation [Ocr1Corrector])
 * pour la recette des Canapés grecs.
 */
class RecipeOcrCanapesGrecsPhase1Test {

    private val rawOcrScanText = listOf(
        "Saveur de",
        "Danapés grecs",
        "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-",
        "cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur + 12 heures.",
        "Découpez la baguette en tranches d'environ 1 cm, faites-les légèrement griller puis frottez-les",
        "d'ail, égouttez le fromage et placez-le sur le pain.",
        "Décorezd'olives coupées en deux et de romarin.",
        "Ingrédients pour 4 personnes",
        "180 g de feta",
        "1/2 verre d'huile d'olive",
        "10 olives vertes dénoyautées",
        "10 olives noires dénoyautées",
        "I petit piment",
        "I gousse d'ail",
        "I baguette de pain gris",
        "du thym et du romarin frais",
        "sel et poivre du moulin"
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
    fun test1_Phase1_Normalization() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val rawLines = rawOcrScanText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val normalizedLines = rawLines.map { line ->
            Ocr1Corrector.correct(line, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs)
        }.filter { it.isNotBlank() }

        val expectedNormalized = listOf(
            "Saveur de",
            "Danapés grecs",
            "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-",
            "cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur 1/2 heures.",
            "Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les",
            "d'ail, égouttez le fromage et placez-le sur le pain.",
            "Décorez d'olives coupées en deux et de romarin.",
            "ingrédients pour 4 personnes",
            "180 g de feta",
            "1/2 verre d'huile d'olive",
            "10 olives vertes dénoyautées",
            "10 olives noires dénoyautées",
            "1 petit piment",
            "1 gousse d'ail",
            "1 baguette de pain gris",
            "du thym et du romarin frais",
            "sel et poivre du moulin"
        )

        assertLinesMatch(normalizedLines, expectedNormalized, "Étape 1 : Normalisation défaillante")
    }
}
