package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de [OcrIngredientUtils].
 */
class OcrIngredientUtilsTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitCombinedIngredients_DefaultArgumentsOverloadsAndBlankLine() {
        // 1. Call with ONLY 2 arguments to cover synthetic default parameter overloads
        val result1 = OcrIngredientUtils.splitCombinedIngredients("200 g de farine", listOf("farine"))
        assertThat(result1).containsExactly("200 g de farine")

        // 2. Blank line returns emptyList
        val result2 = OcrIngredientUtils.splitCombinedIngredients("   ", emptyList())
        assertThat(result2).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCountIngredientSequences_WithNumberFollowingMatch() {
        // Word following "200 g" is number "100" without letters -> covers isLetter() false branch
        val count = OcrIngredientUtils.countIngredientSequences("200 g 100")
        assertThat(count).isEqualTo(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitCombinedIngredients_ParenthesisAndPrefixNumbers() {
        // Line with quantity immediately before parenthesis
        val result = OcrIngredientUtils.splitCombinedIngredients("200 g de beurre (doux) 100 g de sucre", listOf("beurre", "sucre"))
        assertThat(result).hasSize(2)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanIngredientSemantics_SingleWordStandaloneArticle() {
        assertThat(OcrIngredientUtils.cleanIngredientSemantics("du", emptyList())).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `hasExplicitWeight should detect weight and volume units`() {
        assertThat(OcrIngredientUtils.hasExplicitWeight("200 g de farine")).isTrue()
        assertThat(OcrIngredientUtils.hasExplicitWeight("5 cl d'huile")).isTrue()
        assertThat(OcrIngredientUtils.hasExplicitWeight("Sel et poivre")).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `countIngredientSequences should count distinct ingredient sequences`() {
        assertThat(OcrIngredientUtils.countIngredientSequences("2 Carottes 1 Pomme")).isEqualTo(2)
        assertThat(OcrIngredientUtils.countIngredientSequences("2 Carottes")).isEqualTo(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `cleanIngredientSemantics should strip bullets and empty non-letter lines`() {
        assertThat(OcrIngredientUtils.cleanIngredientSemantics("• Tomate", emptyList())).isEqualTo("Tomate")
        assertThat(OcrIngredientUtils.cleanIngredientSemantics("!!!", emptyList())).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should use data driven modifiers and not split size adjectives`() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = OcrIngredientUtils.splitCombinedIngredients(
            "1 petite salade frisée",
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.ingredientSplitterConnectors
        )
        assertThat(result).containsExactly("1 petite salade frisée")
    }
}
