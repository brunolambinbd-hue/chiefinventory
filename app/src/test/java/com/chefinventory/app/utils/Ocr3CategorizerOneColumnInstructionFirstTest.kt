package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3CategorizerOneColumnInstructionFirst] (Lecteur 4 : 1 colonne, instructions en premier).
 */
class Ocr3CategorizerOneColumnInstructionFirstTest {

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
    fun testOneColumnInstructionFirst_SeparatesTopInstructionsAndBottomIngredients() {
        val lines = listOf(
            "Faites revenir le poulet à feu doux pendant 10 min.",
            "Servez chaud.",
            "POUR 4 PERSONNES",
            "200 g de poulet",
            "2 c. à soupe d'huile"
        )

        val isInverted = Ocr3CategorizerOneColumnInstructionFirst.isInvertedRecipe(lines, vocabulary)
        assertThat(isInverted).isTrue()

        val sections = Ocr3CategorizerOneColumnInstructionFirst.categorizeInvertedRecipe(lines, vocabulary)
        assertThat(sections.rawInstructionsList).contains("Faites revenir le poulet à feu doux pendant 10 min.")
        assertThat(sections.rawIngredientsList).contains("200 g de poulet")
    }
}
