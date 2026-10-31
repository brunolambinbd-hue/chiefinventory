package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

class IngredientSplitterTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplit_CommonItemsExactMatchBypass() {
        val result = IngredientSplitter.splitCombinedIngredients("sel et poivre", listOf("sel et poivre"))
        assertThat(result).containsExactly("sel et poivre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplit_PrefixConnectorEndings() {
        val connectors = listOf("jus de", "zeste d'", "beurre du", "sel des", "sauce au", "fruits aux")
        connectors.forEach { prefix ->
            val result = IngredientSplitter.splitCombinedIngredients("$prefix citron et fraises", listOf("citron", "fraises"))
            assertThat(result).isNotEmpty()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun splitCombinedIngredients_splitsByCommaAndAnd() {
        val result = IngredientSplitter.splitCombinedIngredients("sel, poivre et ail", listOf())
        assertThat(result).containsExactly("sel", "poivre", "ail").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun splitCombinedIngredients_splitsByKeywords() {
        val keywords = listOf("sel", "poivre")
        val result = IngredientSplitter.splitCombinedIngredients("2 tomates poivre rouge", keywords)
        assertThat(result).containsExactly("2 tomates", "poivre rouge").inOrder()
    }
}
