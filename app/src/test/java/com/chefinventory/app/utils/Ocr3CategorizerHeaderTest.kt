package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la détection des en-têtes de section
 * et le basculement d'état par [Ocr3CategorizerHeader].
 */
class Ocr3CategorizerHeaderTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessSectionHeader_ExhaustiveLengthAndActionGuards() {
        val state = Ocr3CategorizerHeader.HeaderState()

        // 1. Ingredients header too long (length >= 35) -> returns false
        val longIngr = "Ingrédients pour 4 personnes et leurs accompagnements de saison"
        val isLongIngr = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = longIngr,
            lowerLine = longIngr.lowercase(),
            containsAction = false,
            vocabulary = vocabulary,
            state = state,
            seenIngredientKeywords = mutableSetOf()
        )
        assertThat(isLongIngr).isFalse()

        // 2. Ingredients header containing action verb -> returns false
        val actionIngr = "Ingrédients : épluchez les légumes"
        val isActionIngr = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = actionIngr,
            lowerLine = actionIngr.lowercase(),
            containsAction = true,
            vocabulary = vocabulary,
            state = state,
            seenIngredientKeywords = mutableSetOf()
        )
        assertThat(isActionIngr).isFalse()

        // 3. Instruction header too long (length >= 40) -> returns false
        val longInstr = "Préparation de la pâte à tarte aux pommes de terre et aux légumes d'été"
        val isLongInstr = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = longInstr,
            lowerLine = longInstr.lowercase(),
            containsAction = false,
            vocabulary = vocabulary,
            state = state,
            seenIngredientKeywords = mutableSetOf()
        )
        assertThat(isLongInstr).isFalse()

        // 4. Instruction header containing action verb -> returns false
        val actionInstr = "Préparation de la sauce en mélangeant"
        val isActionInstr = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = actionInstr,
            lowerLine = actionInstr.lowercase(),
            containsAction = true,
            vocabulary = vocabulary,
            state = state,
            seenIngredientKeywords = mutableSetOf()
        )
        assertThat(isActionInstr).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessSectionHeader_DetectsIngredientsHeaderAndUpdateState() {
        val state = Ocr3CategorizerHeader.HeaderState()
        val seenKeywords = mutableSetOf("farine", "sucre")

        val isHeader = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = "ingrédients pour 4 personnes",
            lowerLine = "ingrédients pour 4 personnes",
            containsAction = false,
            vocabulary = vocabulary,
            state = state,
            seenIngredientKeywords = seenKeywords
        )

        assertThat(isHeader).isTrue()
        assertThat(state.currentSection).isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
        assertThat(state.isIngredientListClosed).isFalse()
        assertThat(seenKeywords).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessSectionHeader_DetectsPreparationHeaderAndSwitchToInstructions() {
        val state = Ocr3CategorizerHeader.HeaderState()
        val seenKeywords = mutableSetOf<String>()

        val isHeader = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = "Préparation :",
            lowerLine = "préparation :",
            containsAction = false,
            vocabulary = vocabulary,
            state = state,
            seenIngredientKeywords = seenKeywords
        )

        assertThat(isHeader).isTrue()
        assertThat(state.currentSection).isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessSectionHeader_ExhaustiveVariantsOfHeaders() {
        val instructionVariants = listOf(
            "préparation",
            "préparation :",
            "préparation:",
            "préparation de la pâte",
            "préparation de la sauce",
            "temps de préparation : 15 min",
            "temps de préparation",
            "preparation :",
            "instructions :",
            "consignes :",
            "étapes :",
            "méthode de la préparation :",
            "méthode de cuisson",
            "consignes d'assemblage",
            "déroulement :"
        )
        instructionVariants.forEach { headerText ->
            val state = Ocr3CategorizerHeader.HeaderState()
            val isHeader = Ocr3CategorizerHeader.processSectionHeader(
                workingLine = headerText,
                lowerLine = headerText.lowercase(),
                containsAction = false,
                vocabulary = vocabulary,
                state = state,
                seenIngredientKeywords = mutableSetOf()
            )
            assertThat(isHeader).isTrue()
            assertThat(state.currentSection).isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)
        }

        val ingredientVariants = listOf(
            "ingrédients pour 4 personnes",
            "ingredients :",
            "composition :",
            "ngrédients :"
        )
        ingredientVariants.forEach { headerText ->
            val state = Ocr3CategorizerHeader.HeaderState()
            val isHeader = Ocr3CategorizerHeader.processSectionHeader(
                workingLine = headerText,
                lowerLine = headerText.lowercase(),
                containsAction = false,
                vocabulary = vocabulary,
                state = state,
                seenIngredientKeywords = mutableSetOf()
            )
            assertThat(isHeader).isTrue()
            assertThat(state.currentSection).isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessSectionHeader_SubHeaderAndNonHeaderLines() {
        val vocabSub = OcrVocabulary(ingredientSubHeaders = listOf("pour la sauce"))
        val stateSub = Ocr3CategorizerHeader.HeaderState()

        val isSubHeader = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = "Pour la sauce :",
            lowerLine = "pour la sauce :",
            containsAction = false,
            vocabulary = vocabSub,
            state = stateSub,
            seenIngredientKeywords = mutableSetOf()
        )

        assertThat(isSubHeader).isFalse()
        assertThat(stateSub.currentSection).isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)

        // Non-header normal line
        val stateNormal = Ocr3CategorizerHeader.HeaderState()
        val isNormal = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = "200 g de farine",
            lowerLine = "200 g de farine",
            containsAction = false,
            vocabulary = vocabulary,
            state = stateNormal,
            seenIngredientKeywords = mutableSetOf()
        )

        assertThat(isNormal).isFalse()
    }
}
