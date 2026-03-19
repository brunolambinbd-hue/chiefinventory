package com.example.chiefinventory.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Ocr4MergerTest {    // Liste de simulation pour les tests (reflète le contenu de modifiers.xml)
    private val mockModifiers = listOf("haché", "hachée", "émincés", "ciselé", "fondu", "salade")

    @Test
    fun `mergeIngredients empty input`() {
        // Verify that an empty input list returns an empty list without processing.
        val result = Ocr4Merger.mergeIngredients(emptyList(), mockModifiers)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mergeIngredients normalization filtering`() {
        // Ensure that lines resulting in empty strings after OcrNormalizer.normalize are ignored.
        val input = listOf("±", "environ")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mergeIngredients hanging connector suffix`() {
        // Test merging when the previous line ends with a connector like 'de', 'du', or 'd''.
        val input = listOf("200 g de", "farine")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(1, result.size)
        assertEquals("200 g de farine", result[0])
    }

    @Test
    fun `mergeIngredients hanging noun suffix`() {
        // Verify merging when the previous line ends with a specific functional noun such as 'cuillère', 'jus', or 'gousse'.
        val input = listOf("1 c. à soupe de vinaigre", "de vin blanc")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(1, result.size)
        assertEquals("1 c. à soupe de vinaigre de vin blanc", result[0])
    }

    @Test
    fun `mergeIngredients next line connector prefix`() {
        // Check if the current line is merged when it starts with a connector like 'aux' or 'à'.
        val input = listOf("Pommes de terre", "à l'huile")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(1, result.size)
        assertEquals("Pommes de terre à l'huile", result[0])
    }

    @Test
    fun `mergeIngredients quantity start exclusion`() {
        // Ensure lines starting with digits or bullet points are treated as new ingredients
        // and not merged, even if the previous line is hanging.
        val input = listOf("200 g de farine", "• 2 oeufs")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(2, result.size)
        assertEquals("200 g de farine", result[0])
        assertEquals("2 oeufs", result[1])
    }

    @Test
    fun `mergeIngredients article start exclusion`() {
        // Verify that lines starting with partitive articles (un, une, des, du, de la) are treated as new ingredients.
        val input = listOf("sel, poivre", "des petites feuilles de chicon")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(2, result.size)
        assertEquals("sel, poivre", result[0])
        assertEquals("des petites feuilles de chicon", result[1])
    }

    @Test
    fun `mergeIngredients punctuation boundary check`() {
        // Confirm that if the current buffer ends with a comma or period, no merging occurs even if other semantic rules match.
        val input = listOf("sel, poivre,", "de la ciboulette")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(2, result.size)
        assertEquals("sel, poivre", result[0])
        assertEquals("de la ciboulette", result[1])
    }

    @Test
    fun `mergeIngredients word hyphenation reconstruction`() {
        // Test the logic where a word split by a hyphen at the end of a line is reconstructed without the hyphen and space.
        val input = listOf("une pin-", "cée de sel")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(1, result.size)
        assertEquals("une pincée de sel", result[0])
    }

    @Test
    fun `mergeIngredients non letter hyphen preservation`() {
        // Ensure that a trailing hyphen is not removed if the character preceding it is not a letter.
        val input = listOf("Zone 5-", "Bis")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(1, result.size)
        assertEquals("Zone 5- Bis", result[0])
    }

    @Test
    fun `mergeIngredients orphan modifier haché`() {
        // Verify that a preparation modifier like 'haché' alone on a line is merged back.
        val input = listOf("1 c. à soupe de cerfeuil", "haché")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(1, result.size)
        assertEquals("1 c. à soupe de cerfeuil haché", result[0])
    }

    @Test
    fun `mergeIngredients orphan modifier with comma`() {
        // Check that a line starting with a comma is merged as a continuation.
        val input = listOf("2 oignons", ", émincés")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        assertEquals(1, result.size)
        assertEquals("2 oignons , émincés", result[0])
    }

    @Test
    fun `mergeIngredients modifier length safety`() {
        // Verify that a long line starting with a modifier word is NOT merged (likely a new instruction).
        val input = listOf("100 g de beurre", "fondu doucement dans une casserole à feu doux")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers)
        // La ligne est trop longue (> 3 mots), elle ne doit pas fusionner
        assertEquals(2, result.size)
    }

    @Test
    fun `mergeInstructions narrative block merging`() {
        val input = listOf("Plongez les aubergines", "dans l'eau bouillante")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals(1, result.size)
        assertEquals("Plongez les aubergines dans l'eau bouillante", result[0])
    }

    @Test
    fun `mergeInstructions hyphenated word reconstruction`() {
        val input = listOf("une pin-", "cée de sel.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertEquals("une pincée de sel.", result[0])
    }
}