package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [RecipeMetadataParser1_BlockA_ServingsDiff].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeMetadataParser1_BlockA_ServingsDiffTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessServingsAndDifficulty_KcalDiffAndServings() {
        val results = RawSections()
        val rawLine = "250 kcal Facile Pour 4 personnes"
        val remaining = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(rawLine, rawLine, results)

        assertThat(results.detectedKcal).isEqualTo("250")
        assertThat(results.detectedDifficulty).isEqualTo("facile")
        assertThat(results.detectedServings).isEqualTo("4")
        assertThat(remaining).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessServingsAndDifficulty_BlankWorkingLineWithIngrOrInstrHeaderInLine() {
        val resultsIngr = RawSections()
        val remainingIngr = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(
            rawWorkingLine = "Pour 4 personnes",
            line = "Pour 4 personnes - Ingrédients",
            results = resultsIngr
        )
        assertThat(resultsIngr.detectedServings).isEqualTo("4")
        assertThat(remainingIngr).isEqualTo("ingrédients")

        val resultsInstr = RawSections()
        val remainingInstr = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(
            rawWorkingLine = "Pour 4 personnes",
            line = "Pour 4 personnes - Préparation",
            results = resultsInstr
        )
        assertThat(resultsInstr.detectedServings).isEqualTo("4")
        assertThat(remainingInstr).isEqualTo("préparation")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessServingsAndDifficulty_AlreadySetAndUnaccentedHeaders() {
        val results = RawSections()
        results.detectedKcal = "300"
        results.detectedDifficulty = "moyen"
        results.detectedServings = "2"

        val line = "250 kcal Facile Pour 4 personnes"
        RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(line, line, results)

        assertThat(results.detectedKcal).isEqualTo("300")
        assertThat(results.detectedDifficulty).isEqualTo("moyen")
        assertThat(results.detectedServings).isEqualTo("2")

        // Unaccented "ingredients" and "instructions"
        val resultsIngr = RawSections()
        val remainingIngr = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(
            rawWorkingLine = "pour 4 personnes",
            line = "ingredients pour 4 personnes",
            results = resultsIngr
        )
        assertThat(remainingIngr).isEqualTo("ingrédients")

        val resultsInstr = RawSections()
        val remainingInstr = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(
            rawWorkingLine = "pour 4 personnes",
            line = "instructions pour 4 personnes",
            results = resultsInstr
        )
        assertThat(remainingInstr).isEqualTo("préparation")
    }
}
