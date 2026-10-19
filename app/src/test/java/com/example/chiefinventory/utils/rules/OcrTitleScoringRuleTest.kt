package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrTitleExtractor
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrTitleScoringRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `extractTitle should reject candidate ending with trailing article le and prefer avocat et princesses`() {
        val input = listOf(
            "1 avocat",
            "200 g de haricots princesses",
            "L'huile d'olive, le jus de citron, le",
            "sel et poivre alade d'avocat et princesses"
        )

        val result = OcrTitleExtractor.extractTitle(input, vocabulary)

        assertThat(result.title).ignoringCase().contains("alade d'avocat et princesses")
        assertThat(result.title).ignoringCase().doesNotContain("citron, le")
    }
}
