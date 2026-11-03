package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [RecipeMetadataParser2_BlockB_Price].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeMetadataParser2_BlockB_PriceTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessPriceBlock_LowMediumAndHigh() {
        val resultsLow = RawSections()
        RecipeMetadataParser2_BlockB_Price.processPriceBlock("Prix : bon marché", vocabulary, resultsLow)
        assertThat(resultsLow.detectedPriceRating).isEqualTo("Bon marché")

        val resultsMed = RawSections()
        RecipeMetadataParser2_BlockB_Price.processPriceBlock("Coût moyen", vocabulary, resultsMed)
        assertThat(resultsMed.detectedPriceRating).isEqualTo("Coût moyen")

        val resultsHigh = RawSections()
        RecipeMetadataParser2_BlockB_Price.processPriceBlock("Assez cher", vocabulary, resultsHigh)
        assertThat(resultsHigh.detectedPriceRating).isEqualTo("Assez cher")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessPriceBlock_WithNullAndEmptyCategoryVocabulary() {
        // 1. With null vocabulary
        val resultsNull = RawSections()
        RecipeMetadataParser2_BlockB_Price.processPriceBlock("Prix : bon marché", null, resultsNull)
        assertThat(resultsNull.detectedPriceRating).isEqualTo("Bon marché")

        // 2. Vocabulary with empty price lists
        val emptyPriceVocab = vocabulary.copy(
            priceCategoriesLow = emptyList(),
            priceCategoriesMedium = emptyList(),
            priceCategoriesHigh = emptyList(),
            priceTitleKeywords = emptyList()
        )
        val resultsEmptyVocab = RawSections()
        RecipeMetadataParser2_BlockB_Price.processPriceBlock("Prix : bon marché", emptyPriceVocab, resultsEmptyVocab)
        assertThat(resultsEmptyVocab.detectedPriceRating).isEqualTo("Bon marché")

        // 3. Price rating already set
        val resultsAlreadySet = RawSections()
        resultsAlreadySet.detectedPriceRating = "Bon marché"
        RecipeMetadataParser2_BlockB_Price.processPriceBlock("Prix : bon marché", vocabulary, resultsAlreadySet)
        assertThat(resultsAlreadySet.detectedPriceRating).isEqualTo("Bon marché")
    }
}
