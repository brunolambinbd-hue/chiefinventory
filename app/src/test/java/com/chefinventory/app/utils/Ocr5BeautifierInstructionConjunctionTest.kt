package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD dédié spécifiquement à la validation du traitement des conjonctions de fin de ligne dans [Ocr5BeautifierInstruction].
 */
class Ocr5BeautifierInstructionConjunctionTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun testBeautifyInstructions_handlesConjunctionsAndPunctuationCorrectly() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawInstructions = listOf(
            "Délayez le curry avec la mayonnaise et.",
            "Ajoutez le yaourt et",
            "Un trait de jus de citron.",
            "Lavez les légumes,.",
            "Puis servez frais."
        )

        val beautified = Ocr5BeautifierInstruction.beautifyInstructions(
            rawInstructions,
            stepActionKeywords = vocabulary.stepActionKeywords,
            hangingConnectors = vocabulary.hangingConnectors,
            culinaryUnits = vocabulary.culinaryMeasurementUnits,
            spellingRepairs = vocabulary.ocrSpellingRepairs
        )

        val expected = listOf(
            "Délayez le curry avec la mayonnaise et ajoutez le yaourt et un trait de jus de citron.",
            "Lavez les légumes, puis servez frais."
        )

        assertThat(beautified).isEqualTo(expected)
    }
}
