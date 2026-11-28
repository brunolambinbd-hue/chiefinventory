package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3SpatialColumnClusteredGrouper] (Grouping spatial 2D axe X).
 */
class Ocr3SpatialColumnClusteredGrouperTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGroupLinesByXColumns_SeparatesLeftAndRightColumnsCorrectly() {
        val candidates = listOf(
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("• 2 oignons", top = 500, left = 100),
            OcrTextLine("• 20 g de beurre", top = 380, left = 600),
            OcrTextLine("• Sel", top = 580, left = 600)
        )

        val result = Ocr3SpatialColumnClusteredGrouper.groupLinesByXColumns(candidates)

        assertThat(result.columnLeft.map { it.text }).containsExactly("• 1/2 céleri-rave", "• 2 oignons").inOrder()
        assertThat(result.columnRight.map { it.text }).containsExactly("• 20 g de beurre", "• Sel").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGroupLinesByXColumns_EmptyList_ReturnsEmptyResult() {
        val result = Ocr3SpatialColumnClusteredGrouper.groupLinesByXColumns(emptyList())

        assertThat(result.columnLeft).isEmpty()
        assertThat(result.columnRight).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateColumnSignature_QualifiesIngredientsAndInstructionsColumnsCorrectly() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val ingredientLines = listOf(
            OcrTextLine("• 200 g de poulet"),
            OcrTextLine("• 2 c. à soupe d'huile"),
            OcrTextLine("• Sel et poivre")
        )
        val ingrSignature = Ocr3SpatialColumnClusteredGrouper.evaluateColumnSignature(ingredientLines, vocabulary)
        assertThat(ingrSignature.type).isEqualTo(Ocr3SpatialColumnClusteredGrouper.ColumnType.INGREDIENTS)
        assertThat(ingrSignature.ingredientRatio).isAtLeast(0.60f)

        val instructionLines = listOf(
            OcrTextLine("Coupez le poulet en dés."),
            OcrTextLine("Faites revenir 5 min."),
            OcrTextLine("Mélangez le tout.")
        )
        val instSignature = Ocr3SpatialColumnClusteredGrouper.evaluateColumnSignature(instructionLines, vocabulary)
        assertThat(instSignature.type).isEqualTo(Ocr3SpatialColumnClusteredGrouper.ColumnType.INSTRUCTIONS)
        assertThat(instSignature.instructionRatio).isAtLeast(0.60f)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAssembleReordered1DStream_AssemblesColumnLeftThenRightInOrder() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val candidatesLeft = listOf(
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("• 2 oignons", top = 500, left = 100)
        )
        val candidatesRight = listOf(
            OcrTextLine("• 20 g de beurre", top = 380, left = 600),
            OcrTextLine("• Sel", top = 580, left = 600)
        )
        val twoColResult = Ocr3SpatialColumnClusteredGrouper.SpatialTwoColumnsResult(candidatesLeft, candidatesRight)

        val stream = Ocr3SpatialColumnClusteredGrouper.assembleReordered1DStream(twoColResult, vocabulary)

        assertThat(stream).containsExactly("1/2 céleri-rave", "2 oignons", "20 g de beurre", "Sel").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testDataClassesAndAllBranchesCoverage() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        // Enum coverage
        assertThat(Ocr3SpatialColumnClusteredGrouper.ColumnType.entries).hasSize(3)
        assertThat(Ocr3SpatialColumnClusteredGrouper.ColumnType.valueOf("UNKNOWN"))
            .isEqualTo(Ocr3SpatialColumnClusteredGrouper.ColumnType.UNKNOWN)

        // ColumnSignature & SpatialTwoColumnsResult default arguments & data class methods
        val defaultResult = Ocr3SpatialColumnClusteredGrouper.SpatialTwoColumnsResult(emptyList(), emptyList())
        assertThat(defaultResult.leftSignature.type).isEqualTo(Ocr3SpatialColumnClusteredGrouper.ColumnType.UNKNOWN)

        // Single column grouping (rightColCandidate == null)
        val singleColCandidates = listOf(
            OcrTextLine("• 200 g de farine", top = 100, left = 100),
            OcrTextLine("• 2 oeufs", top = 200, left = 110)
        )
        val singleResult = Ocr3SpatialColumnClusteredGrouper.groupLinesByXColumns(singleColCandidates, vocabulary)
        assertThat(singleResult.columnLeft).hasSize(2)
        assertThat(singleResult.leftSignature.type).isEqualTo(Ocr3SpatialColumnClusteredGrouper.ColumnType.INGREDIENTS)

        // Unknown column signature type (ratios < 0.60f)
        val unknownLines = listOf(OcrTextLine("Bonjour"), OcrTextLine("Merci"))
        val unknownSig = Ocr3SpatialColumnClusteredGrouper.evaluateColumnSignature(unknownLines, vocabulary)
        assertThat(unknownSig.type).isEqualTo(Ocr3SpatialColumnClusteredGrouper.ColumnType.UNKNOWN)

        // Instruction start verbs branch in evaluateColumnSignature
        val startVerbLines = listOf(
            OcrTextLine("Pelez le melon"),
            OcrTextLine("Cuire à feu doux")
        )
        val startVerbSig = Ocr3SpatialColumnClusteredGrouper.evaluateColumnSignature(startVerbLines, vocabulary)
        assertThat(startVerbSig.type).isEqualTo(Ocr3SpatialColumnClusteredGrouper.ColumnType.INSTRUCTIONS)

        // assembleReordered1DStream with bullet only noise line
        val noiseLeft = listOf(OcrTextLine("• ", top = 100, left = 100))
        val noiseRight = listOf(OcrTextLine("- ", top = 100, left = 500))
        val noiseResult = Ocr3SpatialColumnClusteredGrouper.SpatialTwoColumnsResult(noiseLeft, noiseRight)
        val noiseStream = Ocr3SpatialColumnClusteredGrouper.assembleReordered1DStream(noiseResult, vocabulary)
        assertThat(noiseStream).isEmpty()
    }
}
