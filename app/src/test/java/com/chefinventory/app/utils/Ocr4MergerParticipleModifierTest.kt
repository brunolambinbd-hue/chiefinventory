package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la fusion des qualificatifs participes passés culinaires en '-é/-ée' dans [Ocr4Merger].
 */
class Ocr4MergerParticipleModifierTest {

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
    fun testMergeIngredients_MergesPastParticipleQualifierOnNextLine() {
        val rawIngredients = listOf(
            "8 c. à soupe de yaourt",
            "demi-écrémé sucré"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients = rawIngredients,
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            commonIngredients = vocabulary.commonIngredientsNoQty
        )

        assertWithMessage("Le qualificatif 'demi-écrémé sucré' doit être fusionné avec la ligne précédente '8 c. à soupe de yaourt'")
            .that(merged)
            .containsExactly("8 c. à soupe de yaourt demi-écrémé sucré")
    }
}
