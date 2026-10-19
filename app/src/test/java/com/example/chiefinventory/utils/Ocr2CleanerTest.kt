package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class Ocr2CleanerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `clean with empty input list`() {
        val result = Ocr2Cleaner.clean(emptyList(), vocabulary.excludedOcrKeywords)
        assertThat(result).isEmpty()
    }

    @Test
    fun `clean handles whitespace only lines`() {
        val input = listOf(" ", "\t", "\n", "  \t  ")
        val result = Ocr2Cleaner.clean(input, vocabulary.excludedOcrKeywords)
        assertThat(result).isEmpty()
    }

    @Test
    fun `clean ad exclusion via valid regex`() {
        val adExclusions = listOf(".*promo.*")
        val input = listOf("Super promo ici", "Ingrédient normal")
        val result = Ocr2Cleaner.clean(input, emptyList(), adExclusions)
        assertThat(result).containsExactly("Ingrédient normal")
    }

    @Test
    fun `clean ad exclusion via invalid regex fallback`() {
        val adExclusions = listOf("[invalid-regex")
        val input = listOf("Ceci contient [invalid-regex", "Ligne normale")
        val result = Ocr2Cleaner.clean(input, emptyList(), adExclusions)
        assertThat(result).containsExactly("Ligne normale")
    }

    @Test
    fun `clean keyword exclusion strict match`() {
        val excluded = listOf("PAGE", "SAVEUR")
        val input = listOf("Page", "Saveur", "Oignon")
        val result = Ocr2Cleaner.clean(input, excluded)
        assertThat(result).containsExactly("Oignon")
    }

    @Test
    fun `clean keyword exclusion partial match retention`() {
        val excluded = listOf("PAGE")
        val input = listOf("PAGE_NUMBER", "PAGE")
        val result = Ocr2Cleaner.clean(input, excluded)
        assertThat(result).containsExactly("PAGE_NUMBER")
    }

    @Test
    fun `clean numeric noise removal 1 to 3 digits`() {
        val input = listOf("1", "12", "123", "Oignon")
        val result = Ocr2Cleaner.clean(input, emptyList())
        assertThat(result).containsExactly("Oignon")
    }

    @Test
    fun `clean numeric noise retention 4 digits`() {
        val input = listOf("2024", "123")
        val result = Ocr2Cleaner.clean(input, emptyList())
        assertThat(result).containsExactly("2024")
    }

    @Test
    fun `clean alpha numeric noise retention`() {
        val input = listOf("5g", "12a", "12")
        val result = Ocr2Cleaner.clean(input, emptyList())
        assertThat(result).containsExactly("5g", "12a").inOrder()
    }

    @Test
    fun `isTechnicalDimension valid x connector`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 mm x 5 mm")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("10x10cm")).isTrue()
    }

    @Test
    fun `isTechnicalDimension valid sur connector`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 mm sur 5 mm")).isTrue()
    }

    @Test
    fun `isTechnicalDimension case insensitive connectors`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 MM SUR 5 MM")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("10 X 10 CM")).isTrue()
    }

    @Test
    fun `isTechnicalDimension area units detection`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("100 cm2 x 50 cm2")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("10 mm2 sur 10 mm2")).isTrue()
    }

    @Test
    fun `isTechnicalDimension dimension without connector`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 mm")).isFalse()
        assertThat(Ocr2Cleaner.isTechnicalDimension("250 g")).isFalse()
    }

    @Test
    fun `isTechnicalDimension connector without dimension unit`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 kg x 5 kg")).isFalse()
    }

    @Test
    fun `isTechnicalDimension boundary match check`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("100 mms x 100 mms")).isFalse()
        assertThat(Ocr2Cleaner.isTechnicalDimension("100 mm x 100 mm")).isTrue()
    }
}
