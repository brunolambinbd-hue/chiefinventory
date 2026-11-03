package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.Ocr2_5CleanerTitleExtractor
import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
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

        val result = Ocr2_5CleanerTitleExtractor.extractTitle(input, vocabulary)

        assertThat(result.title).ignoringCase().contains("alade d'avocat et princesses")
        assertThat(result.title).ignoringCase().doesNotContain("citron, le")
    }
}
