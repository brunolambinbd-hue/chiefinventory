package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de l'Étape 4 (Merger) vérifiant que l'en-tête "Votre marché"
 * n'est pas présent dans la liste transmise et n'est pas fusionné.
 */
class OcrVotreMarchePhase4Test {

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
    fun testPhase4_MergeIngredients_PreservesCleanIngredientsWithoutHeader() {
        val rawIngredients = listOf(
            "4 oignons",
            "50 g de beurre"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )

        assertWithMessage("La fusion d'Étape 4 doit conserver uniquement les ingrédients propres")
            .that(merged)
            .containsExactly("4 oignons", "50 g de beurre")
            .inOrder()
    }
}
