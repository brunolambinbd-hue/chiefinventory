package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [OcrTitleExtractor0_Context].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrTitleExtractor0_ContextTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractFirstIngredientNameWord_DirectCall() {
        val stop = listOf("de", "du", "des", "d'", "la", "le", "les", "pour")

        // 1. Empty or blank name -> null
        assertThat(OcrTitleExtractor0_Context.extractFirstIngredientNameWord("", stop)).isNull()
        assertThat(OcrTitleExtractor0_Context.extractFirstIngredientNameWord("   ", stop)).isNull()

        // 2. Only stop words -> null
        assertThat(OcrTitleExtractor0_Context.extractFirstIngredientNameWord("de pour", stop)).isNull()

        // 3. Valid name -> returns first word
        assertThat(OcrTitleExtractor0_Context.extractFirstIngredientNameWord("farine fluide", stop)).isEqualTo("farine")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractRecipeIngredientWords_FullCoverage() {
        val lines = listOf(
            "   ",
            "INGRÉDIENTS",
            "200 g de farine pommes choux gousses",
            "3 cuillères de sucre"
        )
        val words = OcrTitleExtractor0_Context.extractRecipeIngredientWords(lines)
        assertThat(words).contains("farine")
        assertThat(words).contains("pomme")
        assertThat(words).contains("chou")
        assertThat(words).contains("sucre")
        assertThat(words).doesNotContain("gousses")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFindFirstIngredientWord_WithNullAndNonNullVocabulary() {
        val lines = listOf("   ", "200 g de farine")

        // 1. With non-null vocabulary
        val word1 = OcrTitleExtractor0_Context.findFirstIngredientWord(lines, vocabulary)
        assertThat(word1).isEqualTo("farine")

        // 2. With null vocabulary
        val word2 = OcrTitleExtractor0_Context.findFirstIngredientWord(lines, null)
        assertThat(word2).isEqualTo("farine")

        // 3. Overloaded method without second argument
        val word3 = OcrTitleExtractor0_Context.findFirstIngredientWord(lines)
        assertThat(word3).isEqualTo("farine")

        // 4. Vocabulary with empty standaloneArticles
        val emptyArticlesVocab = vocabulary.copy(standaloneArticles = emptyList())
        val word4 = OcrTitleExtractor0_Context.findFirstIngredientWord(lines, emptyArticlesVocab)
        assertThat(word4).isEqualTo("farine")

        // 5. Lines with blank parsed name ("200 g") and stopWords only ("3 g de pour")
        val wordBlank = OcrTitleExtractor0_Context.findFirstIngredientWord(listOf("200 g", "3 g de pour", "200 g de farine"), vocabulary)
        assertThat(wordBlank).isEqualTo("farine")

        // 6. With empty lines
        val word5 = OcrTitleExtractor0_Context.findFirstIngredientWord(listOf("   ", "Instructions"), vocabulary)
        assertThat(word5).isNull()
    }
}
