package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

class Ocr4MergerTest {

    private val mockModifiers = listOf("haché", "hachée", "émincés", "ciselé", "fondu", "salade")
    private val mockCommonIngredients = listOf("sel", "poivre", "beurre", "huile")

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_EmptyInput() {
        val result = Ocr4Merger.mergeIngredients(emptyList(), mockModifiers, emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsHangingAndStartsWithConnector_Direct() {
        assertThat(Ocr4Merger.isHanging("du")).isTrue()
        assertThat(Ocr4Merger.isHanging("250 g")).isTrue()
        assertThat(Ocr4Merger.isHanging("250 g de", customConnectors = listOf("de"))).isTrue()

        assertThat(Ocr4Merger.startsWithConnector("de farine")).isTrue()
        assertThat(Ocr4Merger.startsWithConnector("de farine", customConnectors = listOf("de"))).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_NormalizationFiltering() {
        val input = listOf("±", "environ")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_HangingConnectorAndQuantityWithoutName() {
        val input1 = listOf("200 g de", "farine")
        val result1 = Ocr4Merger.mergeIngredients(input1, mockModifiers, emptyList())
        assertThat(result1).containsExactly("200 g de farine")

        val input2 = listOf("2 c. à soupe", "de sucre")
        val result2 = Ocr4Merger.mergeIngredients(input2, mockModifiers, emptyList())
        assertThat(result2).containsExactly("2 c. à soupe de sucre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_HangingNounSuffix() {
        val input = listOf("1 c. à soupe de vinaigre", "de vin blanc")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("1 c. à soupe de vinaigre de vin blanc")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_NextLineConnectorPrefix() {
        val input = listOf("Pommes de terre", "à l'huile")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("Pommes de terre à l'huile")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_QuantityStartExclusion() {
        val input = listOf("200 g de farine", "• 2 oeufs")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("200 g de farine", "2 oeufs").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_ArticleStartExclusion() {
        val input = listOf("sel, poivre", "des petites feuilles de chicon")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, listOf("poivre"))
        assertThat(result).containsExactly("sel", "poivre", "des petites feuilles de chicon").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_PunctuationBoundaryCheck() {
        val input = listOf("sel, poivre,", "de la ciboulette")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("sel, poivre", "de la ciboulette").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_WordHyphenationReconstruction() {
        val input = listOf("une pin-", "cée de sel")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("une pincée de sel")

        // Hyphen with space "pre- "
        val inputSpace = listOf("une pré- ", "paration de pâte")
        val resultSpace = Ocr4Merger.mergeIngredients(inputSpace, mockModifiers, emptyList())
        assertThat(resultSpace).containsExactly("une préparation de pâte")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_NonLetterHyphenPreservation() {
        val input = listOf("Zone 5-", "Bis")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("Zone 5-Bis")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_OrphanModifierHacheAndWithComma() {
        val input1 = listOf("1 c. à soupe de cerfeuil", "haché")
        val result1 = Ocr4Merger.mergeIngredients(input1, mockModifiers, emptyList())
        assertThat(result1).containsExactly("1 c. à soupe de cerfeuil haché")

        val input2 = listOf("2 oignons", ", émincés")
        val result2 = Ocr4Merger.mergeIngredients(input2, mockModifiers, emptyList())
        assertThat(result2).containsExactly("2 oignons , émincés")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_ModifierLengthSafety() {
        val input = listOf("100 g de beurre", "fondu doucement dans une casserole à feu doux")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).hasSize(2)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_RangeConnectors() {
        val input = listOf("1 ou", "2 citrons")
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("1 ou 2 citrons")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_NarrativeBlockAndUnclosedParens() {
        val input1 = listOf("Plongez les aubergines", "dans l'eau bouillante")
        val result1 = Ocr4Merger.mergeInstructions(input1)
        assertThat(result1).containsExactly("Plongez les aubergines dans l'eau bouillante")

        val input2 = listOf("Laissez mariner au frigo (2 heures", "à 4°C).")
        val result2 = Ocr4Merger.mergeInstructions(input2)
        assertThat(result2).containsExactly("Laissez mariner au frigo (2 heures à 4°C).")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_HyphenatedWordReconstruction() {
        val input = listOf("une pin-", "cée de sel.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result).containsExactly("une pincée de sel.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_DimensionLinePreposition() {
        val input = listOf("Coupez le saumon en dés de", "5 mm sur 5 mm.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result).containsExactly("Coupez le saumon en dés de 5 mm sur 5 mm.")
    }
}
