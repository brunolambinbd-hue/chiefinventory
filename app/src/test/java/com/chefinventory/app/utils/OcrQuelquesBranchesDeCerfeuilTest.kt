package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que 'quelques branches' et 'de cerfeuil' sont fusionnées sur 1 seule ligne unifiée ([Ocr4Merger]).
 */
class OcrQuelquesBranchesDeCerfeuilTest {

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
    fun testMergeIngredients_PreservesQuelquesBranchesDeCerfeuilOnSingleLine() {
        val rawIngredients = listOf(
            "quelques branches",
            "de cerfeuil"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients = rawIngredients,
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            commonIngredients = vocabulary.commonIngredientsNoQty,
            mandatoryComplements = vocabulary.mandatoryComplementIngredients,
            customConnectors = vocabulary.ingredientConnectors,
            offTaxonomyList = listOf("cerfeuil", "branches")
        )

        assertWithMessage("La ligne 'quelques branches' et 'de cerfeuil' doit être fusionnée sur 1 seule ligne unifiée")
            .that(merged)
            .containsExactly("quelques branches de cerfeuil")
    }
}
