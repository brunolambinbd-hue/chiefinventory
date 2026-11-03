package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerInstructionTriggerDetector].
 */
class Ocr3CategorizerInstructionTriggerDetectorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertThat(Ocr3CategorizerInstructionTriggerDetector.toString()).contains("Ocr3CategorizerInstructionTriggerDetector")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasInstructionTrigger_AllBranches() {
        val triggers = listOf("préparation", "instructions", "étapes", "consignes")

        // 1. Line with instruction trigger
        assertThat(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("Préparation :", triggers)).isTrue()
        assertThat(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("Instructions pour la cuisson", triggers)).isTrue()

        // 2. Line without trigger
        assertThat(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("200 g de farine", triggers)).isFalse()

        // 3. Blank or empty line -> false
        assertThat(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("", triggers)).isFalse()
        assertThat(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("   ", triggers)).isFalse()

        // 4. Empty triggers list -> false
        assertThat(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("Préparation", emptyList())).isFalse()
    }
}
