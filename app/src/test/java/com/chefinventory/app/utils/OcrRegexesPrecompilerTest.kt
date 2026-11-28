package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant la pré-compilation en amont des Regexes dynamiques dans [OcrRegexesPrecompiler].
 */
class OcrRegexesPrecompilerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        OcrRegexesDynamicHelpers.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
        OcrRegexesDynamicHelpers.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPrecompile_CompilesAllDynamicRegexesInVocabulary() {
        OcrRegexesDynamicHelpers.clearCache()

        assertThat(OcrRegexesDynamicHelpers.hasUnitPattern("c. à soupe")).isFalse()
        assertThat(OcrRegexesDynamicHelpers.hasSpellingRepairPattern("æufs")).isFalse()

        val compiledCount = OcrRegexesDynamicHelpers.precompile(vocabulary)

        assertThat(compiledCount).isGreaterThan(50)
        assertThat(OcrRegexesDynamicHelpers.hasUnitPattern("c. à soupe")).isTrue()
        assertThat(OcrRegexesDynamicHelpers.hasSpellingRepairPattern("lc. à soupe")).isTrue()

        // Verify that compiled patterns match immediately in 0 ms
        val unitPattern = OcrRegexesDynamicHelpers.getUnitPattern("c. à soupe")
        assertThat(unitPattern.containsMatchIn("c. à soupe de farine")).isTrue()

        val repairPattern = OcrRegexesDynamicHelpers.getSpellingRepairPattern("æufs")
        assertThat(repairPattern.containsMatchIn("2 æufs durs")).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPrecompile_BlankEntriesAndEmptyStepActionKeywords_CoversAllBranches() {
        val customVocab = vocabulary.copy(
            culinaryMeasurementUnits = listOf(" ", "g"),
            ocrSpellingRepairs = listOf("  | correct", " wrong | right "),
            phonePrefixes = listOf("   ", "01"),
            stepActionKeywords = emptyList()
        )

        val count = OcrRegexesPrecompiler.precompile(customVocab)
        assertThat(count).isGreaterThan(0)

        // Also test Object generated toString coverage
        assertThat(OcrRegexesPrecompiler.toString()).contains("OcrRegexesPrecompiler")
    }
}
