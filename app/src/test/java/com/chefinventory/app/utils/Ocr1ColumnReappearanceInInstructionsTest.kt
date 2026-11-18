package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation expérimentale (Recette 1 Colonne) reproduisant le bogue de ré-aspiration
 * d'une ligne de suite d'instruction ('dans une petite cocotte, avec 2 c. à soupe d'huile') dans la liste des ingrédients.
 */
class Ocr1ColumnReappearanceInInstructionsTest {

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
    fun testOneColumnRecipeWithSaltPepper_LineSplitInstructionWithQuantity_MustNotReaspirateIntoIngredients() {
        val rawOcrText = """
            SOUPE DE VOLAILLE
            • 200 g de poulet
            • 2 c. à soupe d'huile d'olive
            • Poivre
            • Sel
            Faites revenir le poulet à feu doux dans une poêle
            dans une petite cocotte, avec 2 c. à soupe d'huile d'olive.
            Laissez mijoter 10 min et servez chaud.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)
        val ingredients = result.ingredients?.split("\n") ?: emptyList()
        val instructions = result.instructions?.split("\n") ?: emptyList()

        assertWithMessage("La suite de consigne 'dans une petite cocotte, avec 2 c. à soupe...' doit figurer dans les instructions")
            .that(instructions.any { it.contains("avec 2 c. à soupe", ignoreCase = true) })
            .isTrue()

        assertWithMessage("La suite de consigne 'dans une petite cocotte, avec 2 c. à soupe...' ne doit PAS fuiter dans les ingrédients")
            .that(ingredients.none { it.contains("avec 2 c. à soupe", ignoreCase = true) })
            .isTrue()
    }
}
