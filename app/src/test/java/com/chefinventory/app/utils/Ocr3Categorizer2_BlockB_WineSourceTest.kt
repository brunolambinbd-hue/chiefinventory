package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer2_BlockB_WineSource].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3Categorizer2_BlockB_WineSourceTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBlockBResultDataClassGeneratedMethods() {
        val res1 = BlockBResult(shouldContinue = true, workingLine = "1 cuillère à soupe")
        val res2 = res1.copy()
        val res3 = res1.copy(shouldContinue = false)

        assertThat(res1).isEqualTo(res2)
        assertThat(res1).isNotEqualTo(res3)
        assertThat(res1.hashCode()).isEqualTo(res2.hashCode())
        assertThat(res1.toString()).contains("BlockBResult")

        val (cont, line) = res1
        assertThat(cont).isTrue()
        assertThat(line).isEqualTo("1 cuillère à soupe")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockB_UnfinishedWineAndExcludedKeywords_WineAndSourceAndPurge() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()
        val wineState = Ocr3CategorizerWine.WineState()

        // 1. Unfinished quantity line ("1 cuillère à soupe" + "de moutarde")
        results.rawIngredientsList.add("1 cuillère à soupe")
        val resUnfinished = Ocr3Categorizer2_BlockB_WineSource.processBlockB_UnfinishedWineAndExcludedKeywords(
            rawWorkingLine = "de moutarde",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = true,
            isIngredientListClosed = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords,
            wineState = wineState
        )
        assertThat(resUnfinished.shouldContinue).isTrue()
        assertThat(results.rawIngredientsList.last()).isEqualTo("1 cuillère à soupe de moutarde")

        // 2. Source line ("Tel: 01 45 67 89 10")
        val resSource = Ocr3Categorizer2_BlockB_WineSource.processBlockB_UnfinishedWineAndExcludedKeywords(
            rawWorkingLine = "Tel: 01 45 67 89 10",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            isIngredientListClosed = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords,
            wineState = wineState
        )
        assertThat(resSource.shouldContinue).isTrue()
        assertThat(results.detectedSourceList).contains("01 45 67 89 10")

        // 3. Excluded keywords purge ("200 g de farine pub") -> "200 g de farine"
        val resPurge = Ocr3Categorizer2_BlockB_WineSource.processBlockB_UnfinishedWineAndExcludedKeywords(
            rawWorkingLine = "200 g de farine",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            isIngredientListClosed = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords,
            wineState = wineState
        )
        assertThat(resPurge.shouldContinue).isFalse()
        assertThat(resPurge.workingLine).isEqualTo("200 g de farine")
    }
}
