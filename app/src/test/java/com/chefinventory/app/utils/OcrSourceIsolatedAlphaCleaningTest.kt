package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau Étape 5 ([Ocr5BeautifierSource]) vérifiant la purge
 * des lettres alpha isolées et du bruit de mise en page dans les sources ('A. O', 'O B', 'A.').
 */
class OcrSourceIsolatedAlphaCleaningTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifySources_PurgesIsolatedAlphaLetters_AndKeepsValidBrands() {
        val rawSources = listOf(
            "Maxi GB",
            "A Maxi GB",
            "Maxi GB O",
            "A. O",
            "O B",
            "A.",
            "RTL",
            "M6"
        )

        val beautified = Ocr5BeautifierSource.beautifySources(rawSources)

        assertWithMessage("Les sources doivent conserver 'Maxi GB'")
            .that(beautified)
            .contains("Maxi GB")

        assertWithMessage("'A Maxi GB' doit être nettoyé en 'Maxi GB'")
            .that(Ocr5BeautifierSourceNoiseHelper.cleanIsolatedAlphaNoiseFromSource("A Maxi GB"))
            .isEqualTo("Maxi GB")

        assertWithMessage("'Maxi GB O' doit être nettoyé en 'Maxi GB'")
            .that(Ocr5BeautifierSourceNoiseHelper.cleanIsolatedAlphaNoiseFromSource("Maxi GB O"))
            .isEqualTo("Maxi GB")

        assertWithMessage("Les sources doivent conserver 'RTL'")
            .that(beautified)
            .contains("RTL")

        assertWithMessage("Les sources doivent conserver 'M6'")
            .that(beautified)
            .contains("M6")

        assertWithMessage("Les sources ne doivent PAS contenir le bruit 'A. O'")
            .that(beautified)
            .doesNotContain("A. O")

        assertWithMessage("Les sources ne doivent PAS contenir le bruit 'O B'")
            .that(beautified)
            .doesNotContain("O B")

        assertWithMessage("Les sources ne doivent PAS contenir le bruit 'A.'")
            .that(beautified)
            .doesNotContain("A.")
    }
}
