package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de [OcrTextUtils].
 */
class OcrTextUtilsTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `isInsideParentheses should return true when position is inside brackets`() {
        val text = "1 Oignon (poids 200g)"
        assertThat(OcrTextUtils.isInsideParentheses(text, 12)).isTrue()
        assertThat(OcrTextUtils.isInsideParentheses(text, 2)).isFalse()
    }

    @Test
    fun `isExcluded should match case-insensitive exact words`() {
        assertThat(OcrTextUtils.isExcluded("PAGE", listOf("page"))).isTrue()
        assertThat(OcrTextUtils.isExcluded("Ingrédient", listOf("page"))).isFalse()
    }

    @Test
    fun `isLikelyProperNameOrSource should reject food titles in uppercase`() {
        assertThat(OcrTextUtils.isLikelyProperNameOrSource("RICOTTA ET ÉPINARDS", vocabulary)).isFalse()
        assertThat(OcrTextUtils.isLikelyProperNameOrSource("CANAPÉS GRECS", vocabulary)).isFalse()
        assertThat(OcrTextUtils.isLikelyProperNameOrSource("MICHEL LAROCHE", vocabulary)).isTrue()
    }
}
