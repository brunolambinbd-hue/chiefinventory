package com.chefinventory.app.utils

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant l'extraction et la gestion de la section Vin
 * par le sous-composant [Ocr3CategorizerWine].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerWineTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsWineLineAndContainsWholeWordAndLoadResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val wineRes = Ocr3CategorizerWine.loadResources(context.resources)
        assertThat(wineRes.appellations).isNotEmpty()
        assertThat(wineRes.producers).isNotEmpty()

        // generic wine words -> false
        assertThat(Ocr3CategorizerWine.isWineLine("vin", vocabulary)).isFalse()

        // contains whole word edge cases
        assertThat(Ocr3CategorizerWine.containsWholeWord("   ", "Bordeaux")).isFalse()
        assertThat(Ocr3CategorizerWine.containsWholeWord("Bordeaux 1996", "   ")).isFalse()
        assertThat(Ocr3CategorizerWine.containsWholeWord("Bordeaux 1996", "Bordeaux")).isTrue()

        // Short vintage year line (<15)
        assertThat(Ocr3CategorizerWine.isWineLine("2015", vocabulary)).isTrue()

        // Wine producer match
        assertThat(Ocr3CategorizerWine.isWineLine("Château Margaux 2015", vocabulary)).isTrue()

        // Test loadResources exception and keyword match
        val mockRes = mock<Resources> {
            on { getStringArray(com.chefinventory.app.R.array.wine_appellations) } doThrow Resources.NotFoundException()
            on { getStringArray(com.chefinventory.app.R.array.wine_producers) } doThrow Resources.NotFoundException()
            on { getStringArray(com.chefinventory.app.R.array.wine_keywords) } doThrow Resources.NotFoundException()
            on { getStringArray(com.chefinventory.app.R.array.wine_title_keywords) } doThrow Resources.NotFoundException()
            on { getString(com.chefinventory.app.R.string.wine_remove_pattern) } doThrow Resources.NotFoundException()
        }

        val emptyRes = Ocr3CategorizerWine.loadResources(mockRes)
        assertThat(emptyRes.removePattern).isEmpty()

        val keywordRes = Ocr3CategorizerWine.WineResources(
            appellations = emptyList(),
            producers = emptyList(),
            keywords = listOf("cuvée", "millesime"),
            titleKeywords = emptyList(),
            removePattern = ""
        )
        assertThat(Ocr3CategorizerWine.isWineLine("Une belle cuvée 2018", keywordRes)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_ImplicitWineBlankAndDuplicateBranches() {
        val state = Ocr3CategorizerWine.WineState()

        // Case 1: Implicit wine header line "suggestion :" where cleanWineLine is "" (isNotBlank == false)
        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "suggestion :",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertThat(handled1).isTrue()
        assertThat(state.detectedWineList).isEmpty()

        // Case 2: Implicit wine duplicate matching SECOND item in multi-element list -> duplicate rejected
        state.detectedWineList.add("Château Margaux 1990")
        state.detectedWineList.add("Bordeaux 1996 75 cl")
        val handled2 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Bordeaux 1996 75 cl",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = true,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertThat(handled2).isTrue()
        assertThat(state.detectedWineList).hasSize(2)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_BrandIsNotBlankValidation() {
        // Test with blank brands in knownSourceBrands to cover brand.isNotBlank() false branch
        val customVocab = vocabulary.copy(knownSourceBrands = listOf("", "   ", "Delhaize"))

        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château Delhaize",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = customVocab,
            wineState = state
        )

        assertThat(handled).isTrue()
        assertThat(state.isWineSectionClosed).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_KnownBrandAndNonBrandBranches() {
        val customVocab = vocabulary.copy(knownSourceBrands = listOf("Carrefour", "Delhaize"))

        // Case 1: Known brand "Delhaize" present -> closes wine section
        val stateWithBrand = Ocr3CategorizerWine.WineState()
        stateWithBrand.isWineHeaderActive = true

        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château Delhaize",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = customVocab,
            wineState = stateWithBrand
        )
        assertThat(handled1).isTrue()
        assertThat(stateWithBrand.isWineSectionClosed).isTrue()

        // Case 2: Non-brand / Non-existent brand "BrandInexistant777" -> does not close wine section
        val stateWithoutBrand = Ocr3CategorizerWine.WineState()
        stateWithoutBrand.isWineHeaderActive = true

        val handled2 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château BrandInexistant777",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = customVocab,
            wineState = stateWithoutBrand
        )
        assertThat(handled2).isTrue()
        assertThat(stateWithoutBrand.isWineSectionClosed).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_Vintage2010Term3Branch() {
        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true

        // Line without volume and without carrefour, but with "2010" -> Term 3 evaluated true
        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Bordeaux 2010",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isTrue()
        assertThat(state.detectedWineList).contains("Bordeaux 2010")
        assertThat(state.isWineSectionClosed).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_DuplicateWineUnderActiveHeaderElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true
        state.detectedWineList.add("Château Margaux")

        // Passing duplicate wine "Château Margaux" when header is active -> L68 else branch (duplicate rejected)
        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château Margaux",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isTrue()
        assertThat(state.detectedWineList).hasSize(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_DuplicateWineImplicitDetectionElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        state.detectedWineList.add("Bordeaux 1996 75 cl")

        // Passing duplicate wine line -> else branch (duplicate rejected)
        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Bordeaux 1996 75 cl",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isTrue()
        assertThat(state.detectedWineList).hasSize(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_PureHeaderLineAndMultiLineWithoutVolume() {
        val state = Ocr3CategorizerWine.WineState()

        // Line 1: Pure header line "Conseil vin :" -> cleanWineLine is "" (isNotBlank == false)
        val handled1 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Conseil vin :",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertThat(handled1).isTrue()
        assertThat(state.isWineHeaderActive).isTrue()
        assertThat(state.detectedWineList).isEmpty()

        // Line 2: Wine name without volume "Château Margaux" -> L68 evaluated false
        val handled2 = Ocr3CategorizerWine.processWineLine(
            workingLine = "Château Margaux",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertThat(handled2).isTrue()
        assertThat(state.detectedWineList).contains("Château Margaux")
        assertThat(state.isWineSectionClosed).isFalse()

        // Line 3: Bottle volume line "75 cl" -> L68 evaluated false, section closed
        val handled3 = Ocr3CategorizerWine.processWineLine(
            workingLine = "75 cl",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertThat(handled3).isTrue()
        assertThat(state.isWineSectionClosed).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_NonExplicitWineHeaderElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        // Line without explicit header ("conseil vin") -> Else branch 1 (isExplicitWineHeader = false)
        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "200 g de farine",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )
        assertThat(handled).isFalse()
        assertThat(state.isWineHeaderActive).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_ActiveHeaderWhenWineSectionIsClosedElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        state.isWineHeaderActive = true
        state.isWineSectionClosed = true // Closed section -> Else branch 2

        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Sommelier : Bordeaux 1996 75 cl",
            currentSection = Ocr3Categorizer.SECTION_NONE,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isTrue()
        assertThat(state.detectedWineList).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessWineLine_NonWineLineReturnsFalseElseBranch() {
        val state = Ocr3CategorizerWine.WineState()
        // Line is not a wine line -> Else branch 3
        val handled = Ocr3CategorizerWine.processWineLine(
            workingLine = "Faire cuire au four",
            currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS,
            isIngredientListClosed = false,
            containsAction = false,
            startsWithConnector = false,
            vocabulary = vocabulary,
            wineState = state
        )

        assertThat(handled).isFalse()
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
        state.isWineHeaderActive = true
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
