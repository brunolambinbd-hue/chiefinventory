package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que 'Une petite pointe de cinq' et 'Épices chinoises' restent fusionnés sur 1 seule ligne ([Ocr4Merger]).
 */
class OcrPointeDeCinqEpicesMergeTest {

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
    fun testMergeIngredients_PreservesPointeDeCinqEpicesOnSingleLine() {
        val rawIngredients = listOf(
            "Une petite pointe de cinq",
            "Épices chinoises"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients = rawIngredients,
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            commonIngredients = vocabulary.commonIngredientsNoQty,
            mandatoryComplements = vocabulary.mandatoryComplementIngredients,
            customConnectors = vocabulary.ingredientConnectors,
            offTaxonomyList = listOf("épices chinoises", "épices")
        )

        assertWithMessage("La ligne 'Une petite pointe de cinq' et 'Épices chinoises' doit rester fusionnée sur 1 seule ligne")
            .that(merged)
            .containsExactly("Une petite pointe de cinq Épices chinoises")
    }
}
