package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 3, une ligne se terminant par une césure ('lan-') fusionne immédiatement la ligne suivante ('goustines d'un peu de sauce au curry.') dans les instructions.
 */
class Ocr3HyphenationContinuationTest {

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
    fun testCategorize_HyphenatedInstructionLine_FusesContinuationLineWithoutStealingToIngredients() {
        val lines = listOf(
            "Disposez une tête de langoustine sur chaque assiette, nappez les lan-",
            "goustines d'un peu de sauce au curry."
        )

        val categorisationResult = Ocr3CategorizerOneColumnInstructionFirst.categorizeInvertedRecipe(lines, vocabulary, lines)

        assertWithMessage("Les ingrédients ne doivent pas voler le morceau de mot césuré 'goustines d'un peu de sauce au curry.'")
            .that(categorisationResult.rawIngredientsList)
            .isEmpty()

        val mergedInstructions = Ocr4Merger.mergeInstructions(categorisationResult.rawInstructionsList)

        assertWithMessage("L'instruction césurée doit être fusionnée en Étape 4 en 1 seule ligne d'instruction complète")
            .that(mergedInstructions)
            .containsExactly("Disposez une tête de langoustine sur chaque assiette, nappez les langoustines d'un peu de sauce au curry.")
    }
}
