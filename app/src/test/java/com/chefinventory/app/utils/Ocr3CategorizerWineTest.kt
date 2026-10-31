package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'extraction et la gestion de la section Vin
 * par le sous-composant [Ocr3CategorizerWine].
 */
class Ocr3CategorizerWineTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_ExhaustiveWineHeadersAndVintages() {
        val wineHeaders = listOf(
            "Conseil vin : Pinot Noir 75cl",
            "Vin conseillé : Sauvignon 75cl",
            "Accord vin : Chardonnay 75cl",
            "L'accord met/vin : Bordeaux 2010",
            "Sommelier : Merlot Carrefour 2015"
        )
        wineHeaders.forEach { wineLine ->
            val state = Ocr3CategorizerWine.WineState()
            val handled = Ocr3CategorizerWine.processWineLine(
                workingLine = wineLine,
                currentSection = Ocr3Categorizer.SECTION_NONE,
                isIngredientListClosed = false,
                containsAction = false,
                startsWithConnector = false,
                vocabulary = vocabulary,
                wineState = state
            )
            assertThat(handled).isTrue()
            assertThat(state.detectedWineList).isNotEmpty()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_CapturesValidWineLineAndUpdatesWineState() {
        val state = Ocr3CategorizerWine.WineState()
        val line = "Sauvignon de Touraine 1995, 75 cl"

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = line,
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isTrue()
        assertThat(state.detectedWineList).contains("Sauvignon de Touraine 1995, 75 cl")
        assertThat(state.isWineSectionClosed).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_RejectsLineContainingCulinaryActionVerb() {
        val state = Ocr3CategorizerWine.WineState()
        val line = "Mélangez le vin avec la sauce"

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = line,
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = true,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isFalse()
        assertThat(state.detectedWineList).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_CapturesMultiLineWineWithAppellations() {
        val state = Ocr3CategorizerWine.WineState()

        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Atinium, Bordeaux Graves",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        val handled2 = Ocr3CategorizerWine.processWineLine(
            workingLine = "MO, 1996, 75 cl",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled1).isTrue()
        assertThat(handled2).isTrue()
        assertThat(state.detectedWineList).contains("Atinium, Bordeaux Graves")
        assertThat(state.detectedWineList).contains("M.O., 1996, 75 cl")
        assertThat(state.isWineSectionClosed).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_DiscardsNoiseLinesAfterWineSectionIsClosed() {
        val state = Ocr3CategorizerWine.WineState()
        state.isWineSectionClosed = true

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "ATINUM",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isTrue()
        assertThat(state.detectedWineList).isEmpty()
    }
}
