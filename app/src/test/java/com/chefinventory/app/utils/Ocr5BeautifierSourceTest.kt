package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la beautification, la majusculation et le dé-doublonnage
 * des sources par [Ocr5BeautifierSource].
 */
class Ocr5BeautifierSourceTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifySources_EmptyList_ReturnsEmpty() {
        assertThat(Ocr5BeautifierSource.beautifySources(emptyList())).isEmpty()
        assertThat(Ocr5BeautifierSource.beautifySources(listOf("  ", ""))).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifySources_PunctuationOnlyAndAlreadyCapitalizedAndDigitStarts() {
        val input = listOf(
            ": : .",
            "Hôtel Hilton",
            "02/542.42.42"
        )

        val result = Ocr5BeautifierSource.beautifySources(input)

        assertThat(result).containsExactly(
            "Hôtel Hilton",
            "02/542.42.42"
        ).inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifySources_CapitalizationAndPunctuationAndDeduplication() {
        val input = listOf(
            "hôtel hilton, place rogier",
            ": hôtel hilton, place rogier,",
            "  02/542.42.42  "
        )

        val result = Ocr5BeautifierSource.beautifySources(input)

        assertThat(result).containsExactly(
            "Hôtel hilton, place rogier",
            "02/542.42.42"
        ).inOrder()
    }
}
