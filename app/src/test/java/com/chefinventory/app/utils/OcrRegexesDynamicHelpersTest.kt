package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
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

        assertThat(pattern.containsMatchIn(input)).isTrue()
    }

    @Test
    fun testGetPhonePrefixPattern_MatchesAccentedPhonePrefixesOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getPhonePrefixPattern("Tél")
        val input = "Tél: 02/542.42.42"

        assertThat(pattern.containsMatchIn(input)).isTrue()
    }

    @Test
    fun testGetAdPrefixPattern_MatchesAdExclusions() {
        val pattern = OcrRegexesDynamicHelpers.getAdPrefixPattern("Maxi GB")
        val input = "Plat disponible chez Maxi GB"

        assertThat(pattern.containsMatchIn(input)).isTrue()
    }

    @Test
    fun testGetInstructionPrefixPattern_MatchesActionVerbs() {
        val keywords = listOf("Coupez", "Plongez", "Mélangez")
        val pattern = OcrRegexesDynamicHelpers.getInstructionPrefixPattern(keywords)

        assertThat(pattern.containsMatchIn("1. Coupez les légumes")).isTrue()
    }

    @Test
    fun testGetSpellingRepairPattern_MatchesUppercaseOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getSpellingRepairPattern("salade")
        val input = "SALADE DE LANGOUSTINES AU CURRY"

        assertThat(pattern.matcher(input).find()).isTrue()
    }

    @Test
    fun testGetStemWordPattern_MatchesAccentedStemsOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getStemWordPattern("épinards")
        val input = "Crevettes grises aux épinards"

        assertThat(pattern.containsMatchIn(input)).isTrue()
    }

    @Test
    fun testGetWholeWordPattern_MatchesWholeWordsOnJvm() {
        val pattern = OcrRegexesDynamicHelpers.getWholeWordPattern("Bordeaux")
        val input = "Un bon vin de Bordeaux"

        assertThat(pattern.containsMatchIn(input)).isTrue()
    }
}
