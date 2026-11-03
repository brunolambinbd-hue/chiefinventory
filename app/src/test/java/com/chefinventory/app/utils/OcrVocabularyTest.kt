package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock

class OcrVocabularyTest {

    @Test
    fun testOcrVocabularyAll78CopyAndConstructorBitmaskBranches() {
        val base = OcrVocabulary()

        // Call copy() providing ALL 39 parameters explicitly
        // This forces all 39 bitmask branches in copy$default to evaluate to FALSE!
        val fullCopy = base.copy(
            stepActionKeywords = listOf("a"),
            commonIngredientsNoQty = listOf("b"),
            excludedOcrKeywords = listOf("c"),
            ingredientPreparationModifiers = listOf("d"),
            instructionSwitchKeywords = listOf("e"),
            ingredientSemanticExclusions = listOf("f"),
            ingredientEndMarkers = listOf("g"),
            ingredientSubHeaders = listOf("h"),
            sourceKeywords = listOf("i"),
            knownSourceBrands = listOf("j"),
            phonePrefixes = listOf("k"),
            wineAppellations = listOf("l"),
            wineProducers = listOf("m"),
            wineKeywords = listOf("n"),
            wineTitleKeywords = listOf("o"),
            ocr3ExplicitWineHeaders = listOf("p"),
            wineRemovePattern = "q",
            ocrSpellingRepairs = listOf("r"),
            adExclusions = listOf("s"),
            ocrNoiseExclusions = listOf("t"),
            titleKeywords = listOf("u"),
            mandatoryComplementIngredients = listOf("v"),
            hangingConnectors = listOf("w"),
            ingredientVarietyQualifiers = listOf("x"),
            baseIngredientsExpectingVariety = listOf("y"),
            standaloneArticles = listOf("z"),
            culinaryMeasurementUnits = listOf("aa"),
            titleSpellingRepairs = listOf("bb"),
            titleMultilineConnectors = listOf("cc"),
            ingredientSplitterConnectors = listOf("dd"),
            ocrDiacriticRepairs = listOf("ee"),
            priceTitleKeywords = listOf("ff"),
            priceCategoriesLow = listOf("gg"),
            priceCategoriesMedium = listOf("hh"),
            priceCategoriesHigh = listOf("ii"),
            ingredientConnectors = listOf("jj"),
            protectedSectionHeaders = listOf("kk"),
            timeHeaderPrefixes = listOf("ll"),
            timeHeaderKeywords = listOf("mm")
        )

        assertEquals(listOf("mm"), fullCopy.timeHeaderKeywords)
    }

    @Test
    fun testOcrVocabularyExhaustiveBytecodeBranches() {
        // 1. Instantiation providing ALL 39 explicit arguments (forces all bitmasks to false)
        val fullExplicit = OcrVocabulary(
            stepActionKeywords = listOf("a"),
            commonIngredientsNoQty = listOf("b"),
            excludedOcrKeywords = listOf("c"),
            ingredientPreparationModifiers = listOf("d"),
            instructionSwitchKeywords = listOf("e"),
            ingredientSemanticExclusions = listOf("f"),
            ingredientEndMarkers = listOf("g"),
            ingredientSubHeaders = listOf("h"),
            sourceKeywords = listOf("i"),
            knownSourceBrands = listOf("j"),
            phonePrefixes = listOf("k"),
            wineAppellations = listOf("l"),
            wineProducers = listOf("m"),
            wineKeywords = listOf("n"),
            wineTitleKeywords = listOf("o"),
            ocr3ExplicitWineHeaders = listOf("p"),
            wineRemovePattern = "q",
            ocrSpellingRepairs = listOf("r"),
            adExclusions = listOf("s"),
            ocrNoiseExclusions = listOf("t"),
            titleKeywords = listOf("u"),
            mandatoryComplementIngredients = listOf("v"),
            hangingConnectors = listOf("w"),
            ingredientVarietyQualifiers = listOf("x"),
            baseIngredientsExpectingVariety = listOf("y"),
            standaloneArticles = listOf("z"),
            culinaryMeasurementUnits = listOf("aa"),
            titleSpellingRepairs = listOf("bb"),
            titleMultilineConnectors = listOf("cc"),
            ingredientSplitterConnectors = listOf("dd"),
            ocrDiacriticRepairs = listOf("ee"),
            priceTitleKeywords = listOf("ff"),
            priceCategoriesLow = listOf("gg"),
            priceCategoriesMedium = listOf("hh"),
            priceCategoriesHigh = listOf("ii"),
            ingredientConnectors = listOf("jj"),
            protectedSectionHeaders = listOf("kk"),
            timeHeaderPrefixes = listOf("ll"),
            timeHeaderKeywords = listOf("mm")
        )

        // 2. Equals comparison evaluating through the 39th field
        val fullExplicitSame = fullExplicit.copy()
        val fullExplicitDiffLast = fullExplicit.copy(timeHeaderKeywords = listOf("diff"))

        assertTrue(fullExplicit == fullExplicitSame)
        assertFalse(fullExplicit == fullExplicitDiffLast)
        assertEquals(listOf("mm"), fullExplicit.component39())
    }

