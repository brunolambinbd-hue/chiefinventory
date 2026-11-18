package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3CategorizerRouter] (Aiguilleur Étape 3)
 * vérifiant l'orientations des 4 types de recettes vers leurs lecteurs respectifs.
 */
class Ocr3CategorizerRouterTest {

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
    fun testRouter_OneColumnRecipe_RoutesToClassicOneColumn() {
        val linesWithoutTitle = listOf(
            "INGRÉDIENTS",
            "• 4 tomates",
            "• 2 c. à soupe d'huile",
            "INSTRUCTIONS",
            "• Coupez les tomates en dés."
        )

        val sections = Ocr3CategorizerRouter.routeAndCategorize(linesWithoutTitle, emptyList(), linesWithoutTitle, vocabulary)

        assertThat(sections.rawIngredientsList).containsAtLeast("4 tomates", "2 c. à soupe d'huile")
        assertThat(sections.rawInstructionsList).contains("Coupez les tomates en dés.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRouter_NarrativeRecipe_RoutesToOneColumnOnlyInstructions() {
        val linesWithoutTitle = listOf(
            "Faites revenir 200 g de poulet dans 2 c. à soupe d'huile d'olive pendant 10 min.",
            "Servez chaud."
        )

        val sections = Ocr3CategorizerRouter.routeAndCategorize(linesWithoutTitle, emptyList(), linesWithoutTitle, vocabulary)

        assertThat(sections.rawInstructionsList).hasSize(2)
        assertThat(sections.rawIngredientsList).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateReaderType_IsolatesEvaluationCleanly() {
        val linesWithoutTitle = listOf(
            "INGRÉDIENTS",
            "• 4 tomates",
            "INSTRUCTIONS",
            "• Coupez les tomates."
        )

        val (readerType, _) = Ocr3CategorizerRouter.evaluateReaderType(linesWithoutTitle, emptyList(), vocabulary)
        assertThat(readerType).isEqualTo(Ocr3CategorizerRouter.ReaderType.ONE_COLUMN_INGREDIENT_FIRST)
    }
}
