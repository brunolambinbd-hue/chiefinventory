package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3CategorizerTwoColumnsIngredientFirst] (Lecteur 2 : 2 colonnes d'ingrédients, positionnement 2D).
 */
class Ocr3CategorizerTwoColumnsIngredientFirstTest {

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
    fun testTwoColumnsIngredientFirst_ReordersSpatiallyAndExtractsAllIngredients() {
        val lines2D = listOf(
            OcrTextLine("SOUPE AU CÉLERI", top = 50, left = 100),
            // Colonne 1 (Gauche, left = 100)
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("• 2 oignons", top = 500, left = 100),
            // Colonne 2 (Droite, left = 600)
            OcrTextLine("• 20 g de beurre", top = 380, left = 600),
            OcrTextLine("• Sel", top = 580, left = 600),
            // Instructions (top = 1200)
            OcrTextLine("Épluchez le céleri-rave", top = 1200, left = 100)
        )

        val results = RawSections()
        val reordered = Ocr3CategorizerTwoColumnsIngredientFirst.reorder2DLines(lines2D, vocabulary, results)
        val sections = Ocr3CategorizerOneColumnIngredientFirst.categorize(reordered, vocabulary)

        assertWithMessage("Les ingrédients de la Colonne 1 (céleri-rave) doivent précéder les ingrédients de la Colonne 2 (beurre)")
            .that(sections.rawIngredientsList.indexOfFirst { it.contains("céleri") })
            .isLessThan(sections.rawIngredientsList.indexOfFirst { it.contains("beurre") })
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFindInstructionTopLimit_FindsFirstActionVerbTop() {
        val lines2D = listOf(
            OcrTextLine("SOUPE AU CÉLERI", top = 50, left = 100),
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("Épluchez le céleri-rave", top = 1200, left = 100)
        )
        val results = RawSections()
        val limit = Ocr3CategorizerTwoColumnsIngredientFirst.findInstructionTopLimit(lines2D, vocabulary, results)

        assertWithMessage("La limite d'instruction Y doit être fixée à 1200 (top de 'Épluchez')")
            .that(limit)
            .isEqualTo(1200)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractIngredientCandidates_FiltersLinesAboveInstructionLimit() {
        val lines2D = listOf(
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("• 2 oignons", top = 500, left = 100),
            OcrTextLine("Épluchez le céleri-rave", top = 1200, left = 100)
        )
        val results = RawSections()
        val candidates = Ocr3CategorizerTwoColumnsIngredientFirst.extractIngredientCandidates(lines2D, 1200, vocabulary, results)

        assertWithMessage("Seules les 2 lignes sous la limite d'instruction doivent être candidates")
            .that(candidates)
            .hasSize(2)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSortTwoColumnsSpatially_SortsColumn1ThenColumn2() {
        val candidates = listOf(
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("• 20 g de beurre", top = 380, left = 600)
        )
        val reordered = Ocr3CategorizerTwoColumnsIngredientFirst.sortTwoColumnsSpatially(candidates)

        assertWithMessage("La Colonne 1 (left=100) doit précéder la Colonne 2 (left=600)")
            .that(reordered)
            .containsExactly("• 1/2 céleri-rave", "• 20 g de beurre")
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSortTwoColumnsSpatiallyByClusterAndSignature_SortsColumn1ThenColumn2() {
        val candidates = listOf(
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("• 20 g de beurre", top = 380, left = 600)
        )
        val reordered = Ocr3CategorizerTwoColumnsIngredientFirst.sortTwoColumnsSpatiallyByClusterAndSignature(candidates)

        assertWithMessage("La Colonne 1 (left=100) doit précéder la Colonne 2 (left=600)")
            .that(reordered)
            .containsExactly("• 1/2 céleri-rave", "• 20 g de beurre")
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractInstructionLines_CollectsLinesBelowInstructionLimit() {
        val lines2D = listOf(
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("Épluchez le céleri-rave", top = 1200, left = 100)
        )
        val instructions = Ocr3CategorizerTwoColumnsIngredientFirst.extractInstructionLines(lines2D, 1200, vocabulary)

        assertWithMessage("Seule la ligne à top >= 1170 (Épluchez) doit être extraite comme instruction")
            .that(instructions)
            .containsExactly("Épluchez le céleri-rave")
    }
}
