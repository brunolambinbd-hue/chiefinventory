package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer5_BlockE_Dispatch].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3Categorizer5_BlockE_DispatchTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsEndMarkerLine_AllVariants() {
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("sel et poivre")).isTrue()
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("sel, poivre")).isTrue()
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("sel")).isTrue()
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("poivre")).isTrue()
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.isEndMarkerLine("farine")).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateShouldPullBack_AllBranches() {
        // 1. isReappearance = true -> false
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
            isReappearance = true, isHyphenatedInstruction = false, isEndMarkerLine = false,
            containsAction = false, closedState = false, isStrictWithQty = true, isCommonWithNoAction = true
        )).isFalse()

        // 2. isHyphenatedInstruction = true -> false
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
            isReappearance = false, isHyphenatedInstruction = true, isEndMarkerLine = false,
            containsAction = false, closedState = false, isStrictWithQty = true, isCommonWithNoAction = true
        )).isFalse()

        // 3. isEndMarkerLine && !containsAction -> true
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
            isReappearance = false, isHyphenatedInstruction = false, isEndMarkerLine = true,
            containsAction = false, closedState = false, isStrictWithQty = false, isCommonWithNoAction = false
        )).isTrue()

        // 4. closedState = true with isCommonWithNoAction = true
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
            isReappearance = false, isHyphenatedInstruction = false, isEndMarkerLine = false,
            containsAction = false, closedState = true, isStrictWithQty = false, isCommonWithNoAction = true
        )).isTrue()

        // 5. closedState = false with isStrictWithQty = true
        assertThat(Ocr3Categorizer5_BlockE_Dispatch.evaluateShouldPullBack(
            isReappearance = false, isHyphenatedInstruction = false, isEndMarkerLine = false,
            containsAction = false, closedState = false, isStrictWithQty = true, isCommonWithNoAction = false
        )).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SectionDispatch_IngredientsAndInstructionsAndFallback() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. Dispatch INGREDIENTS section ("200 g de farine")
        val closed1 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "200 g de farine",
            lowerLine = "200 g de farine",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closed1).isFalse()
        assertThat(results.rawIngredientsList).contains("200 g de farine")

        // 2. Dispatch INGREDIENTS section end-marker ("sel et poivre")
        val closed2 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "sel et poivre",
            lowerLine = "sel et poivre",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closed2).isTrue()

        // 3. Dispatch INGREDIENTS section with containsAction = true
        val closedActionIngr = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "couper les oignons",
            lowerLine = "couper les oignons",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedActionIngr).isFalse()

        // 4. Dispatch INSTRUCTIONS section ("Faire cuire au four")
        val closed3 = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Faire cuire au four",
            lowerLine = "faire cuire au four",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = true,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closed3).isTrue()
        assertThat(results.rawInstructionsList).contains("Faire cuire au four")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SectionDispatch_TechnicalDimensionHyphenatedInstructionAndFallback() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. Technical dimension in SECTION_INGREDIENTS ("5 cm x 10 cm")
        val closedTech = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "5 cm x 10 cm",
            lowerLine = "5 cm x 10 cm",
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedTech).isFalse()

        // 2. Hyphenated instruction in SECTION_INSTRUCTIONS ("verser -")
        val closedHyphen = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "verser -",
            lowerLine = "verser -",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = true,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedHyphen).isFalse()
        assertThat(results.rawInstructionsList).contains("verser -")

        // 3. Technical dimension in SECTION_INSTRUCTIONS ("5 cm x 10 cm")
        val closedTechInInstructions = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "5 cm x 10 cm",
            lowerLine = "5 cm x 10 cm",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = true,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedTechInInstructions).isFalse()

        // 4. Fallback SECTION_NONE looksLikeIngredient = true
        val closedNoneIngr = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "100 g de sucre",
            lowerLine = "100 g de sucre",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = true,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedNoneIngr).isFalse()
        assertThat(results.rawIngredientsList).contains("100 g de sucre")

        // 5. Fallback SECTION_NONE startsWithConnector = true
        val closedConnectorNone = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "de farine",
            lowerLine = "de farine",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = true,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedConnectorNone).isFalse()

        // 6. Fallback SECTION_NONE looksLikeIngredient = false
        val closedNoneInstr = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "Remuer énergiquement",
            lowerLine = "remuer énergiquement",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedNoneInstr).isFalse()
        assertThat(results.rawInstructionsList).contains("Remuer énergiquement")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SectionDispatch_CommonIngredientWithActionInInstructions() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // Common ingredient with containsAction = true in SECTION_INSTRUCTIONS ("couper le beurre")
        val closedCommonAction = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "couper le beurre",
            lowerLine = "couper le beurre",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = true,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedCommonAction).isTrue()
        assertThat(results.rawInstructionsList).contains("couper le beurre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlockE_SectionDispatch_YellowLinesYellowToGreen() {
        val results = RawSections()
        val seenKeywords = mutableSetOf<String>()

        // 1. hasInstructionTrigger = true with looksLikeIngredient = true in SECTION_INSTRUCTIONS ("pour la farine")
        val closedTrigger = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "pour la farine",
            lowerLine = "pour la farine",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = false,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = true,
            isReappearance = false,
            hasInstructionTrigger = true,
            looksLikeIngredient = true,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedTrigger).isFalse()

        // 2. startsWithBullet = true with isCommonIngredient = true in SECTION_INSTRUCTIONS ("• le beurre")
        val closedBulletBeurre = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
            workingLine = "• le beurre",
            lowerLine = "• le beurre",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithBullet = true,
            startsWithConnector = false,
            hasWeight = false,
            hasHardQuantity = false,
            isReappearance = false,
            hasInstructionTrigger = false,
            looksLikeIngredient = false,
            vocabulary = vocabulary,
            results = results,
            seenIngredientKeywords = seenKeywords
        )
        assertThat(closedBulletBeurre).isFalse()
    }
}
