package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que '120 g de surimi (poisson au goût de crabe) ou de véritable' et 'crabe' ne sont PAS séparés ([Ocr4Merger]).
 */
class OcrOuDeVeritableCrabeMergeTest {

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
    fun testMergeIngredients_PreservesOuDeVeritableCrabeOnSingleLine() {
        val rawIngredients = listOf(
            "120 g de surimi (poisson au goût de crabe) ou de véritable",
            "crabe"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients = rawIngredients,
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            commonIngredients = vocabulary.commonIngredientsNoQty,
            mandatoryComplements = vocabulary.mandatoryComplementIngredients,
            customConnectors = vocabulary.ingredientConnectors,
            offTaxonomyList = listOf("crabe", "surimi")
        )

        assertWithMessage("La ligne '120 g de surimi (poisson au goût de crabe) ou de véritable' et 'crabe' ne doit PAS être découpée avant crabe")
            .that(merged)
            .containsExactly("120 g de surimi (poisson au goût de crabe) ou de véritable crabe")
    }
}
