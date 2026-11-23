package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 5 (Beautifier), l'élision 'd'' ou 'l'' sur un mot (ex: "d'arachide", "d'olive") est préservée lors du remplacement Levenshtein avec le dictionnaire.
 */
class OcrElisionBeautifierTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_PreservesElisionPrefix() {
        val input = listOf("1 c. à soupe d'huile d'arachide", "1 c. à soupe d'huile d'olive")
        val offTaxonomy = listOf("arachide", "olive", "huile d'olive")

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("L'élision 'd'' de 'd'arachide' et 'd'olive' doit être préservée en Étape 5 (0 disparition du d')")
            .that(beautified)
            .containsExactly("1 c. à soupe d'huile d'arachide", "1 c. à soupe d'huile d'olive")
            .inOrder()
    }
}
