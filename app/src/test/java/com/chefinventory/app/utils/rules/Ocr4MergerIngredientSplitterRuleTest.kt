package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.Ocr4MergerIngredientSplitter
import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertWithMessage
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
    fun `splitCombinedIngredients should split using production common ingredients XML`() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("2 tomates poivre rouge", baseVocabulary.commonIngredientsNoQty)
        assertWithMessage("La ligne '2 tomates poivre rouge' doit être scindée en '2 tomates' et 'poivre rouge'")
            .that(result)
            .containsExactly("2 tomates", "poivre rouge")
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should not split short prefix before keyword`() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("1 sel", listOf("sel"))
        assertWithMessage("La quantité '1' ne doit pas être séparée du mot 'sel'")
            .that(result)
            .containsExactly("1 sel")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should not split leading articles before ingredient keywords`() {
        val resultSplitter = Ocr4MergerIngredientSplitter.splitCombinedIngredients("le jus de 1/2 citron", baseVocabulary.commonIngredientsNoQty)
        assertWithMessage("Le préfixe 'le jus de' ne doit pas être séparé de '1/2 citron'")
            .that(resultSplitter)
            .containsExactly("le jus de 1/2 citron")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should not split size adjective petite before salade frisee`() {
        val resultSplitter = Ocr4MergerIngredientSplitter.splitCombinedIngredients("1 petite salade frisée", baseVocabulary.commonIngredientsNoQty)
        assertWithMessage("L'adjectif de taille 'petite' ne doit pas être séparé de 'salade frisée'")
            .that(resultSplitter)
            .containsExactly("1 petite salade frisée")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `splitCombinedIngredients should split sel et poivre when following vinaigre`() {
        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients("vinaigre sel et poivre", baseVocabulary.commonIngredientsNoQty)
        assertWithMessage("L'assaisonnement 'sel et poivre' doit être séparé de 'vinaigre'")
            .that(result)
            .containsExactly("vinaigre", "sel et poivre")
            .inOrder()
    }
}