    @Test
    fun testOcrVocabularyDataClassGeneratedMethods() {
        val vocab1 = OcrVocabulary(stepActionKeywords = listOf("couper"))
        val vocab2 = vocab1.copy()
        val vocab3 = vocab1.copy(stepActionKeywords = listOf("mélanger"))

        assertTrue(vocab1 == vocab2)
        assertFalse(vocab1 == vocab3)
        assertFalse(vocab1.equals(null))
        assertFalse(vocab1.equals("autreObjet"))
        assertEquals(vocab1.hashCode(), vocab2.hashCode())
        assertTrue(vocab1.toString().contains("OcrVocabulary"))
        assertEquals(listOf("couper"), vocab1.component1())
    }

    @Test
    fun `default instantiation should have empty lists and blank pattern`() {
        val vocabulary = OcrVocabulary()
        
        assertTrue(vocabulary.stepActionKeywords.isEmpty())
        assertTrue(vocabulary.commonIngredientsNoQty.isEmpty())
        assertTrue(vocabulary.excludedOcrKeywords.isEmpty())
        assertTrue(vocabulary.ingredientPreparationModifiers.isEmpty())
        assertTrue(vocabulary.instructionSwitchKeywords.isEmpty())
        assertTrue(vocabulary.ingredientSemanticExclusions.isEmpty())
        assertTrue(vocabulary.ingredientEndMarkers.isEmpty())
        assertTrue(vocabulary.ingredientSubHeaders.isEmpty())
        assertTrue(vocabulary.sourceKeywords.isEmpty())
        assertTrue(vocabulary.phonePrefixes.isEmpty())
        assertTrue(vocabulary.wineAppellations.isEmpty())
        assertTrue(vocabulary.wineProducers.isEmpty())
        assertTrue(vocabulary.wineKeywords.isEmpty())
        assertTrue(vocabulary.wineTitleKeywords.isEmpty())
        assertEquals("", vocabulary.wineRemovePattern)
    }

    @Test
    fun `custom instantiation should preserve provided keyword lists`() {
        val actions = listOf("couper", "mélanger")
        val ingredients = listOf("farine", "oeufs")
        
        val vocabulary = OcrVocabulary(
            stepActionKeywords = actions,
            commonIngredientsNoQty = ingredients,
            wineRemovePattern = "Accord: "
        )

        assertEquals(actions, vocabulary.stepActionKeywords)
        assertEquals(ingredients, vocabulary.commonIngredientsNoQty)
        assertEquals("Accord: ", vocabulary.wineRemovePattern)
    }

