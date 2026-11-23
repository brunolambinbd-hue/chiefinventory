package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 1, une lettre 'e' isolée en fin de ligne (ex: 'Découpez le poulet e') est automatiquement corrigée en 'et'.
 */
class OcrEtTruncationTest {

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
    fun testCorrect_TruncatedEAtLineEnd_ConvertsToEt() {
        val testCases = mapOf(
            "Découpez le poulet e" to "Découpez le poulet et",
            "Mélangez le yaourt e" to "Mélangez le yaourt et"
        )

        testCases.forEach { (rawInput, expectedOutput) ->
            val corrected = Ocr1Corrector.correct(rawInput)
            assertWithMessage("La lettre 'e' isolée en fin de ligne '$rawInput' doit être corrigée en 'et'")
                .that(corrected)
                .isEqualTo(expectedOutput)
        }
    }
}
