package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD dédié spécifiquement à la validation des cas de dépollution et beautification dans [Ocr5BeautifierWine].
 */
class Ocr5BeautifierWineSanitizeTest {

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
    fun testBeautifyWines_rejectsGlyphsAndSymbols() {
        val rawWines = listOf(
            "©",
            "§",
            "Ə",
            "• - *",
            "| ! -",
            "Bordeaux Graves 1995 75 cl"
        )

        val beautified = Ocr5BeautifierWine.beautifyWines(rawWines)

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(beautified)
            .containsExactly("Bordeaux Graves 1995 75 cl")
    }

    /**
     * Cas 2 : Rejet des lettres seules et fragments orphelins (ex: "E", "e t", "a", "de").
     */
    @Test
    fun testBeautifyWines_rejectsOrphanSingleLettersAndShortNoise() {
        val rawWines = listOf(
            "E",
            "e t",
            "de",
            "a",
            "Touraine Sauvignon blanc, 1995, 75 cl"
        )

        val beautified = Ocr5BeautifierWine.beautifyWines(rawWines)

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(beautified)
            .containsExactly("Touraine Sauvignon blanc, 1995, 75 cl")
    }

    /**
     * Cas 3a : Retrait de la ponctuation initiale ":" et majusculation.
     */
    @Test
    fun testBeautifyWines_cleansLeadingColonAndCapitalizes() {
        val rawWines = listOf(": bordeaux graves 1995 75 cl")

        val beautified = Ocr5BeautifierWine.beautifyWines(rawWines)

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(beautified)
            .containsExactly("Bordeaux graves 1995 75 cl")
    }

    /**
     * Cas 3b : Retrait du tiret initial "-" et majusculation.
     */
    @Test
    fun testBeautifyWines_cleansLeadingDashAndCapitalizes() {
        val rawWines = listOf("- muscat d'Alsace 1996")

        val beautified = Ocr5BeautifierWine.beautifyWines(rawWines)

        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(beautified)
            .containsExactly("Muscat d'Alsace 1996")
    }
}
