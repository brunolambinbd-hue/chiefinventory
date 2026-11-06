package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B dédiés à [Ocr4Merger].
 */
class Ocr4MergerTest {

    private val mockModifiers = listOf("haché", "hachée", "émincé", "émincée")

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_HyphenatedAndHangingConnectors() {
        val input = listOf(
            "200 g de poivrons",
            "rouges-",
            "et jaunes"
        )
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_OrphanModifier() {
        val input = listOf(
            "1 oignon",
            "émincé"
        )
        val result = Ocr4Merger.mergeIngredients(input, mockModifiers, emptyList())
        assertThat(result).containsExactly("1 oignon émincé")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_CustomConnectorsAndEmptyInput() {
        assertThat(Ocr4Merger.mergeIngredients(emptyList(), emptyList(), emptyList())).isEmpty()

        val input = listOf("1 verre", "de lait")
        val result = Ocr4Merger.mergeIngredients(input, emptyList(), emptyList(), customConnectors = listOf("de"))
        assertThat(result).containsExactly("1 verre de lait")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_EmptyInputAndPunctuationSplits() {
        assertThat(Ocr4Merger.mergeInstructions(emptyList())).isEmpty()

        val input = listOf("Préparez la pâte. Laissez reposer. Cuisez à 180°C.")
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result).containsExactly(
            "Préparez la pâte.",
            "Laissez reposer.",
            "Cuisez à 180°C."
        )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsHanging_And_StartsWithConnector_Branches() {
        assertThat(Ocr4Merger.isHanging("le")).isTrue()
        assertThat(Ocr4Merger.isHanging("1 citron (1/2 pour le jus -")).isTrue()
        assertThat(Ocr4Merger.isHanging("100 g de")).isTrue()
        assertThat(Ocr4Merger.isHanging("2 cuillères")).isTrue()
        assertThat(Ocr4Merger.isHanging("1 pomme de terre")).isFalse()

        assertThat(Ocr4Merger.startsWithConnector("de farine")).isTrue()
        assertThat(Ocr4Merger.startsWithConnector("d'huile")).isTrue()
        assertThat(Ocr4Merger.startsWithConnector("farine")).isFalse()
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
    fun testMergeInstructions_LangoustinesHyphenatedWordReconstruction() {
        val input = listOf(
            "Vous poOuvez préparer une soupe avec les têtes et les carcasses des lan-",
            "goustines."
        )
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result).containsExactly(
            "Vous poOuvez préparer une soupe avec les têtes et les carcasses des langoustines."
        )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_TrailingArticleDesMerge() {
        val input = listOf(
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des",
            "lamelles de mangue. Décorez de salade et de cresson, ajoutez les",
            "croûtons."
        )
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result).containsExactly(
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des lamelles de mangue.",
            "Décorez de salade et de cresson, ajoutez les croûtons."
        )
    }
}
