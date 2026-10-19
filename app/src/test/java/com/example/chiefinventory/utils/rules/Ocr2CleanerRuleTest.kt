package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.Ocr2Cleaner
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class Ocr2CleanerRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `clean should filter out empty or blank lines`() {
        val input = listOf("", "   ", "\t", "\n", "Sel")
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList())
        assertThat(result).containsExactly("Sel")
    }

    @Test
    fun `clean should filter out strict excluded keywords using production XML`() {
        val input = listOf("Page", "Saveur", "Oignon")
        val result = Ocr2Cleaner.clean(input, excludedKeywords = baseVocabulary.excludedOcrKeywords)
        assertThat(result).containsExactly("Oignon")
    }

    @Test
    fun `clean should filter out advertisements via regex`() {
        val adExclusions = listOf(".*promo.*", ".*pub.*")
        val input = listOf("Super promo ici", "100 g de beurre")
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList(), adExclusions = adExclusions)
        assertThat(result).containsExactly("100 g de beurre")
    }

    @Test
    fun `clean should filter out presentation plate marketing captions via production ads_exclusions`() {
        val adExclusions = listOf(".*Assiette de pr[eéèê]sentation.*", ".*disponible dans les.*")
        val input = listOf(
            "Assiette de présentation disponible dans les Maxi GB",
            "Assiette de presentation disponible dans les Maxi GB",
            "100 g de beurre"
        )
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList(), adExclusions = adExclusions)
        assertThat(result).containsExactly("100 g de beurre")
    }

    @Test
    fun `clean should strip marketing caption prefix from concatenated instruction line`() {
        val adExclusions = listOf("Assiette de pr[eéèê]sentation disponible dans les Maxi GB")
        val input = listOf(
            "Assiette de présentation disponible dans les Maxi GB Pendant ce temps, préparez la vinaigrette."
        )
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList(), adExclusions = adExclusions)
        assertThat(result).containsExactly("Pendant ce temps, préparez la vinaigrette.")
    }

    @Test
    fun `clean should filter out isolated digits up to 3 chars`() {
        val input = listOf("1", "12", "123", "2024", "Sel")
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList())
        assertThat(result).containsExactly("2024", "Sel").inOrder()
    }

    @Test
    fun `isTechnicalDimension should identify dimensions with connectors`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 mm sur 5 mm")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("10x10cm")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("100 cm2 x 50 cm2")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 mm")).isFalse()
        assertThat(Ocr2Cleaner.isTechnicalDimension("250 g")).isFalse()
    }
}
