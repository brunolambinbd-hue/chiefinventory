package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la préservation du trait d'union grammatical impératif français
 * ('taillez-les', 'coupez-les', 'ajoutez-y') lors de la fusion d'instructions par [Ocr4Merger.mergeInstructions].
 */
class OcrImperativeHyphenTest {

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
    fun testMergeInstructions_PreservesImperativeGrammaticalHyphen() {
        val input1 = listOf("Épluchez les pommes de terre et taillez-", "les en petits dés.")
        val result1 = Ocr4Merger.mergeInstructions(input1)
        assertWithMessage("Le trait d'union grammatical impératif 'taillez-les' doit être conservé")
            .that(result1)
            .containsExactly("Épluchez les pommes de terre et taillez-les en petits dés.")

        val input2 = listOf("Prenez les oignons et coupez-", "les finement.")
        val result2 = Ocr4Merger.mergeInstructions(input2)
        assertWithMessage("Le trait d'union grammatical impératif 'coupez-les' doit être conservé")
            .that(result2)
            .containsExactly("Prenez les oignons et coupez-les finement.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_WeldsStandardWordCesureWithoutHyphen() {
        val input = listOf("une pin-", "cée de sel.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertWithMessage("La césure de mot ordinaire 'pin-' + 'cée' doit être soudée en 'pincée'")
            .that(result)
            .containsExactly("une pincée de sel.")
    }
}
