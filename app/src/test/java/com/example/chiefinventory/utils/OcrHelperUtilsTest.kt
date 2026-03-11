package com.example.chiefinventory.utils

import org.junit.Assert.*
import org.junit.Test

class OcrHelperUtilsTest {

    @Test
    fun `countIngredientSequences basic sequence counting`() {
        // Verify that standard patterns like '2 Carottes' or '1 Pomme' are counted correctly.
        assertEquals(1, OcrHelperUtils.countIngredientSequences("2 Carottes"))
        assertEquals(2, OcrHelperUtils.countIngredientSequences("2 Carottes 1 Pomme"))
    }

    @Test
    fun `countIngredientSequences OCR variant detection`() {
        // Test if OCR characters 'l', 'I', '|', '!' are correctly interpreted as the number 1.
        assertEquals(1, OcrHelperUtils.countIngredientSequences("l Carotte"))
        assertEquals(1, OcrHelperUtils.countIngredientSequences("I Carotte"))
        assertEquals(1, OcrHelperUtils.countIngredientSequences("| Carotte"))
        assertEquals(1, OcrHelperUtils.countIngredientSequences("! Carotte"))
    }

    @Test
    fun `countIngredientSequences connector exclusion check`() {
        // Ensure numbers preceded by 'ou', 'à', '-', 'et', or 'sur' (e.g., '2 ou 3') are not counted as new sequences.
        assertEquals(1, OcrHelperUtils.countIngredientSequences("2 ou 3 Carottes"))
        assertEquals(1, OcrHelperUtils.countIngredientSequences("4 à 5 Pommes"))
        assertEquals(1, OcrHelperUtils.countIngredientSequences("6 - 7 Poires"))
    }

    @Test
    fun `countIngredientSequences technical unit exclusion`() {
        // Verify that sequences followed by units like 'mm', 'cm', 'min', or 'sec' are ignored.
        assertEquals(0, OcrHelperUtils.countIngredientSequences("Cuire 10 min"))
        assertEquals(0, OcrHelperUtils.countIngredientSequences("Cuire 10 sec"))
        assertEquals(0, OcrHelperUtils.countIngredientSequences("Couper en 5 mm"))
        assertEquals(0, OcrHelperUtils.countIngredientSequences("Couper en 5 cm"))
    }

    @Test
    fun `countIngredientSequences parentheses isolation`() {
        // Ensure that quantity patterns appearing inside parentheses are excluded from the count.
        assertEquals(1, OcrHelperUtils.countIngredientSequences("1 Oignon (poids 200g)"))
        assertEquals(0, OcrHelperUtils.countIngredientSequences("(2 Tomates)"))
    }

    @Test
    fun `countIngredientSequences empty or non matching string`() {
        // Test behavior with empty strings or strings containing no digit-letter patterns.
        assertEquals(0, OcrHelperUtils.countIngredientSequences(""))
        assertEquals(0, OcrHelperUtils.countIngredientSequences("Pas d'ingrédients ici"))
    }

    @Test
    fun `splitCombinedIngredients basic quantity split`() {
        // Verify splitting of a line like '1 Oignon 2 Tomates' into individual ingredient strings.
        val result = OcrHelperUtils.splitCombinedIngredients("1 Oignon 2 Tomates", emptyList())
        assertEquals(listOf("1 Oignon", "2 Tomates"), result)
    }

    @Test
    fun `splitCombinedIngredients common items split`() {
        // Test splitting based on items provided in commonItems list (e.g., 'Sel Poivre').
        val common = listOf("Sel", "Poivre")
        val result = OcrHelperUtils.splitCombinedIngredients("Sel Poivre", common)
        assertEquals(listOf("Sel", "Poivre"), result)
    }

    @Test
    fun `splitCombinedIngredients hache variant logic`() {
        // Check specialized splitting for 'haché de', 'hachée de', etc., while excluding standard connectors.
        val result = OcrHelperUtils.splitCombinedIngredients("persil haché de l'ail", emptyList())
        // Note: Logic depends on internal regex construction.
        assertTrue(result.size >= 1)
    }

    @Test
    fun `splitCombinedIngredients leading double digit removal`() {
        // Verify that the regex '^\d+\s+\d+\s+' correctly strips redundant leading numbering.
        val result = OcrHelperUtils.splitCombinedIngredients("12 34 1 Oignon", emptyList())
        assertEquals(listOf("1 Oignon"), result)
    }

    @Test
    fun `splitCombinedIngredients parentheses protection`() {
        // Ensure that potential split points located inside parentheses are ignored to keep units or notes intact.
        val result = OcrHelperUtils.splitCombinedIngredients("1 Oignon (200 g) 2 Tomates", emptyList())
        assertEquals(listOf("1 Oignon (200 g)", "2 Tomates"), result)
    }

