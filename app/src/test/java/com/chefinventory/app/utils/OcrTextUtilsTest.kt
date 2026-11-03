package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
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
    @CoveredRequirement("6.1.1.9")
    fun testIsInsideParentheses_PositionBeyondLength() {
        val text = "1 Oignon (poids 200g)"
        assertThat(OcrTextUtils.isInsideParentheses(text, 99)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsExcluded_BlankText() {
        assertThat(OcrTextUtils.isExcluded("   ", listOf("pub"))).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsLikelyProperNameOrSource_LowercaseAndUppercaseCases() {
        assertThat(OcrTextUtils.isLikelyProperNameOrSource("michel laroche", vocabulary)).isFalse()
        assertThat(OcrTextUtils.isLikelyProperNameOrSource("HÔTEL HILTON", vocabulary)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isInsideParentheses should return true when position is inside brackets`() {
        val text = "1 Oignon (poids 200g)"
        assertThat(OcrTextUtils.isInsideParentheses(text, 12)).isTrue()
        assertThat(OcrTextUtils.isInsideParentheses(text, 2)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isExcluded should match case-insensitive exact words`() {
        assertThat(OcrTextUtils.isExcluded("PAGE", listOf("page"))).isTrue()
        assertThat(OcrTextUtils.isExcluded("Ingrédient", listOf("page"))).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isLikelyProperNameOrSource should reject food titles in uppercase`() {
        assertThat(OcrTextUtils.isLikelyProperNameOrSource("RICOTTA ET ÉPINARDS", vocabulary)).isFalse()
        assertThat(OcrTextUtils.isLikelyProperNameOrSource("CANAPÉS GRECS", vocabulary)).isFalse()
        assertThat(OcrTextUtils.isLikelyProperNameOrSource("MICHEL LAROCHE", vocabulary)).isTrue()
    }
}
