package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 1, 'I', 'l', 'L', '|' collé devant des chiffres (ex: '(I25 g)', 'l50 g') est systématiquement converti en chiffre 1.
 */
class Ocr1CorrectorDigitOneTest {

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
    fun testCorrect_LetterOneBeforeDigits_ConvertsToOneDigit() {
        val testCases = mapOf(
            "I yaourt nature (I25 g)" to "1 yaourt nature (125 g)",
            "l50 g de farine" to "150 g de farine",
            "I00 ml de lait" to "100 ml de lait"
        )

        testCases.forEach { (rawInput, expectedOutput) ->
            val corrected = Ocr1Corrector.correct(rawInput)
            assertWithMessage("Pour la ligne '$rawInput', le 'I'/'l' collé devant des chiffres doit être converti en '1'")
                .that(corrected)
                .isEqualTo(expectedOutput)
        }
    }
}
