package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant le maintien en section INSTRUCTIONS
 * lorsqu'une consigne d'instruction se termine par une parenthèse ouvrante non fermée '(Ajoutez un peu'.
 */
class OcrUnclosedInstructionParenthesisTest {

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
    fun testUnclosedInstructionParenthesis_KeepsNextLineInInstructionsAndMergesSentence() {
        val lines = listOf(
            "SALADE DE QUINOA",
            "INGRÉDIENTS",
            "• 150 g de quinoa",
            "• 2 c. à soupe d'huile",
            "INSTRUCTIONS",
            "• Faites cuire 10 min jusqu'à ce que l'eau se soit évaporée (Ajoutez un peu",
            "d'eau si nécessaire)",
            "• Servez bien chaud."
        )

        val result = RecipeOcrParser.parse(lines.joinToString("\n"), vocabulary)
        val ingredients = result.ingredients?.split("\n") ?: emptyList()
        val instructions = result.instructions?.split("\n") ?: emptyList()

        assertWithMessage("La suite de la parenthèse 'd'eau si nécessaire)' ne doit PAS fuiter dans les ingrédients")
            .that(ingredients.none { it.contains("nécessaire", ignoreCase = true) })
            .isTrue()

        assertWithMessage("La phrase d'instruction avec parenthèse doit être fusionnée en une seule consigne fluide")
            .that(instructions.any { it.contains("évaporée (Ajoutez un peu d'eau si nécessaire)", ignoreCase = true) })
            .isTrue()
    }
}
