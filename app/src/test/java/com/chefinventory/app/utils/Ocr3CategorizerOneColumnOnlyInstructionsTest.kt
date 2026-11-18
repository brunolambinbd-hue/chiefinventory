package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3CategorizerOneColumnOnlyInstructions] (Lecteur 3 : 1 colonne, instructions seules).
 */
class Ocr3CategorizerOneColumnOnlyInstructionsTest {

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
    fun testOneColumnOnlyInstructions_ExtractsImpliedIngredientsForPantry() {
        val lines = listOf(
            "Faites revenir 200 g de poulet dans 2 c. à soupe d'huile d'olive pendant 10 min.",
            "Servez bien chaud."
        )

        val isNarrative = Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(lines, vocabulary)
        assertThat(isNarrative).isTrue()

        val sections = Ocr3CategorizerOneColumnOnlyInstructions.categorizeNarrativeRecipe(lines, vocabulary)
        assertThat(sections.rawInstructionsList).hasSize(2)
        assertThat(sections.rawIngredientsList).isNotEmpty()
    }
}
