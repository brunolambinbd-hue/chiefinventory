package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires isolés vérifiant les générateurs de Regexes dynamiques de [OcrRegexesDynamicHelpers].
 */
class OcrRegexesDynamicHelpersTest {

    @Before
    fun setUp() {
        OcrRegexesDynamicHelpers.clearCache()
    }

    @After
    fun tearDown() {
        OcrRegexesDynamicHelpers.clearCache()
    }

    @Test
    fun testGetUnitPattern_MatchesAccentedUnitsAtLineEndOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getUnitPattern("c. à café")
        val input = "c. à café"

        assertWithMessage("Le motif d'unité dynamique doit matcher l'unité accentuée 'c. à café' en fin de ligne")
            .that(pattern.containsMatchIn(input))
            .isTrue()
    }

    @Test
    fun testGetPhonePrefixPattern_MatchesAccentedPhonePrefixesOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getPhonePrefixPattern("Tél")
        val input = "Tél: 02/542.42.42"

        assertWithMessage("Le motif de préfixe téléphonique doit matcher 'Tél:'")
            .that(pattern.containsMatchIn(input))
            .isTrue()
    }

    @Test
    fun testGetAdPrefixPattern_MatchesAdExclusions() {
        val pattern = OcrRegexesDynamicHelpers.getAdPrefixPattern("Maxi GB")
        val input = "Plat disponible chez Maxi GB"

        assertWithMessage("Le motif de préfixe publicitaire doit matcher l'enseigne 'Maxi GB'")
            .that(pattern.containsMatchIn(input))
            .isTrue()
    }

    @Test
    fun testGetInstructionPrefixPattern_MatchesActionVerbs() {
        val keywords = listOf("Coupez", "Plongez", "Mélangez")
        val pattern = OcrRegexesDynamicHelpers.getInstructionPrefixPattern(keywords)

        assertWithMessage("Le motif de préfixe d'instruction doit matcher la consigne '1. Coupez les légumes'")
            .that(pattern.containsMatchIn("1. Coupez les légumes"))
            .isTrue()
    }

    @Test
    fun testGetSpellingRepairPattern_MatchesUppercaseOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getSpellingRepairPattern("salade")
        val input = "SALADE DE LANGOUSTINES AU CURRY"

        assertWithMessage("Le motif de réparation orthographique doit matcher le mot majuscule 'SALADE' sur la JVM")
            .that(pattern.matcher(input).find())
            .isTrue()
    }

    @Test
    fun testGetStemWordPattern_MatchesAccentedStemsOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getStemWordPattern("épinards")
        val input = "Crevettes grises aux épinards"

        assertWithMessage("Le motif de radical d'ingrédient doit matcher le mot accentué 'épinards'")
            .that(pattern.containsMatchIn(input))
            .isTrue()
    }

    @Test
    fun testGetWholeWordPattern_MatchesWholeWordsOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getWholeWordPattern("Bordeaux")
        val input = "Un bon vin de Bordeaux"

        assertWithMessage("Le motif de mot entier doit matcher 'Bordeaux'")
            .that(pattern.containsMatchIn(input))
            .isTrue()
    }
}
