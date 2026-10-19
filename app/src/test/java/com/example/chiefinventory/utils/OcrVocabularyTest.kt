package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock

class OcrVocabularyTest {

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
        assertTrue(vocabulary.wineAppellations.contains("saumur"))
        assertTrue(vocabulary.wineAppellations.contains("chablis"))

        assertFalse("Real step action keywords should be loaded from step_keywords.xml", vocabulary.stepActionKeywords.isEmpty())
        assertFalse("Real common ingredients should be loaded from ingredients.xml", vocabulary.commonIngredientsNoQty.isEmpty())
        assertTrue(vocabulary.commonIngredientsNoQty.contains("sel"))

        assertFalse("Real wine remove pattern should be loaded from wine_arrays.xml", vocabulary.wineRemovePattern.isBlank())
    }
}
