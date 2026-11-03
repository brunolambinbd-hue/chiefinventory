package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.Ocr4MergerIngredientSplitter
import com.chefinventory.app.utils.OcrHelperUtils
import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

class Ocr4MergerIngredientSplitterRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should split by punctuation and connectors`() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("sel, poivre et ail", emptyList())
        assertThat(result).containsExactly("sel", "poivre", "ail").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should split using production common ingredients XML`() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("2 tomates poivre rouge", baseVocabulary.commonIngredientsNoQty)
        assertThat(result).containsExactly("2 tomates", "poivre rouge").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should not split short prefix before keyword`() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("1 sel", listOf("sel"))
        assertThat(result).containsExactly("1 sel")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should not split leading articles before ingredient keywords`() {
        val resultSplitter = Ocr4MergerIngredientSplitter.splitCombinedIngredients("le jus de 1/2 citron", baseVocabulary.commonIngredientsNoQty)
        assertThat(resultSplitter).containsExactly("le jus de 1/2 citron")

        val resultHelper = OcrHelperUtils.splitCombinedIngredients("le jus de 1/2 citron", baseVocabulary.commonIngredientsNoQty)
        assertThat(resultHelper).containsExactly("le jus de 1/2 citron")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should not split size adjective petite before salade frisee`() {
        val resultSplitter = Ocr4MergerIngredientSplitter.splitCombinedIngredients("1 petite salade frisée", baseVocabulary.commonIngredientsNoQty)
        assertThat(resultSplitter).containsExactly("1 petite salade frisée")

        val resultHelper = OcrHelperUtils.splitCombinedIngredients("1 petite salade frisée", baseVocabulary.commonIngredientsNoQty)
        assertThat(resultHelper).containsExactly("1 petite salade frisée")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should split sel et poivre when following vinaigre`() {
        val result = OcrHelperUtils.splitCombinedIngredients("vinaigre sel et poivre", baseVocabulary.commonIngredientsNoQty)
        assertThat(result).containsExactly("vinaigre", "sel et poivre").inOrder()
    }
}
