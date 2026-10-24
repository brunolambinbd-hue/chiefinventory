package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.Ocr4Merger
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Niveau A vérifiant les règles de fusion et recollage sémantique de [Ocr4Merger].
 */
class Ocr4MergerRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `mergeInstructions should fluidly reconnect et- hyphenated conjunction line with space`() {
        val input = listOf(
            "Pendant ce temps, hachez fin",
            "les radis et les gousses d'ail et-",
            "mélangez-les à la crème"
        )
        val result = Ocr4Merger.mergeInstructions(input)
        assertThat(result.first()).contains("gousses d'ail et mélangez-les")
    }

    @Test
    fun `mergeIngredients should merge range connectors 2 ou 3 paquets`() {
        val input = listOf("2 ou", "3 paquets", "de jambon de Parme")
        val result = Ocr4Merger.mergeIngredients(input, vocabulary.ingredientPreparationModifiers, vocabulary.commonIngredientsNoQty, vocabulary.mandatoryComplementIngredients)
        assertThat(result.first()).contains("2 ou 3 paquets de jambon de Parme")
    }

    @Test
    fun `mergeIngredients should merge unclosed parenthesis ingredient across two lines`() {
        val input = listOf("1 citron (1/2 pour le jus -", "1/2 pour la décoration)")
        val result = Ocr4Merger.mergeIngredients(input, vocabulary.ingredientPreparationModifiers, vocabulary.commonIngredientsNoQty, vocabulary.mandatoryComplementIngredients)
        assertThat(result.first()).contains("1 citron (1/2 pour le jus - 1/2 pour la décoration)")
    }

    @Test
    fun `mergeIngredients should merge 4 line ingredient with parenthetical weight range`() {
        val input = listOf("2 ou 3 paquets", "de jambon de Parme", "en fines tranches", "(125 à 180 g)")
        val result = Ocr4Merger.mergeIngredients(input, vocabulary.ingredientPreparationModifiers, vocabulary.commonIngredientsNoQty, vocabulary.mandatoryComplementIngredients)
        assertThat(result.first()).contains("2 ou 3 paquets de jambon de Parme en fines tranches (125 à 180 g)")
    }

    @Test
    fun `mergeIngredients should merge pour wok subline into preceding legumes eminces line`() {
        val input = listOf("200 g de légumes émincés", "Pour wok (avec soja)")
        val result = Ocr4Merger.mergeIngredients(input, vocabulary.ingredientPreparationModifiers, vocabulary.commonIngredientsNoQty, vocabulary.mandatoryComplementIngredients, vocabulary.ingredientConnectors)
        assertThat(result.first()).contains("200 g de légumes émincés pour wok (avec soja)")
    }
}
