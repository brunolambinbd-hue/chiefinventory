package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [Ocr2Cleaner].
 */
class Ocr2CleanerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie la suppression des lignes de bruits publicitaires complets issues de [OcrVocabulary.adExclusions] (`ads_exclusions.xml`).
     */
    @Test
    fun `clean should remove full advertisement lines from ads_exclusions`() {
        val input = listOf(
            "1 avocat",
            "200 g de farine",
            "Assiette de présentation disponible dans les Maxi GB"
        )

        val result = Ocr2Cleaner.clean(input, vocabulary.excludedOcrKeywords, vocabulary.adExclusions)

        assertThat(result).containsExactly(
            "1 avocat",
            "200 g de farine"
        )
    }

    /**
     * Vérifie la suppression des mots-clés d'exclusion stricte et des numéros de pages isolés issues de [OcrVocabulary.excludedOcrKeywords].
     */
    @Test
    fun `clean should remove strict excluded keywords like page numbers`() {
        val input = listOf(
            "100 g de beurre",
            "45",
            "PAGE"
        )

        val result = Ocr2Cleaner.clean(input, vocabulary.excludedOcrKeywords, vocabulary.adExclusions)

        assertThat(result).containsExactly(
            "100 g de beurre"
        )
    }

    /**
     * Vérifie l'identification des dimensions techniques ("5 mm sur 5 mm") et leur distinction par rapport aux quantités d'ingrédients.
     */
    @Test
    fun `isTechnicalDimension should identify dimension lines vs ingredients`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 mm sur 5 mm")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("1 cm x 2 cm")).isTrue()

        assertThat(Ocr2Cleaner.isTechnicalDimension("200 g de farine")).isFalse()
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 cl d'huile")).isFalse()
    }
}
