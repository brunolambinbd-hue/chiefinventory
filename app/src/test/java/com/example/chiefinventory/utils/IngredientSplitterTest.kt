package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IngredientSplitterTest {

    @Test
    fun splitCombinedIngredients_splitsByCommaAndAnd() {
        val result = IngredientSplitter.splitCombinedIngredients("sel, poivre et ail", listOf())
        assertThat(result).containsExactly("sel", "poivre", "ail").inOrder()
    }

    @Test
    fun splitCombinedIngredients_splitsByKeywords() {
        val keywords = listOf("sel", "poivre")
        val result = IngredientSplitter.splitCombinedIngredients("2 tomates poivre rouge", keywords)
        assertThat(result).containsExactly("2 tomates", "poivre rouge").inOrder()
    }
}
