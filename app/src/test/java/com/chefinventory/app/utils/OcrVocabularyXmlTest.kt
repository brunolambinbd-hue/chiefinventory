package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires dédiés à l'intégrité du chargement et du contenu des fichiers XML de vocabulaire OCR.
 */
class OcrVocabularyXmlTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `loadFromProjectResources should load non-empty diacritic repairs list`() {
        assertThat(vocabulary.ocrDiacriticRepairs).isNotEmpty()
    }

    @Test
    fun `defaultDiacriticRepairs should convert accented characters correctly`() {
        assertThat(Ocr1Corrector.correct("ế")).isEqualTo("é")
        assertThat(Ocr1Corrector.correct("ề")).isEqualTo("è")
        assertThat(Ocr1Corrector.correct("ấ")).isEqualTo("â")
        assertThat(Ocr1Corrector.correct("ầ")).isEqualTo("à")
        assertThat(Ocr1Corrector.correct("ặ")).isEqualTo("a")
    }

    @Test
    fun `loadFromProjectResources should load non-empty orphan noun number and stop words`() {
        assertThat(vocabulary.orphanNounNumberWords).isNotEmpty()
        assertThat(vocabulary.orphanNounStopWords).isNotEmpty()
    }

    @Test
    fun `loadFromProjectResources should load non-empty protected headers`() {
        assertThat(vocabulary.ocr1ProtectedIngredientsHeaders).isNotEmpty()
        assertThat(vocabulary.ocr1ProtectedInstructionsHeaders).isNotEmpty()
    }

    @Test
    fun `loadFromProjectResources should load non-empty culinary measurement units`() {
        assertThat(vocabulary.culinaryMeasurementUnits).isNotEmpty()
    }
}
