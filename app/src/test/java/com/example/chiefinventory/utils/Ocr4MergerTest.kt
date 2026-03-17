package com.example.chiefinventory.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Ocr4MergerTest {

    @Test
    fun `mergeIngredients empty input`() {
        // Verify that an empty input list returns an empty list without processing.
        val result = Ocr4Merger.mergeIngredients(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mergeIngredients normalization filtering`() {
        // Ensure that lines resulting in empty strings after OcrNormalizer.normalize are ignored.
        val input = listOf("±", "environ") // Rules in Normalizer that return empty or filtered out
        val result = Ocr4Merger.mergeIngredients(input)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mergeIngredients hanging connector suffix`() {
        // Test merging when the previous line ends with a connector like 'de', 'du', or 'd''.
        val input = listOf("200 g de", "farine")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(1, result.size)
        assertEquals("200 g de farine", result[0])
    }

    @Test
    fun `mergeIngredients hanging noun suffix`() {
        // Verify merging when the previous line ends with a specific functional noun such as 
        // 'cuillère', 'jus', or 'gousse'.
        val input = listOf("1 c. à soupe de vinaigre", "de vin blanc")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(1, result.size)
        assertEquals("1 c. à soupe de vinaigre de vin blanc", result[0])
    }

    @Test
    fun `mergeIngredients next line connector prefix`() {
        // Check if the current line is merged when it starts with a connector like 'aux' or 'à'.
        val input = listOf("Pommes de terre", "à l'huile")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(1, result.size)
        assertEquals("pommes de terre à l'huile", result[0])
    }

    @Test
    fun `mergeIngredients quantity start exclusion`() {
        // Ensure lines starting with digits or bullet points are treated as new ingredients 
        // and not merged, even if the previous line is hanging.
        val input = listOf("200 g de farine", "• 2 oeufs")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(2, result.size)
        assertEquals("200 g de farine", result[0])
        assertEquals("2 oeufs", result[1])
    }

    @Test
    fun `mergeIngredients article start exclusion`() {
        // Verify that lines starting with partitive articles (un, une, des, du, de la) are 
        // treated as new ingredients.
        val input = listOf("sel, poivre", "des petites feuilles de chicon")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(2, result.size)
        assertEquals("sel, poivre", result[0])
        assertEquals("des petites feuilles de chicon", result[1])
    }

    @Test
    fun `mergeIngredients punctuation boundary check`() {
        // Confirm that if the current buffer ends with a comma or period, no merging occurs 
        // even if other semantic rules match.
        val input = listOf("sel, poivre,", "de la ciboulette")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(2, result.size)
        assertEquals("sel, poivre", result[0]) // Normalizer trims punctuation at end
        assertEquals("de la ciboulette", result[1])
    }

    @Test
    fun `mergeIngredients word hyphenation reconstruction`() {
        // Test the logic where a word split by a hyphen at the end of a line is 
        // reconstructed without the hyphen and space.
        val input = listOf("une pin-", "cée de sel")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(1, result.size)
        assertEquals("une pincée de sel", result[0])
    }

    @Test
    fun `mergeIngredients non letter hyphen preservation`() {
        // Ensure that a trailing hyphen is not removed if the character preceding it is 
        // not a letter (e.g., a digit or symbol).
        val input = listOf("Zone 5-", "Bis")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(1, result.size)
        assertEquals("Zone 5- Bis", result[0])
    }

    @Test
    fun `mergeIngredients case insensitivity`() {
        // Verify that connectors and hanging nouns are identified correctly regardless 
        // of their casing (e.g., 'VINAIGRE' or 'De').
        val input = listOf("1 C. A SOUPE DE VINAIGRE", "DE VIN BLANC")
        val result = Ocr4Merger.mergeIngredients(input)
        assertEquals(1, result.size)
        assertTrue(result[0].contains("VINAIGRE DE VIN BLANC", ignoreCase = true))
    }

    @Test
    fun `mergeInstructions empty input`() {
        // Verify that an empty input list returns an empty list for instructions.
        val result = Ocr4Merger.mergeInstructions(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mergeInstructions narrative block merging`() {
        // Test that sequential lines of narrative text without step indicators (numbers/bullets) 
        // are merged into a single block.
        val input = listOf("Plongez les aubergines", "dans l'eau bouillante")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals(1, result.size)
        assertEquals("plongez les aubergines dans l'eau bouillante", result[0])
    }

    @Test
    fun `mergeInstructions new step detection`() {
        // Ensure that lines starting with numbers, dashes, or bullets force the 
        // finalization of the current block and start a new one.
        val input = listOf("Etape 1", "• Etape 2")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals(2, result.size)
    }

    @Test
    fun `mergeInstructions sentence splitting logic`() {
        // Verify that merged blocks are correctly split into individual strings based on 
        // sentence terminators like '.', '!', or '?'.
        val input = listOf("Mélangez. Ajoutez le sel! Servez?")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals(3, result.size)
        assertEquals("mélangez.", result[0])
        assertEquals("ajoutez le sel!", result[1])
        assertEquals("servez?", result[2])
    }

    @Test
    fun `mergeInstructions split lookbehind accuracy`() {
        // Ensure the sentence splitter uses a positive lookbehind so that punctuation 
        // marks are preserved at the end of the resulting strings.
        val input = listOf("Faites cuire. Coupez ensuite.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals("faites cuire.", result[0])
        assertEquals("coupez ensuite.", result[1])
    }

    @Test
    fun `mergeInstructions whitespace cleanup`() {
        // Check that extra spaces resulting from merging lines or splitting sentences 
        // are trimmed and cleaned.
        val input = listOf("  Ligne 1  ", "   Ligne 2   ")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals("ligne 1 ligne 2", result[0])
    }

    @Test
    fun `mergeInstructions hyphenated word reconstruction`() {
        // Verify that the hyphenation rule applies to narrative instruction blocks 
        // similarly to ingredients.
        val input = listOf("une pin-", "cée de sel.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals("une pincée de sel.", result[0])
    }

    @Test
    fun `mergeInstructions multiple punctuation handling`() {
        // Test behavior when sentences end with multiple punctuation marks like '!!!' 
        // to ensure they don't produce empty strings.
        val input = listOf("Attention !!! C'est chaud.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals(2, result.size)
        assertEquals("attention !!!", result[0])
    }

    @Test
    fun `mergeInstructions digit in middle of text`() {
        // Ensure that a digit appearing in the middle of a sentence (e.g., 'wait 5 minutes') 
        // does not trigger the 'isNewStep' logic.
        val input = listOf("Attendez 5 minutes", "puis servez.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals(1, result.size)
        assertEquals("attendez 5 minutes puis servez.", result[0])
    }

    @Test
    fun `mergeInstructions empty string normalization`() {
        // Confirm that whitespace-only lines are discarded during the instruction 
        // merging process.
        val input = listOf("Ligne 1", "  ", "Ligne 2")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals(1, result.size) // They merge because no sentence end
    }
}
