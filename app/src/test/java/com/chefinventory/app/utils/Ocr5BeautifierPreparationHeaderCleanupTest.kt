package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant que les en-têtes de section avec deux-points ("PRÉPARATION:", "INSTRUCTIONS:")
 * sont éliminés à 100% de la liste d'instructions finales par l'embellisseur.
 */
class Ocr5BeautifierPreparationHeaderCleanupTest {

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
    fun testBeautifyInstructions_EliminatesPreparationAndInstructionHeadersWithColons() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val inputInstructions = listOf(
            "PRÉPARATION:",
            "PRÉPARATION :",
            "INSTRUCTIONS:",
            "1. Coupez les tomates en dés.",
            "2. Mélangez avec l'huile d'olive."
        )

        val beautified = Ocr5Beautifier.beautifyInstructions(
            inputInstructions,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors,
            vocabulary.culinaryMeasurementUnits,
            vocabulary.ocrSpellingRepairs
        )

        assertWithMessage("L'en-tête 'PRÉPARATION:' ne doit pas figurer dans les instructions finales")
            .that(beautified)
            .doesNotContain("PRÉPARATION:")

        assertWithMessage("L'en-tête 'PRÉPARATION :' avec espace ne doit pas figurer dans les instructions finales")
            .that(beautified)
            .doesNotContain("PRÉPARATION :")

        assertWithMessage("L'en-tête 'INSTRUCTIONS:' ne doit pas figurer dans les instructions finales")
            .that(beautified)
            .doesNotContain("INSTRUCTIONS:")

        assertWithMessage("Les vraies consignes de préparation doivent être conservées")
            .that(beautified)
            .containsExactly(
                "Coupez les tomates en dés.",
                "Mélangez avec l'huile d'olive."
            )
            .inOrder()
    }
}
