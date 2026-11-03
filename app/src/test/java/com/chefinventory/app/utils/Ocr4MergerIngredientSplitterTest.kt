package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

class Ocr4MergerIngredientSplitterTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsValidSplitPrefix_AllBranches() {
        // 1. Prefix in ARTICLES -> false
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("de")).isFalse()
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("du")).isFalse()

        // 2. Prefix matches SIZE_PREFIX_REGEX -> false
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("1 petite")).isFalse()
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("1/2 grand")).isFalse()

        // 3. Prefix ends with connector -> false
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("gousses de")).isFalse()
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("gousses d'")).isFalse()
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("gousses du")).isFalse()
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("gousses des")).isFalse()
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("gousses au")).isFalse()
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("gousses aux")).isFalse()

        // 4. Prefix ends with modifier -> false
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("haché petit")).isFalse()
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("haché grande")).isFalse()

        // 5. Valid prefix -> true
        assertThat(Ocr4MergerIngredientSplitter.isValidSplitPrefix("2 gousses d'ail")).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplit_CommonItemsExactMatchBypass() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("sel et poivre", listOf("sel et poivre"))
        assertThat(result).containsExactly("sel et poivre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplit_PrefixConnectorEndings() {
        val connectors = listOf("jus de", "zeste d'", "beurre du", "sel des", "sauce au", "fruits aux")
        connectors.forEach { prefix ->
            val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("$prefix citron et fraises", listOf("citron", "fraises"))
            assertThat(result).isNotEmpty()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplit_TrailingDelimiterAndDuplicates() {
        val result1 = Ocr4MergerIngredientSplitter.splitCombinedIngredients("sel, poivre,", emptyList())
        assertThat(result1).containsExactly("sel", "poivre")

        val result2 = Ocr4MergerIngredientSplitter.splitCombinedIngredients("sel, sel", emptyList())
        assertThat(result2).containsExactly("sel")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun splitCombinedIngredients_splitsByCommaAndAnd() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("sel, poivre et ail", listOf())
        assertThat(result).containsExactly("sel", "poivre", "ail").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun splitCombinedIngredients_splitsByKeywords() {
        val keywords = listOf("sel", "poivre")
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("2 tomates poivre rouge", keywords)
        assertThat(result).containsExactly("2 tomates", "poivre rouge").inOrder()
    }
}
