package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la conversion des caractères diacritiques parasites
 * issus du dictionnaire XML [diacritic_repairs.xml] dans [Ocr1Normalizer].
 */
class OcrDiacriticRepairsTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `normalize should convert vietnamese and eastern european ocr diacritic accents from XML`() {
        val input = "200 g d'ếpinards frais, 1/2 cuillệre à café"
        val normalized = Ocr1Normalizer.normalize(input, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs)

        assertThat(normalized).contains("d'épinards")
        assertThat(normalized).contains("cuillère")
    }

    @Test
    fun `vocabulary should load ocrDiacriticRepairs from diacritic_repairs XML`() {
        assertThat(vocabulary.ocrDiacriticRepairs).contains("ế|é")
        assertThat(vocabulary.ocrDiacriticRepairs).contains("ề|è")
        assertThat(vocabulary.ocrDiacriticRepairs).contains("š|s")
        assertThat(vocabulary.ocrDiacriticRepairs).contains("č|c")
    }
}
