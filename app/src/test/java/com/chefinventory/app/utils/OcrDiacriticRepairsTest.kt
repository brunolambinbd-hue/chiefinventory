package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires isolés pour les réparations d'accents et diacritiques parasites Unicode (`ocr_step1_diacritic_repairs.xml`).
 */
class OcrDiacriticRepairsTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `defaultDiacriticRepairs should convert accented characters correctly`() {
        val diacritics = vocabulary.ocrDiacriticRepairs
        assertThat(Ocr1Corrector.correct("ế", diacriticRepairs = diacritics)).isEqualTo("é")
        assertThat(Ocr1Corrector.correct("ề", diacriticRepairs = diacritics)).isEqualTo("è")
        assertThat(Ocr1Corrector.correct("ấ", diacriticRepairs = diacritics)).isEqualTo("â")
        assertThat(Ocr1Corrector.correct("ầ", diacriticRepairs = diacritics)).isEqualTo("à")
        assertThat(Ocr1Corrector.correct("ặ", diacriticRepairs = diacritics)).isEqualTo("a")
    }

    @Test
    fun `applyDiacriticRepairs should fallback gracefully on empty list or invalid format`() {
        assertThat(Ocr1Corrector.applyDiacriticRepairs("test", emptyList())).isEqualTo("test")
        val withInvalidDiacritic = Ocr1Corrector.correct("ế", diacriticRepairs = listOf("invalid_no_pipe"))
        assertThat(withInvalidDiacritic).isEqualTo("ế")
    }

    @Test
    fun testNormalize_EmptyHeaderListsAndHeaderMatchNoChange() {
        val emptyVocab = OcrVocabulary()
        val result = Ocr1Corrector.correct("ingrédients", diacriticRepairs = emptyVocab.ocrDiacriticRepairs)
        assertThat(result).isEqualTo("ingrédients")
    }
}
