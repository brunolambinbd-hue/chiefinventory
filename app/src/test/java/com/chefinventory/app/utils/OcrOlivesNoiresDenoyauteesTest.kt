package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que '10 olives noires dénoyautées' ne sont PAS séparées sur deux lignes ([Ocr4Merger]).
 */
class OcrOlivesNoiresDenoyauteesTest {

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
    fun testMergeIngredients_PreservesOlivesNoiresDenoyauteesOnSingleLine() {
        val rawIngredients = listOf(
            "10 olives",
            "noires dénoyautées"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients = rawIngredients,
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            commonIngredients = vocabulary.commonIngredientsNoQty,
            mandatoryComplements = vocabulary.mandatoryComplementIngredients,
            customConnectors = vocabulary.ingredientConnectors,
            offTaxonomyList = listOf("olives", "noires", "olives noires")
        )

        assertWithMessage("La ligne '10 olives noires dénoyautées' doit être fusionnée sur 1 seule ligne unifiée")
            .that(merged)
            .containsExactly("10 olives noires dénoyautées")
    }
}
