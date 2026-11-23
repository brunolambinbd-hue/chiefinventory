package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que '4 nouvelles pommes de terre' reste sur 1 seule ligne et que 'nouvelles' n'est jamais transformé par Levenshtein en 'nouilles'.
 */
class OcrPommesDeTerreNouvellesTest {

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
    fun testMergeIngredients_PreservesPommesDeTerreNouvellesOnSingleLineAndWithoutNouilles() {
        val rawIngredients = listOf(
            "4 nouvelles pommes de",
            "terre"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients = rawIngredients,
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            commonIngredients = vocabulary.commonIngredientsNoQty,
            mandatoryComplements = vocabulary.mandatoryComplementIngredients,
            customConnectors = vocabulary.ingredientConnectors,
            offTaxonomyList = listOf("nouilles", "pommes de terre")
        )

        assertWithMessage("La ligne '4 nouvelles pommes de terre' doit être fusionnée sur 1 seule ligne unifiée")
            .that(merged)
            .containsExactly("4 nouvelles pommes de terre")

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(merged, offTaxonomyList = listOf("nouilles", "pommes de terre"))

        assertWithMessage("L'adjectif 'nouvelles' de '4 nouvelles pommes de terre' ne doit JAMAIS être dévié par Levenshtein en 'nouilles'")
            .that(beautified)
            .containsExactly("4 nouvelles pommes de terre")
    }
}
