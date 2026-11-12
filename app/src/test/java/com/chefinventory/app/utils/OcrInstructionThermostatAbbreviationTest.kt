package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant que les abréviations culinaires se terminant par un point ("th.", "c.", "temp.")
 * ne coupent pas la consigne d'instructions et fusionnent la valeur de température/thermostat ("7 - 210°C") dans la même phrase.
 */
class OcrInstructionThermostatAbbreviationTest {

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
    fun testMergeInstructions_DoesNotSplitOnThermostatAbbreviationDot() {
        val inputInstructions = listOf(
            "Vous pouvez faire dorer les nems dans la friteuse (180) ou alors au four (préchauffé sur th.",
            "7 - 210)."
        )

        val merged = Ocr4Merger.mergeInstructions(inputInstructions)

        assertWithMessage("La consigne tronquée après 'th.' doit fusionner avec '7 - 210).' en une seule phrase")
            .that(merged)
            .containsExactly(
                "Vous pouvez faire dorer les nems dans la friteuse (180) ou alors au four (préchauffé sur th. 7 - 210)."
            )
    }


}
