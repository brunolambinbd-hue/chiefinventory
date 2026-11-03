package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

class IngredientSplitterTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsValidSplitPrefix_AllBranches() {
        // 1. Prefix in ARTICLES -> false
        assertThat(IngredientSplitter.isValidSplitPrefix("de")).isFalse()
        assertThat(IngredientSplitter.isValidSplitPrefix("du")).isFalse()

        // 2. Prefix matches SIZE_PREFIX_REGEX -> false
        assertThat(IngredientSplitter.isValidSplitPrefix("1 petite")).isFalse()
        assertThat(IngredientSplitter.isValidSplitPrefix("1/2 grand")).isFalse()

        // 3. Prefix ends with connector -> false
        assertThat(IngredientSplitter.isValidSplitPrefix("gousses de")).isFalse()
        assertThat(IngredientSplitter.isValidSplitPrefix("gousses d'")).isFalse()
        assertThat(IngredientSplitter.isValidSplitPrefix("gousses du")).isFalse()
        assertThat(IngredientSplitter.isValidSplitPrefix("gousses des")).isFalse()
        assertThat(IngredientSplitter.isValidSplitPrefix("gousses au")).isFalse()
        assertThat(IngredientSplitter.isValidSplitPrefix("gousses aux")).isFalse()

        // 4. Prefix ends with modifier -> false
        assertThat(IngredientSplitter.isValidSplitPrefix("haché petit")).isFalse()
        assertThat(IngredientSplitter.isValidSplitPrefix("haché grande")).isFalse()

        // 5. Valid prefix -> true
        assertThat(IngredientSplitter.isValidSplitPrefix("2 gousses d'ail")).isTrue()
    }

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
    fun testSplit_TrailingDelimiterAndDuplicates() {
        val result1 = IngredientSplitter.splitCombinedIngredients("sel, poivre,", emptyList())
        assertThat(result1).containsExactly("sel", "poivre")

        val result2 = IngredientSplitter.splitCombinedIngredients("sel, sel", emptyList())
        assertThat(result2).containsExactly("sel")
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
