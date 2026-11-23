package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que '1 baguette de pain gris' et 'crevettes grises' conservent l'adjectif 'gris/grises' intact sans déviation Levenshtein en 'ghis'.
 */
class OcrPainGrisTest {

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
    fun testBeautifyIngredients_PreservesPainGrisAndCrevettesGrises() {
        val input = listOf("1 baguette de pain gris", "200 g de crevettes grises")
        val offTaxonomy = listOf("ghis", "pain", "crevettes")

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("L'adjectif 'gris' de '1 baguette de pain gris' ne doit JAMAIS être dévié par Levenshtein en 'ghis'")
            .that(beautified)
            .containsExactly("1 baguette de pain gris", "200 g de crevettes grises")
    }
}
