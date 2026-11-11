package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Le temps '1 h 30' doit être converti en 90 minutes")
            .that(RecipeMetadataParser3_BlockC_Times.extractMinutes("1 h 30"))
            .isEqualTo("90")

        assertWithMessage("Une heure géante hors limites avec minutes '30' doit extraire les minutes '30'")
            .that(RecipeMetadataParser3_BlockC_Times.extractMinutes("99999999999999999999 h 30"))
            .isEqualTo("30")

        assertWithMessage("Le temps '1 h min' doit être converti en 60 minutes")
            .that(RecipeMetadataParser3_BlockC_Times.extractMinutes("1 h min"))
            .isEqualTo("60")

        assertWithMessage("Le temps '45 min' doit extraire 45 minutes")
            .that(RecipeMetadataParser3_BlockC_Times.extractMinutes("45 min"))
            .isEqualTo("45")

        assertWithMessage("Le nombre '15' seul doit extraire 15 minutes")
            .that(RecipeMetadataParser3_BlockC_Times.extractMinutes("15"))
            .isEqualTo("15")

        assertWithMessage("Un texte non numérique 'abc' doit retourner null")
            .that(RecipeMetadataParser3_BlockC_Times.extractMinutes("abc"))
            .isNull()
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

        assertWithMessage("Le temps de préparation '20' doit remplacer la valeur précédente '15'")
            .that(results.detectedPrepTime)
            .isEqualTo("20")

        assertWithMessage("Le temps de cuisson '35' doit remplacer la valeur précédente '30'")
            .that(results.detectedCookTime)
            .isEqualTo("35")

        assertWithMessage("Le temps de repos '60' doit être extrait")
            .that(results.detectedRestingTime)
            .isEqualTo("60")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessStandaloneTime_DirectCallAllBranches() {
        // 1. Prep time null -> sets prep time
        val res1 = RawSections()
        val line1 = RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 min", false, res1)
        assertWithMessage("Le premier temps isolé '15 min' doit être assigné au temps de préparation")
            .that(res1.detectedPrepTime)
            .isEqualTo("15")

        assertWithMessage("La ligne doit être vidée après extraction")
            .that(line1)
            .isEmpty()

        // 2. Prep time set, cook time null -> sets cook time
        val res2 = RawSections()
        res2.detectedPrepTime = "10"
        RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 min", false, res2)
        assertWithMessage("Le second temps isolé '15 min' doit être assigné au temps de cuisson")
            .that(res2.detectedCookTime)
            .isEqualTo("15")

        // 3. Both prep and cook set -> logs info and does not overwrite
        val res3 = RawSections()
        res3.detectedPrepTime = "10"
        res3.detectedCookTime = "20"
        RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 min", false, res3)
        assertWithMessage("Le temps de préparation déjà renseigné '10' ne doit pas être écrasé")
            .that(res3.detectedPrepTime)
            .isEqualTo("10")

        assertWithMessage("Le temps de cuisson déjà renseigné '20' ne doit pas être écrasé")
            .that(res3.detectedCookTime)
            .isEqualTo("20")

        // 4. Instruction sentence -> time preserved in line
        val res4 = RawSections()
        val line4 = RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 min", true, res4)
        assertWithMessage("Le temps de préparation '15' doit être extrait")
            .that(res4.detectedPrepTime)
            .isEqualTo("15")

        assertWithMessage("Une phrase de consigne doit conserver son texte '15 min'")
            .that(line4)
            .isEqualTo("15 min")

        // 5. Line >= 12 chars -> ignored
        val res5 = RawSections()
        val line5 = RecipeMetadataParser3_BlockC_Times.processStandaloneTime("15 minute chrono", false, res5)
        assertWithMessage("Une ligne longue (>= 12 chars) ne doit pas être traitée comme temps isolé")
            .that(res5.detectedPrepTime)
            .isNull()

        assertWithMessage("La ligne longue doit être retournée inchangée")
            .that(line5)
            .isEqualTo("15 minute chrono")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessTimesBlock_CombinedPrefixedAndStandalone() {
        // Combined times
        val resultsComb = RawSections()
        RecipeMetadataParser3_BlockC_Times.processTimesBlock("15 min + 30 min de cuisson", "15 min + 30 min de cuisson", null, resultsComb)
        assertWithMessage("Le temps combiné '15 min' de préparation doit être extrait")
            .that(resultsComb.detectedPrepTime)
            .isEqualTo("15")

        assertWithMessage("Le temps combiné '30 min' de cuisson doit être extrait")
            .that(resultsComb.detectedCookTime)
            .isEqualTo("30")

        // Combined time in bullet instruction sentence
        val resultsBulletComb = RawSections()
        RecipeMetadataParser3_BlockC_Times.processTimesBlock("15 min + 30 min de cuisson", "• 15 min + 30 min de cuisson", null, resultsBulletComb)
        assertWithMessage("Le temps de préparation de '15' doit être extrait de la ligne à puce")
            .that(resultsBulletComb.detectedPrepTime)
            .isEqualTo("15")

        assertWithMessage("Le temps de cuisson de '30' doit être extrait de la ligne à puce")
            .that(resultsBulletComb.detectedCookTime)
            .isEqualTo("30")

        // Combined times in instruction sentence
        val resultsCombInstr = RawSections()
        RecipeMetadataParser3_BlockC_Times.processTimesBlock("1. Prép : 15 min, Cuisson : 30 min", "1. Prép : 15 min, Cuisson : 30 min", null, resultsCombInstr)
        assertWithMessage("Le temps de préparation '15' doit être extrait de l'instruction préfixée")
            .that(resultsCombInstr.detectedPrepTime)
            .isEqualTo("15")

        // Instruction sentence with time
        val resultsInstr = RawSections()
        RecipeMetadataParser3_BlockC_Times.processTimesBlock("1. Cuisson : 15 min", "1. Cuisson : 15 min", null, resultsInstr)
        assertWithMessage("Le temps de cuisson '15' doit être extrait de l'instruction")
            .that(resultsInstr.detectedCookTime)
            .isEqualTo("15")
    }
}
