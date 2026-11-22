package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que les adjectifs culinaires (frais, haché, émincé, jaunes) ne passent PAS dans Levenshtein pour éviter toute déviation vers des noms d'aliments (fraise, mâche).
 */
class OcrIngredientAdjectiveLevenshteinExclusionTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_ExcludesAdjectivesFromLevenshteinReplacements() {
        val input = listOf(
            "200 g d'épinards frais",
            "1 c. à soupe de persil haché",
            "3 jaunes d'œufs"
        )

        // Simuler un dictionnaire d'Open Food Facts contenant "fraise" et "mâche"
        val offTaxonomy = listOf("fraise", "mâche", "épinards", "persil", "œufs")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(
            ingredients = input,
            offTaxonomyList = offTaxonomy
        )

        assertWithMessage("L'adjectif 'frais' ne doit JAMAIS être dévié vers 'fraise'")
            .that(result)
            .contains("200 g d'épinards frais")

        assertWithMessage("L'adjectif 'haché' ne doit JAMAIS être dévié vers 'mâche'")
            .that(result)
            .contains("1 c. à soupe de persil haché")

        assertWithMessage("L'adjectif 'jaunes' ne doit JAMAIS être dévié vers un nom d'aliment")
            .that(result)
            .contains("3 jaunes d'œufs")
    }
}
