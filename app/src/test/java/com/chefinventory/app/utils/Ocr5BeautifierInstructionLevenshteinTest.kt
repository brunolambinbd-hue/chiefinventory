package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la correction des mots d'ingrédients candidats cités dans les instructions ([Ocr5BeautifierInstruction]).
 */
class Ocr5BeautifierInstructionLevenshteinTest {

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
    fun testBeautifyInstructions_FixesCandidateIngredientWordsInInstructionsAtStep5() {
        val input = listOf(
            "Prélevez finement le zeste de lorange.",
            "Hachez loignon et les épinards.",
            "Faites cuire pendant 10 minutes."
        )

        val offTaxonomy = listOf("orange", "oignon")
        val result = Ocr5BeautifierInstruction.beautifyInstructions(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("La typo 'lorange' dans 'zeste de lorange' doit être corrigée en 'l'orange'")
            .that(result)
            .contains("Prélevez finement le zeste de l'orange.")

        assertWithMessage("La typo 'loignon' dans 'Hachez loignon' doit être corrigée en 'l'oignon'")
            .that(result)
            .contains("Hachez l'oignon et les épinards.")

        assertWithMessage("La consigne de cuisson avec verbes d'action et unité de temps 'minutes' doit rester intacte")
            .that(result)
            .contains("Faites cuire pendant 10 minutes.")
    }
}
