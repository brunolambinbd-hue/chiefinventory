package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 2.5, le trieur spatial Ocr2DSpatialLayoutSorter
 * réordonne géométriquement et fusionne horizontalement les fragments d'une même ligne 2D ('2 c.' + 'à soupe d'eau' -> '2 c. à soupe d'eau').
 */
class Ocr2DSpatialFragmentFusionTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSortSpatially_FragmentedHorizontalLine_FusesAndOrdersInCorrectReadingOrder() {
        val lines2D = listOf(
            OcrTextLine("2 c.", top = 701, left = 122, bottom = 731, right = 147),
            OcrTextLine("4 c. à soupe de vinaigre de", top = 619, left = 51, bottom = 655, right = 378),
            OcrTextLine("vin blanc", top = 656, left = 162, bottom = 683, right = 272),
            OcrTextLine("à soupe d'eau", top = 697, left = 173, bottom = 730, right = 327)
        )

        val sortedAndFused = Ocr2DSpatialLayoutSorter.sortSpatially(lines2D)
        val sortedTexts = sortedAndFused.map { it.text }

        assertWithMessage("Le trieur spatial 2D doit fusionner '2 c.' et 'à soupe d'eau' et ordonner les lignes du haut vers le bas")
            .that(sortedTexts)
            .containsExactly(
                "4 c. à soupe de vinaigre de",
                "vin blanc",
                "2 c. à soupe d'eau"
            )
            .inOrder()
    }
}