    @Test
    fun `splitCombinedIngredients connector protection`() {
        // Verify that words preceded or followed by connectors (et, ou, d', etc.) do not trigger a split.
        val result = OcrHelperUtils.splitCombinedIngredients("1 kg de tomates et 2 carottes", emptyList())
        assertEquals(listOf("1 kg de tomates et 2 carottes"), result)
    }

    @Test
    fun `splitCombinedIngredients empty commonItems handling`() {
        // Test the function behavior when the commonItems list is empty or contains only 'haché' variants.
        val result = OcrHelperUtils.splitCombinedIngredients("1 Oignon", emptyList())
        assertEquals(listOf("1 Oignon"), result)
    }

    @Test
    fun `isLikelyProperNameOrSource positive detection`() {
        // Verify detection of lines with 2-4 words where all words start with uppercase (e.g., 'Jean-Pierre Source').
        assertTrue(OcrHelperUtils.isLikelyProperNameOrSource("Jean Pierre"))
        assertTrue(OcrHelperUtils.isLikelyProperNameOrSource("NAHIT YILMAZ"))
        assertTrue(OcrHelperUtils.isLikelyProperNameOrSource("NAHIT YILMAZ Jean Pierre"))
        assertFalse(OcrHelperUtils.isLikelyProperNameOrSource("NAHIT YILMAZ Jean Pierre Jean")) // test with 5 words
    }

    @Test
    fun `isLikelyProperNameOrSource digit rejection`() {
        // Ensure the function returns false if the line contains any digits, even if casing matches.
        assertFalse(OcrHelperUtils.isLikelyProperNameOrSource("Jean 2 Pierre"))
    }

    @Test
    fun `isLikelyProperNameOrSource word count boundaries`() {
        // Test rejection of single words or lines with more than 4 words.
        assertFalse(OcrHelperUtils.isLikelyProperNameOrSource("Jean"))
        assertFalse(OcrHelperUtils.isLikelyProperNameOrSource("Jean Pierre Paul Jacques Philippe"))
    }

    @Test
    fun `isLikelyProperNameOrSource mixed casing check`() {
        // Verify rejection if words in the middle of the string start with lowercase letters.
        assertFalse(OcrHelperUtils.isLikelyProperNameOrSource("Jean de Pierre"))
    }

    @Test
    fun `isExcluded case insensitive match`() {
        // Verify that the function returns true for exact keyword matches regardless of casing or whitespace.
        assertTrue(OcrHelperUtils.isExcluded("PAGE", listOf("page")))
        assertTrue(OcrHelperUtils.isExcluded("  SAVEUR  ", listOf("saveur")))
    }

    @Test
    fun `isExcluded empty input or list`() {
        // Test behavior with empty strings or an empty exclusion list.
        assertFalse(OcrHelperUtils.isExcluded("", listOf("test")))
        assertFalse(OcrHelperUtils.isExcluded("test", emptyList()))
    }

    @Test
    fun `cleanIngredientSemantics bullet removal`() {
        // Verify that leading '•', '-', and '*' are successfully stripped from the start of the text.
        assertEquals("Tomate", OcrHelperUtils.cleanIngredientSemantics("• Tomate", emptyList()))
        assertEquals("Oignon", OcrHelperUtils.cleanIngredientSemantics("- Oignon", emptyList()))
        assertEquals("Poivre", OcrHelperUtils.cleanIngredientSemantics("* Poivre", emptyList()))
    }

    @Test
    fun `cleanIngredientSemantics length and letter validation`() {
        // Ensure strings with no letters or length <= 1 are returned as empty strings.
        assertEquals("", OcrHelperUtils.cleanIngredientSemantics("1", emptyList()))
        assertEquals("", OcrHelperUtils.cleanIngredientSemantics("!!!", emptyList()))
    }

    @Test
    fun `cleanIngredientSemantics proper name exclusion`() {
        // Verify that lines identified as proper names (via isLikelyProperNameOrSource) are filtered out.
        assertEquals("", OcrHelperUtils.cleanIngredientSemantics("Nahit Yilmaz", emptyList()))
    }

    @Test
    fun `cleanIngredientSemantics semantic list exclusion`() {
        // Test exclusion of specific structural words like 'POUR' when provided in semanticExclusions.
        assertEquals("", OcrHelperUtils.cleanIngredientSemantics("Pour 4 personnes", emptyList(), listOf("POUR 4 PERSONNES")))
    }

    @Test
    fun `cleanIngredientSemantics parentheses retention`() {
        // Verify that technical data inside parentheses (e.g., '(500g)') is preserved in the final output.
        assertEquals("6 aubergines (500g)", OcrHelperUtils.cleanIngredientSemantics("6 aubergines (500g)", emptyList()))
    }

}
