package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Ocr4MergerTest {

    private val mockModifiers = listOf("haché", "hachée", "émincés", "ciselé", "fondu", "salade")
    private val mockCommonIngredients = listOf("sel", "poivre", "beurre", "huile")

    @Test
    fun `mergeIngredients empty input`() {
        val result = Ocr4Merger.mergeIngredients(emptyList(), mockModifiers, emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    fun `mergeIngredients normalization filtering`() {
        val input = listOf("±", "environ")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    fun `mergeIngredients hanging connector suffix`() {
        val input = listOf("200 g de", "farine")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("200 g de farine")
    }

    @Test
    fun `mergeIngredients hanging noun suffix`() {
        val input = listOf("1 c. à soupe de vinaigre", "de vin blanc")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("1 c. à soupe de vinaigre de vin blanc")
    }

    @Test
    fun `mergeIngredients next line connector prefix`() {
        val input = listOf("Pommes de terre", "à l'huile")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("Pommes de terre à l'huile")
    }

    @Test
    fun `mergeIngredients quantity start exclusion`() {
        val input = listOf("200 g de farine", "• 2 oeufs")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("200 g de farine", "2 oeufs").inOrder()
    }

    @Test
    fun `mergeIngredients article start exclusion`() {
        val input = listOf("sel, poivre", "des petites feuilles de chicon")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, listOf("poivre"))
        assertThat(result).containsExactly("sel", "poivre", "des petites feuilles de chicon").inOrder()
    }

    @Test
    fun `mergeIngredients punctuation boundary check`() {
        val input = listOf("sel, poivre,", "de la ciboulette")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("sel, poivre", "de la ciboulette").inOrder()
    }

    @Test
    fun `mergeIngredients word hyphenation reconstruction`() {
        val input = listOf("une pin-", "cée de sel")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("une pincée de sel")
    }

    @Test
    fun `mergeIngredients non letter hyphen preservation`() {
        val input = listOf("Zone 5-", "Bis")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("Zone 5-Bis")
    }

    @Test
    fun `mergeIngredients orphan modifier haché`() {
        val input = listOf("1 c. à soupe de cerfeuil", "haché")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("1 c. à soupe de cerfeuil haché")
    }

    @Test
    fun `mergeIngredients orphan modifier with comma`() {
        val input = listOf("2 oignons", ", émincés")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("2 oignons , émincés")
    }

    @Test
    fun `mergeIngredients modifier length safety`() {
        val input = listOf("100 g de beurre", "fondu doucement dans une casserole à feu doux")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).hasSize(2)
    }

    @Test
    fun `mergeInstructions narrative block merging`() {
        val input = listOf("Plongez les aubergines", "dans l'eau bouillante")
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result).containsExactly("Plongez les aubergines dans l'eau bouillante")
    }

    @Test
    fun `mergeInstructions hyphenated word reconstruction`() {
        val input = listOf("une pin-", "cée de sel.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result).containsExactly("une pincée de sel.")
    }

    @Test
    fun `mergeInstructions should merge dimension line when previous line ends with hanging preposition`() {
        val input = listOf("Coupez le saumon en dés de", "5 mm sur 5 mm.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result).containsExactly("Coupez le saumon en dés de 5 mm sur 5 mm.")
    }
}
