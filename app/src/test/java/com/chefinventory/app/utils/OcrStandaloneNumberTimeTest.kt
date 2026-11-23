package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'un chiffre isolé sur sa ligne (ex: '20', '15', '30') est reconnu comme méta-donnée de durée (prepTime/cookTime) et purgé du flux.
 */
class OcrStandaloneNumberTimeTest {

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
    fun testProcessTimesBlock_StandaloneNumber_ExtractedAsPrepTimeAndPurged() {
        val results = RawSections()
        val cleanedLine = RecipeMetadataParser3_BlockC_Times.processTimesBlock("20", "20", null, results)

        assertWithMessage("Le chiffre isolé '20' doit être capturé comme temps de préparation")
            .that(results.detectedPrepTime)
            .isEqualTo("20")

        assertWithMessage("La ligne '20' doit être vidée (purger comme méta-donnée pure)")
            .that(cleanedLine)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessTimesBlock_TwoStandaloneNumbers_ExtractedAsPrepAndCookTime() {
        val results = RawSections()
        val cleaned1 = RecipeMetadataParser3_BlockC_Times.processTimesBlock("20", "20", null, results)
        val cleaned2 = RecipeMetadataParser3_BlockC_Times.processTimesBlock("45", "45", null, results)

        assertWithMessage("Le 1er chiffre isolé '20' doit être capturé comme temps de préparation")
            .that(results.detectedPrepTime)
            .isEqualTo("20")

        assertWithMessage("Le 2e chiffre isolé '45' doit être capturé comme temps de cuisson")
            .that(results.detectedCookTime)
            .isEqualTo("45")

        assertWithMessage("Les deux lignes doivent être vidées (purgées comme méta-données pures)")
            .that(cleaned1)
            .isEmpty()

        assertWithMessage("La 2e ligne doit être vidée")
            .that(cleaned2)
            .isEmpty()
    }
}
