package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la beautification, la majusculation et le dé-doublonnage
 * des sources par [Ocr5SourceBeautifier].
 */
class Ocr5SourceBeautifierTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifySources_EmptyList_ReturnsEmpty() {
        assertThat(Ocr5SourceBeautifier.beautifySources(emptyList())).isEmpty()
        assertThat(Ocr5SourceBeautifier.beautifySources(listOf("  ", ""))).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifySources_CapitalizationAndPunctuationAndDeduplication() {
        val input = listOf(
            "hôtel hilton, place rogier",
            ": hôtel hilton, place rogier,",
            "  02/542.42.42  "
        )

        val result = Ocr5SourceBeautifier.beautifySources(input)

        assertThat(result).containsExactly(
            "Hôtel hilton, place rogier",
            "02/542.42.42"
        ).inOrder()
    }
}
