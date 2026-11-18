package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau Étape 3 ([Ocr3SpatialLineReorderer])
 * vérifiant le ré-ordonnancement spatial 2D des colonnes d'ingrédients (Colonne 1 Gauche, Colonne 2 Droite)
 * et la détection dynamique de la frontière d'altitude du premier verbe d'instruction.
 */
class Ocr3SpatialLineReordererTest {

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
    fun testReorder2DLines_TwoColumns_ReordersColumn1ThenColumn2ThenInstructions() {
        val lines2D = listOf(
            OcrTextLine("SOUPE AU CÉLERI", top = 50, left = 100),
            OcrTextLine("15 MIN FACILE", top = 120, left = 100),
            // Colonne 1 (Gauche, left = 100)
            OcrTextLine("• 1/2 céleri-rave", top = 400, left = 100),
            OcrTextLine("• 2 oignons", top = 500, left = 100),
            OcrTextLine("• 100 g de roquefort", top = 600, left = 100),
            // Colonne 2 (Droite, left = 600)
            OcrTextLine("• 20 g de beurre", top = 380, left = 600),
            OcrTextLine("• 2 cubes de bouillon", top = 480, left = 600),
            OcrTextLine("• Sel", top = 580, left = 600),
            // Instructions (Pleine largeur, top = 1200)
            OcrTextLine("Épluchez le céleri-rave et coupez-le en dés", top = 1200, left = 100)
        )

        val results = RawSections()
        val reordered = Ocr3SpatialLineReorderer.reorder2DLines(lines2D, vocabulary, results)

        val celerIdx = reordered.indexOfFirst { it.contains("céleri-rave") }
        val beurreIdx = reordered.indexOfFirst { it.contains("beurre") }
        val epluchezIdx = reordered.indexOfFirst { it.contains("Épluchez") }

        assertWithMessage("Les ingrédients de la Colonne 1 (céleri-rave) doivent précéder les ingrédients de la Colonne 2 (beurre)")
            .that(celerIdx)
            .isLessThan(beurreIdx)

        assertWithMessage("Les ingrédients de la Colonne 2 (beurre) doivent précéder la première instruction (Épluchez)")
            .that(beurreIdx)
            .isLessThan(epluchezIdx)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testReorder2DLines_EmptyInput_ReturnsEmptyList() {
        val results = RawSections()
        val reordered = Ocr3SpatialLineReorderer.reorder2DLines(emptyList(), vocabulary, results)
        assertThat(reordered).isEmpty()
    }
}
