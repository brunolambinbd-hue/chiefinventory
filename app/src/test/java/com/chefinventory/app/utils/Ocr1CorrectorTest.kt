package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires isolés pour l'Étape 1 du pipeline OCR ([Ocr1Corrector]).
 */
class Ocr1CorrectorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun testNormalize_StandardFlowsAndSpellingRepairs() {
        // Blank input
        assertThat(Ocr1Corrector.correct("")).isEmpty()

        // Fraction rules and number corrections
        assertThat(Ocr1Corrector.correct("1/2 l de lait")).isEqualTo("1/2 l de lait")

        // Spelling repairs from dictionary
        val result1 = Ocr1Corrector.correct("poisson au goit de crabe", vocabulary.ocrSpellingRepairs)
        assertThat(result1).isEqualTo("poisson au goût de crabe")

        val result2 = Ocr1Corrector.correct("FONDUE ÀLA BIERE", vocabulary.ocrSpellingRepairs)
        assertThat(result2).isEqualTo("FONDUE À LA BIERE")

        // Test applySpellingRepairs with invalid formats and non-letter text
        assertThat(Ocr1Corrector.applySpellingRepairs("12345", listOf("wrong|correct"))).isEqualTo("12345")
        assertThat(Ocr1Corrector.applySpellingRepairs("test", listOf("invalid_no_pipe", "wrong|correct"))).isEqualTo("test")
    }

    @Test
    fun testNormalize_DiacriticRepairsAndEmptyListAndInvalidEntries() {
        val diacritics = vocabulary.ocrDiacriticRepairs
        assertThat(Ocr1Corrector.correct("ế", diacriticRepairs = diacritics)).isEqualTo("é")
        assertThat(Ocr1Corrector.correct("ề", diacriticRepairs = diacritics)).isEqualTo("è")
        assertThat(Ocr1Corrector.correct("ấ", diacriticRepairs = diacritics)).isEqualTo("â")
        assertThat(Ocr1Corrector.correct("ầ", diacriticRepairs = diacritics)).isEqualTo("à")
        assertThat(Ocr1Corrector.correct("ặ", diacriticRepairs = diacritics)).isEqualTo("a")

        // Empty diacritic repairs fallback
        assertThat(Ocr1Corrector.applyDiacriticRepairs("test", emptyList())).isEqualTo("test")

        // Invalid diacritic entries without pipe
        val withInvalidDiacritic = Ocr1Corrector.correct("ế", diacriticRepairs = listOf("invalid_no_pipe"))
        assertThat(withInvalidDiacritic).isEqualTo("ế")
    }

    @Test
    fun testNormalize_TrailingPunctuationRemoval() {
        assertThat(Ocr1Corrector.correct("500 g de farine,")).isEqualTo("500 g de farine")
        assertThat(Ocr1Corrector.correct("200 ml d'eau:")).isEqualTo("200 ml d'eau")
        assertThat(Ocr1Corrector.correct("1 pincée de sel;")).isEqualTo("1 pincée de sel")

        // Trailing hyphen should be preserved for hyphenated multi-line instructions
        assertThat(Ocr1Corrector.correct("Hachez finement le surimi ou-")).isEqualTo("Hachez finement le surimi ou-")
    }
}
