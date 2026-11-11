package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD dédié spécifiquement à la validation des cas de dépollution et beautification dans [Ocr5BeautifierSource].
 */
class Ocr5BeautifierSourceSanitizeTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    /**
     * Cas 1 : Rejet automatique des symboles et glyphes bruits (ex: "©", "§", "Ə", "• - *", "| ! -").
     */
    @Test
    fun testBeautifySources_rejectsGlyphsAndSymbols() {
        val rawSources = listOf(
            "©",
            "§",
            "Ə",
            "• - *",
            "| ! -",
            "Nahit YILMAZ, Hôtel Conrad International"
        )

        val beautified = Ocr5BeautifierSource.beautifySources(rawSources)

        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(beautified)
            .containsExactly("Nahit YILMAZ, Hôtel Conrad International")
    }

    /**
     * Cas 2 : Rejet des lettres seules et fragments orphelins (ex: "E", "e t", "a", "de").
     */
    @Test
    fun testBeautifySources_rejectsOrphanSingleLettersAndShortNoise() {
        val rawSources = listOf(
            "E",
            "e t",
            "de",
            "a",
            "Avenue Louise 71, 1050 BRUXELLES"
        )

        val beautified = Ocr5BeautifierSource.beautifySources(rawSources)

        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(beautified)
            .containsExactly("Avenue Louise 71, 1050 BRUXELLES")
    }

    /**
     * Cas 3a : Retrait de la ponctuation initiale ":" et majusculation.
     */
    @Test
    fun testBeautifySources_cleansLeadingColonAndCapitalizes() {
        val rawSources = listOf(": hôtel conrad international")

        val beautified = Ocr5BeautifierSource.beautifySources(rawSources)

        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(beautified)
            .containsExactly("Hôtel conrad international")
    }

    /**
     * Cas 3b : Retrait du tiret initial "-" et majusculation.
     */
    @Test
    fun testBeautifySources_cleansLeadingDashAndCapitalizes() {
        val rawSources = listOf("- maxi GB bruxelles")

        val beautified = Ocr5BeautifierSource.beautifySources(rawSources)

        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(beautified)
            .containsExactly("Maxi GB bruxelles")
    }
}
