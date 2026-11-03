package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [RecipeMetadataParser3_BlockC_Times].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeMetadataParser3_BlockC_TimesTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMinutes_AllFormats() {
        assertThat(RecipeMetadataParser3_BlockC_Times.extractMinutes("1 h 30")).isEqualTo("90")
        assertThat(RecipeMetadataParser3_BlockC_Times.extractMinutes("99999999999999999999 h 30")).isEqualTo("30")
        assertThat(RecipeMetadataParser3_BlockC_Times.extractMinutes("1 h min")).isEqualTo("60")
        assertThat(RecipeMetadataParser3_BlockC_Times.extractMinutes("45 min")).isEqualTo("45")
        assertThat(RecipeMetadataParser3_BlockC_Times.extractMinutes("15")).isEqualTo("15")
        assertThat(RecipeMetadataParser3_BlockC_Times.extractMinutes("abc")).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testUpdateTimeResult_PrepCookRestingAndUnknown() {
        val results = RawSections()
        RecipeMetadataParser3_BlockC_Times.updateTimeResult(results, "préparation", "15")
        RecipeMetadataParser3_BlockC_Times.updateTimeResult(results, "prep", "20")
        RecipeMetadataParser3_BlockC_Times.updateTimeResult(results, "cuis", "30")
        RecipeMetadataParser3_BlockC_Times.updateTimeResult(results, "cuisson", "35")
        RecipeMetadataParser3_BlockC_Times.updateTimeResult(results, "repos", "60")
        RecipeMetadataParser3_BlockC_Times.updateTimeResult(results, "inconnu", "100")

        assertThat(results.detectedPrepTime).isEqualTo("20")
        assertThat(results.detectedCookTime).isEqualTo("35")
        assertThat(results.detectedRestingTime).isEqualTo("60")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessStandaloneTime_DirectCallAllBranches() {
        // 1. Prep time null -> sets prep time
        val res1 = RawSections()
        val line1 = RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 min", false, res1)
        assertThat(res1.detectedPrepTime).isEqualTo("15")
        assertThat(line1).isEmpty()

        // 2. Prep time set, cook time null -> sets cook time
        val res2 = RawSections()
        res2.detectedPrepTime = "10"
        RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 min", false, res2)
        assertThat(res2.detectedCookTime).isEqualTo("15")

        // 3. Both prep and cook set -> logs info and does not overwrite
        val res3 = RawSections()
        res3.detectedPrepTime = "10"
        res3.detectedCookTime = "20"
        RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 min", false, res3)
        assertThat(res3.detectedPrepTime).isEqualTo("10")
        assertThat(res3.detectedCookTime).isEqualTo("20")

        // 4. Instruction sentence -> time preserved in line
        val res4 = RawSections()
        val line4 = RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 min", true, res4)
        assertThat(res4.detectedPrepTime).isEqualTo("15")
        assertThat(line4).isEqualTo("15 min")

        // 5. Line >= 12 chars -> ignored
        val res5 = RawSections()
        val line5 = RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 minute chrono", false, res5)
        assertThat(res5.detectedPrepTime).isNull()
        assertThat(line5).isEqualTo("15 minute chrono")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessTimesBlock_CombinedPrefixedAndStandalone() {
        // Combined times
        val resultsComb = RawSections()
        RecipeMetadataParser3_BlockC_Times.processTimesBlock("15 min + 30 min de cuisson", "15 min + 30 min de cuisson", null, resultsComb)
        assertThat(resultsComb.detectedPrepTime).isEqualTo("15")
        assertThat(resultsComb.detectedCookTime).isEqualTo("30")

        // Combined time in bullet instruction sentence
        val resultsBulletComb = RawSections()
        RecipeMetadataParser3_BlockC_Times.processTimesBlock("15 min + 30 min de cuisson", "• 15 min + 30 min de cuisson", null, resultsBulletComb)
        assertThat(resultsBulletComb.detectedPrepTime).isEqualTo("15")
        assertThat(resultsBulletComb.detectedCookTime).isEqualTo("30")

        // Combined times in instruction sentence
        val resultsCombInstr = RawSections()
        RecipeMetadataParser3_BlockC_Times.processTimesBlock("1. Prép : 15 min, Cuisson : 30 min", "1. Prép : 15 min, Cuisson : 30 min", null, resultsCombInstr)
        assertThat(resultsCombInstr.detectedPrepTime).isEqualTo("15")

        // Instruction sentence with time
        val resultsInstr = RawSections()
        RecipeMetadataParser3_BlockC_Times.processTimesBlock("1. Cuisson : 15 min", "1. Cuisson : 15 min", null, resultsInstr)
        assertThat(resultsInstr.detectedCookTime).isEqualTo("15")
    }
}
