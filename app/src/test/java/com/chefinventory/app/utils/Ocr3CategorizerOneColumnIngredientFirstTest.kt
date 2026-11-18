package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3CategorizerOneColumnIngredientFirst] (Lecteur 1 : 1 colonne, ingrédients d'abord).
 */
class Ocr3CategorizerOneColumnIngredientFirstTest {

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
    fun testOneColumnIngredientFirst_ExtractsIngredientsThenInstructions() {
        val lines = listOf(
            "SALADE DE TOMATES",
            "INGRÉDIENTS",
            "• 4 tomates",
            "• 2 c. à soupe d'huile",
            "INSTRUCTIONS",
            "• Coupez les tomates en dés.",
            "• Mélangez avec l'huile."
        )

        val sections = Ocr3CategorizerOneColumnIngredientFirst.categorize(lines, vocabulary)

        assertThat(sections.rawIngredientsList).containsAtLeast("4 tomates", "2 c. à soupe d'huile")
        assertThat(sections.rawInstructionsList).containsExactly("Coupez les tomates en dés.", "Mélangez avec l'huile.").inOrder()
    }
}
