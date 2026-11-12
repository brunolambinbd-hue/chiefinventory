package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau B (Étape 4 Merger) vérifiant que le complément d'ingrédient 'sauce'
 * est fusionné avec 'sweet chili' pour former '2 c. à soupe de sweet chili sauce',
 * et que le modificateur 'fraîche hachée' est fusionné avec 'coriandre'.
 */
class Ocr4MergerSauceTest {

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
    fun testMergeIngredients_MergesSweetChiliWithSauceAndCoriandreWithFraicheHachee() {
        val rawIngredients = listOf(
            "2 c. à soupe de sweet chili",
            "sauce",
            "1 c. à soupe de coriandre",
            "fraîche hachée"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )

        assertWithMessage("La ligne '2 c. à soupe de sweet chili' doit fusionner avec 'sauce'")
            .that(merged)
            .contains("2 c. à soupe de sweet chili sauce")

        assertWithMessage("La ligne '1 c. à soupe de coriandre' doit fusionner avec 'fraîche hachée'")
            .that(merged)
            .contains("1 c. à soupe de coriandre fraîche hachée")
    }
}
