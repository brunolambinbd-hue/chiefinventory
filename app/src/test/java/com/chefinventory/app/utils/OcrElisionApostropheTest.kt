package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 1, 'd' suivi d'un espace et d'une voyelle (ex: 'd olive', 'd ail') est automatiquement corrigé en 'd'' ('d'olive', 'd'ail').
 */
class OcrElisionApostropheTest {

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
    fun testCorrect_DWithoutApostropheBeforeVowel_InsertsApostrophe() {
        val testCases = mapOf(
            "d'huile d olive" to "d'huile d'olive",
            "1 gousse d ail" to "1 gousse d'ail",
            "jus d orange" to "jus d'orange",
            "huile d avocat" to "huile d'avocat"
        )

        testCases.forEach { (rawInput, expectedOutput) ->
            val corrected = Ocr1Corrector.correct(rawInput)
            assertWithMessage("La chaîne '$rawInput' doit insérer une apostrophe après le 'd' devant une voyelle")
                .that(corrected)
                .isEqualTo(expectedOutput)
        }
    }
}
