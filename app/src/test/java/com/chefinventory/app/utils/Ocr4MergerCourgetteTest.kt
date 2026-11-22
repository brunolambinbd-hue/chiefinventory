package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la scission d'ingrédients combinés à l'Étape 4 pour le mot 'courgette' via la liste OFF de Room DB ([Ocr4MergerIngredientSplitter]).
 */
class Ocr4MergerCourgetteTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitCombinedIngredients_SplitsLineContainingCourgetteUsingOffTaxonomyList() {
        val line = "200 g de saumon 2 courgettes"
        val offTaxonomy = listOf("courgette", "courgettes", "saumon")

        val result = Ocr4MergerIngredientSplitter.splitCombinedIngredients(
            line = line,
            commonItems = vocabulary.commonIngredientsNoQty,
            offTaxonomyList = offTaxonomy
        )

        assertWithMessage("La ligne combinée '200 g de saumon 2 courgettes' doit être scindée même sans 'courgette' dans le fichier XML")
            .that(result)
            .containsExactly("200 g de saumon", "2 courgettes")
    }
}
