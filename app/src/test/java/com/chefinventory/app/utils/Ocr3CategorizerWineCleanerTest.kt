package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerWineCleaner].
 */
class Ocr3CategorizerWineCleanerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertThat(Ocr3CategorizerWineCleaner.toString()).contains("Ocr3CategorizerWineCleaner")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasWineVolume_AllCases() {
        assertThat(Ocr3CategorizerWineCleaner.hasWineVolume("Bouteille de 75 cl")).isTrue()
        assertThat(Ocr3CategorizerWineCleaner.hasWineVolume("75cl")).isTrue()
        assertThat(Ocr3CategorizerWineCleaner.hasWineVolume("37.5 cl")).isTrue()
        assertThat(Ocr3CategorizerWineCleaner.hasWineVolume("1.5 l")).isTrue()
        assertThat(Ocr3CategorizerWineCleaner.hasWineVolume("Farine")).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanWineLine_HeaderRemovalAndSpellingCorrections() {
        val input = "Notre vin conseillé : Un Bordeau 1996 75 cl"
        val cleaned = Ocr3CategorizerWineCleaner.cleanWineLine(input, vocabulary)
        assertThat(cleaned).isEqualTo("Un Bordeaux 1996 75 cl")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanWineLine_WithEmptyRemovePatternInResources() {
        val emptyRemovePatternRes = Ocr3CategorizerWine.WineResources(
            appellations = vocabulary.wineAppellations,
            producers = vocabulary.wineProducers,
            keywords = vocabulary.wineKeywords,
            titleKeywords = vocabulary.wineTitleKeywords,
            removePattern = ""
        )
        val cleaned = Ocr3CategorizerWineCleaner.cleanWineLine("Un Bordeau 1996", emptyRemovePatternRes)
        assertThat(cleaned).isEqualTo("Un Bordeaux 1996")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testApplySpellingCorrections_DirectCall() {
        val corrected = Ocr3CategorizerWineCleaner.applySpellingCorrections("Un Bordeau Atinum")
        assertThat(corrected).contains("Bordeaux")
    }
}
