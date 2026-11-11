package com.chefinventory.app.utils

import com.chefinventory.app.utils.Ocr5Beautifier.filterBeautifierNoise
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD dédié spécifiquement à la fonction d'extension [filterBeautifierNoise] de l'Étape 5.
 */
class Ocr5BeautifierNoiseFilterTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun testFilterBeautifierNoise_removesSymbolAndForeignGlyphLines() {
        val rawLines = listOf(
            "©",
            "Plongez les aubergines dans l'eau bouillante.",
            "§",
            "• - *",
            "200 g de farine",
            "Ə",
            "| ! -"
        )

        val filtered = rawLines.filterBeautifierNoise()

        val expected = listOf(
            "Plongez les aubergines dans l'eau bouillante.",
            "200 g de farine"
        )

        assertThat(filtered).isEqualTo(expected)
    }

    @Test
    fun testFilterBeautifierNoise_preservesValidFrenchTextAndLettrines() {
        val rawLines = listOf(
            "E",
            "10 min",
            "Éplucher les pommes",
            "Cœur de palmier",
            "Bûchettes de melon"
        )

        val filtered = rawLines.filterBeautifierNoise()

        assertThat(filtered).isEqualTo(rawLines)
    }
}
