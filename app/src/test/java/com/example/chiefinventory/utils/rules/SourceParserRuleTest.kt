package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.SourceParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class SourceParserRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `isSourceLine should detect phone numbers`() {
        assertThat(SourceParser.isSourceLine("Tel: 01 23 45 67 89", baseVocabulary)).isTrue()
        assertThat(SourceParser.isSourceLine("+32 2 542 42 42", baseVocabulary)).isTrue()
    }

    @Test
    fun `isSourceLine should detect source keywords from production XML`() {
        assertThat(SourceParser.isSourceLine("Restaurant La Table", baseVocabulary)).isTrue()
        assertThat(SourceParser.isSourceLine("12 rue des Alpes", baseVocabulary)).isTrue()
    }

    @Test
    fun `isSourceLine should detect custom synthetic source keyword complement`() {
        val customVocabulary = baseVocabulary.copy(
            sourceKeywords = baseVocabulary.sourceKeywords + "bistro_test"
        )
        assertThat(SourceParser.isSourceLine("Visite au bistro_test", customVocabulary)).isTrue()
    }

    @Test
    fun `isSourceLine should ignore plain instruction text`() {
        assertThat(SourceParser.isSourceLine("Couper les oignons", baseVocabulary)).isFalse()
    }

    @Test
    fun `cleanSourceLine should strip phone prefixes using negative lookbehind`() {
        assertThat(SourceParser.cleanSourceLine("Tel: 01 23 45 67 89", baseVocabulary)).isEqualTo("01 23 45 67 89")
        assertThat(SourceParser.cleanSourceLine("Hôtel 012345", baseVocabulary)).isEqualTo("Hôtel 012345")
        assertThat(SourceParser.cleanSourceLine("Tél. : 02/542.42.42", baseVocabulary)).isEqualTo("02/542.42.42")
    }

    @Test
    fun `pipeline should preserve early source Maxi GB from stripped ad line`() {
        val ocrScanWithAd = """
            INGRÉDIENTS
            200 g de farine
            100 g de beurre
            INSTRUCTIONS
            Mélangez la farine et le beurre.
            Assiette de présentation disponible dans les Maxi GB
        """.trimIndent()

        val result = com.example.chiefinventory.utils.RecipeOcrParser.parse(ocrScanWithAd, baseVocabulary)

        assertThat(result.source).isEqualTo("Maxi GB")
        assertThat(result.instructions).contains("Mélangez la farine et le beurre.")
        assertThat(result.instructions).doesNotContain("Assiette de présentation")
    }
}
