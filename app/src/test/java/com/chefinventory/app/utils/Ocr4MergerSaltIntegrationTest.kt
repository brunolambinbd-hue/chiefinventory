package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'intégration de niveau B (Étape 4 Merger) vérifiant que le helper du sel
 * [Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin] est correctement connecté dans [Ocr4Merger.mergeIngredients] :
 * - Autorise la fusion de 'sel' + 'de Guérande' -> 'sel de Guérande'.
 * - Refuse la fusion parasite de 'sel' avec 'de jus d'orange...'.
 */
class Ocr4MergerSaltIntegrationTest {

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
    fun testMergeIngredients_ValidSaltOrigin_AllowsMerge() {
        val inputGuerande = listOf("sel", "de Guérande")
        val mergedGuerande = Ocr4Merger.mergeIngredients(
            inputGuerande,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )
        assertWithMessage("'sel' + 'de Guérande' doit fusionner en 'sel de Guérande'")
            .that(mergedGuerande)
            .containsExactly("sel de Guérande")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_InvalidSaltOrigin_RefusesMerge() {
        val inputInvalid = listOf("sel", "de jus d'orange et une pincée de sel.")
        val mergedInvalid = Ocr4Merger.mergeIngredients(
            inputInvalid,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )
        assertWithMessage("'sel' + 'de jus d'orange...' ne doit PAS fusionner et doit rester séparé")
            .that(mergedInvalid)
            .containsExactly("sel", "de jus d'orange et une pincée de sel.")
            .inOrder()
    }
}
