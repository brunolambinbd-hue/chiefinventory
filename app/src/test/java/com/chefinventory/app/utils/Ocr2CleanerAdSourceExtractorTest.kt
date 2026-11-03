package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr2CleanerAdSourceExtractor].
 */
class Ocr2CleanerAdSourceExtractorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertThat(Ocr2CleanerAdSourceExtractor.toString()).contains("Ocr2CleanerAdSourceExtractor")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractSourceFromAdLine_AllBranches() {
        // 1. Valid brand in ad line
        val result = Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("Assiette disponible dans les Maxi GB", vocabulary)
        assertThat(result).isEqualTo("Maxi GB")

        // 2. Empty or blank input -> null
        assertThat(Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("", vocabulary)).isNull()
        assertThat(Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("   ", vocabulary)).isNull()

        // 3. Vocabulary with empty knownSourceBrands -> null
        val emptyBrandsVocab = vocabulary.copy(knownSourceBrands = emptyList())
        assertThat(Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("Assiette Carrefour", emptyBrandsVocab)).isNull()

        // 4. No matching brand in text -> null
        assertThat(Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("Assiette disponible dans les supermarchés", vocabulary)).isNull()

        // 5. Blank brand entry in vocabulary list -> skipped safely
        val vocabWithBlankBrand = vocabulary.copy(knownSourceBrands = listOf("   ", "Delhaize"))
        val resultDelhaize = Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("Recette proposée par Delhaize", vocabWithBlankBrand)
        assertThat(resultDelhaize).isEqualTo("Delhaize")
    }
}
