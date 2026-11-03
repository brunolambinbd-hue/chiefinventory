package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerSourceCleaner].
 */
class Ocr3CategorizerSourceCleanerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertThat(Ocr3CategorizerSourceCleaner.toString()).contains("Ocr3CategorizerSourceCleaner")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanSourceLine_AllPrefixesAndPunctuation() {
        // 1. Phone prefixes cleaning
        assertThat(Ocr3CategorizerSourceCleaner.cleanSourceLine("Tel: 01 23 45 67 89", vocabulary)).isEqualTo("01 23 45 67 89")
        assertThat(Ocr3CategorizerSourceCleaner.cleanSourceLine("Tél. 02 54 88 99", vocabulary)).isEqualTo("02 54 88 99")
        assertThat(Ocr3CategorizerSourceCleaner.cleanSourceLine("Fax 04 55", vocabulary)).isEqualTo("04 55")
        assertThat(Ocr3CategorizerSourceCleaner.cleanSourceLine("GSM: 0612345678", vocabulary)).isEqualTo("0612345678")

        // 2. Overload with phonePrefixes list directly
        assertThat(Ocr3CategorizerSourceCleaner.cleanSourceLine("Tel: 01 23 45 67 89", listOf("Tel"))).isEqualTo("01 23 45 67 89")

        // 3. Leading punctuation cleaning
        assertThat(Ocr3CategorizerSourceCleaner.cleanSourceLine(": : 02/542.42.42", vocabulary)).isEqualTo("02/542.42.42")
    }
}
