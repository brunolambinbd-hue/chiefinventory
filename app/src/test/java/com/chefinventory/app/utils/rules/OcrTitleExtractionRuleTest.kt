package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.Ocr3CategorizerTitleExtractor
import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertWithMessage
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
        val result = Ocr3CategorizerTitleExtractor.extractTitle(input, baseVocabulary)

        assertWithMessage("Le titre 'Artichauts et vinaigrette aux échalotes' doit être extrait")
            .that(result.title)
            .ignoringCase()
            .contains("rtichauts et vinaigrette aux échalotes")
    }
}
