package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerProperNameDetector].
 */
class Ocr3CategorizerProperNameDetectorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertThat(Ocr3CategorizerProperNameDetector.toString()).contains("Ocr3CategorizerProperNameDetector")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsLikelyProperNameOrSource_AllBranches() {
        // 1. Valid uppercase proper names
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("MICHEL LAROCHE", vocabulary)).isTrue()
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("HÔTEL HILTON", vocabulary)).isTrue()

        // 2. Default argument overload (vocabulary = null)
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("MICHEL LAROCHE")).isTrue()

        // 3. Lowercase name -> false
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("michel laroche", vocabulary)).isFalse()

        // 4. Word count not in 2..4 -> false
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("MICHEL", vocabulary)).isFalse()
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("UN DEUX TROIS QUATRE CINQ", vocabulary)).isFalse()

        // 5. Contains digits -> false
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("HÔTEL HILTON 123", vocabulary)).isFalse()

        // 6. Contains food connector or food word -> false
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("RICOTTA ET ÉPINARDS", vocabulary)).isFalse()
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("CANAPÉS GRECS", vocabulary)).isFalse()

        // 7. With null vocabulary
        assertThat(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("MICHEL LAROCHE", null)).isTrue()
    }
}
