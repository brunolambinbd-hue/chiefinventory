package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Le composant Ocr3CategorizerInstructionTriggerDetector doit générer une représentation textuelle valide")
            .that(Ocr3CategorizerInstructionTriggerDetector.toString())
            .contains("Ocr3CategorizerInstructionTriggerDetector")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasInstructionTrigger_AllBranches() {
        val triggers = listOf("préparation", "instructions", "étapes", "consignes")

        // 1. Line with instruction trigger
        assertWithMessage("La ligne 'Préparation :' doit être reconnue comme déclencheur d'instruction")
            .that(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("Préparation :", triggers))
            .isTrue()

        assertWithMessage("La ligne 'Instructions pour la cuisson' doit être reconnue comme déclencheur d'instruction")
            .that(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("Instructions pour la cuisson", triggers))
            .isTrue()

        // 2. Line without trigger
        assertWithMessage("La ligne '200 g de farine' ne doit pas être qualifiée de déclencheur d'instruction")
            .that(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("200 g de farine", triggers))
            .isFalse()

        // 3. Blank or empty line -> false
        assertWithMessage("Une ligne vide ne doit pas être qualifiée de déclencheur d'instruction")
            .that(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("", triggers))
            .isFalse()

        assertWithMessage("Une ligne d'espaces ne doit pas être qualifiée de déclencheur d'instruction")
            .that(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("   ", triggers))
            .isFalse()

        // 4. Empty triggers list -> false
        assertWithMessage("Une liste de déclencheurs vide doit retourner faux")
            .that(Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger("Préparation", emptyList()))
            .isFalse()
    }
}
