package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrTitleExtractor
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrTitleExtractionRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `extractTitle should extract title for scenario artichauts`() {
        val input = listOf("4 artichauts", "o/rtichauts et vinaigrette aux échalotes")
        val result = OcrTitleExtractor.extractTitle(input, baseVocabulary)

        assertThat(result.title).ignoringCase().contains("rtichauts et vinaigrette aux échalotes")
    }
}
