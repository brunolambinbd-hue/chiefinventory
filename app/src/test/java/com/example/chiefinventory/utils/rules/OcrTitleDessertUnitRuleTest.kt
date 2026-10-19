package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrTitleExtractor
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrTitleDessertUnitRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `extractTitle should reject candidate starting with dessert spoon unit like C a dessert de rhum`() {
        val input = listOf("1 c. à dessert de rhum", "C. à dessert de rhum")
        val result = OcrTitleExtractor.extractTitle(input, baseVocabulary)

        assertThat(result.title).isNull()
    }
}