    @Test
    fun `fromResources should map all array resources to vocabulary lists`() {
        val mockResources = mock<Resources> {
            on { getStringArray(R.array.step_action_keywords) } doReturn arrayOf("couper", "mélanger")
            on { getStringArray(R.array.common_ingredients_no_qty) } doReturn arrayOf("sel", "poivre")
            on { getStringArray(R.array.excluded_ocr_keywords) } doReturn arrayOf("page", "pub")
            on { getStringArray(R.array.ingredient_preparation_modifiers) } doReturn arrayOf("haché")
            on { getStringArray(R.array.instruction_switch_keywords) } doReturn arrayOf("cuisson")
            on { getStringArray(R.array.ingredient_semantic_exclusions) } doReturn arrayOf("bio")
            on { getStringArray(R.array.ingredient_end_markers) } doReturn arrayOf("fin")
            on { getStringArray(R.array.ingredient_sub_headers) } doReturn arrayOf("sauce")
            on { getStringArray(R.array.source_keywords) } doReturn arrayOf("hôtel")
            on { getStringArray(R.array.phone_prefixes) } doReturn arrayOf("tél")
            on { getStringArray(R.array.wine_appellations) } doReturn arrayOf("Bordeaux")
            on { getStringArray(R.array.wine_producers) } doReturn arrayOf("Château")
            on { getStringArray(R.array.wine_keywords) } doReturn arrayOf("vin")
            on { getStringArray(R.array.wine_title_keywords) } doReturn arrayOf("médaille")
            on { getString(R.string.wine_remove_pattern) } doReturn "Accord: "
        }

        val vocabulary = OcrVocabulary.fromResources(mockResources)

        assertEquals(listOf("couper", "mélanger"), vocabulary.stepActionKeywords)
        assertEquals(listOf("sel", "poivre"), vocabulary.commonIngredientsNoQty)
        assertEquals(listOf("page", "pub"), vocabulary.excludedOcrKeywords)
        assertEquals(listOf("haché"), vocabulary.ingredientPreparationModifiers)
        assertEquals(listOf("cuisson"), vocabulary.instructionSwitchKeywords)
        assertEquals(listOf("bio"), vocabulary.ingredientSemanticExclusions)
        assertEquals(listOf("fin"), vocabulary.ingredientEndMarkers)
        assertEquals(listOf("sauce"), vocabulary.ingredientSubHeaders)
        assertEquals(listOf("hôtel"), vocabulary.sourceKeywords)
        assertEquals(listOf("tél"), vocabulary.phonePrefixes)
        assertEquals(listOf("Bordeaux"), vocabulary.wineAppellations)
        assertEquals(listOf("Château"), vocabulary.wineProducers)
        assertEquals(listOf("vin"), vocabulary.wineKeywords)
        assertEquals(listOf("médaille"), vocabulary.wineTitleKeywords)
        assertEquals("Accord: ", vocabulary.wineRemovePattern)
    }

    @Test
    fun `fromResources should handle missing resources safely without crashing`() {
        val mockResources = mock<Resources> {
            on { getStringArray(R.array.step_action_keywords) } doThrow Resources.NotFoundException("Not found")
            on { getString(R.string.wine_remove_pattern) } doThrow Resources.NotFoundException("Not found")
        }

        val vocabulary = OcrVocabulary.fromResources(mockResources)

        assertTrue(vocabulary.stepActionKeywords.isEmpty())
        assertEquals("", vocabulary.wineRemovePattern)
    }

    @Test
    fun `loadFromProjectResources should parse real production XML files from disk`() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        assertFalse("Real wine appellations should be loaded from wine_arrays.xml", vocabulary.wineAppellations.isEmpty())
        assertTrue(vocabulary.wineAppellations.any { it.equals("saumur", ignoreCase = true) })
        assertTrue(vocabulary.wineAppellations.any { it.equals("chablis", ignoreCase = true) })

        assertFalse("Real step action keywords should be loaded from step_keywords.xml", vocabulary.stepActionKeywords.isEmpty())
        assertFalse("Real common ingredients should be loaded from ingredients.xml", vocabulary.commonIngredientsNoQty.isEmpty())
        assertTrue(vocabulary.commonIngredientsNoQty.contains("sel"))

        assertFalse("Real wine remove pattern should be loaded from wine_arrays.xml", vocabulary.wineRemovePattern.isBlank())
    }
}
