package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la beautification, la majusculation et le dé-doublonnage
 * des vins par [Ocr5BeautifierWine].
 */
class Ocr5BeautifierWineTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyWines_EmptyList_ReturnsEmpty() {
        assertThat(Ocr5BeautifierWine.beautifyWines(emptyList())).isEmpty()
        assertThat(Ocr5BeautifierWine.beautifyWines(listOf("  ", ""))).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyWines_PunctuationOnlyAndAlreadyCapitalizedAndDigitStarts() {
        val input = listOf(
            ": : .",
            "Bordeaux 1996 75 cl",
            "2015 Château Margaux"
        )

        val result = Ocr5BeautifierWine.beautifyWines(input)

        assertThat(result).containsExactly(
            "Bordeaux 1996 75 cl",
            "2015 Château Margaux"
        ).inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyWines_CapitalizationAndPunctuationAndDeduplication() {
        val input = listOf(
            "bordeaux 1996 75 cl",
            ": bordeaux 1996 75 cl,",
            "  château margaux 2015  "
        )

        val result = Ocr5BeautifierWine.beautifyWines(input)

        assertThat(result).containsExactly(
            "Bordeaux 1996 75 cl",
            "Château margaux 2015"
        ).inOrder()
    }
}
