package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.IngredientSplitter
import com.example.chiefinventory.utils.OcrHelperUtils
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class IngredientSplitterRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `splitCombinedIngredients should split by punctuation and connectors`() {
        val result = IngredientSplitter.splitCombinedIngredients("sel, poivre et ail", emptyList())
        assertThat(result).containsExactly("sel", "poivre", "ail").inOrder()
    }

    @Test
    fun `splitCombinedIngredients should split using production common ingredients XML`() {
        val result = IngredientSplitter.splitCombinedIngredients("2 tomates poivre rouge", baseVocabulary.commonIngredientsNoQty)
        assertThat(result).containsExactly("2 tomates", "poivre rouge").inOrder()
    }

    @Test
    fun `splitCombinedIngredients should not split short prefix before keyword`() {
        val result = IngredientSplitter.splitCombinedIngredients("1 sel", listOf("sel"))
        assertThat(result).containsExactly("1 sel")
    }

    @Test
    fun `splitCombinedIngredients should not split leading articles before ingredient keywords`() {
        val resultSplitter = IngredientSplitter.splitCombinedIngredients("le jus de 1/2 citron", baseVocabulary.commonIngredientsNoQty)
        assertThat(resultSplitter).containsExactly("le jus de 1/2 citron")

        val resultHelper = OcrHelperUtils.splitCombinedIngredients("le jus de 1/2 citron", baseVocabulary.commonIngredientsNoQty)
        assertThat(resultHelper).containsExactly("le jus de 1/2 citron")
    }
}
