package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
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
    fun `processWineLine should capture valid wine line and update wineState`() {
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
    fun `processWineLine should reject line containing culinary action verb`() {
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
    fun `processWineLine should reject line starting with ingredient connector`() {
        val state = Ocr3CategorizerWine.WineState()
        val line = "de vin blanc"

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = line,
            currentSection = Ocr3Categorizer.SECTION_INGREDIENTS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = true,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isFalse()
        assertThat(state.detectedWineList).isEmpty()
    }

    @Test
    fun `processWineLine should capture multi line wine recommendation before closure`() {
        val state = Ocr3CategorizerWine.WineState()

        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Atinum, Bordeaux Graves",
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
    fun `processWineLine should discard noise lines after wine section is closed`() {
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
