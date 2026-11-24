package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant la mesure de densité spatiale sur la recette cible 2 colonnes 'Artichauts et vinaigrette aux échalotes'.
 */
class Ocr2DSpatialDensityArtichautsTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeSpatialDensity_ArtichautsRecipe_DetectsTwoColumnsLayout() {
        val linesArtichauts = listOf(
            OcrTextLine("INGRÉDIENTS", top = 122, left = 66, bottom = 156, right = 339),
            OcrTextLine("4 artichauts", top = 249, left = 132, bottom = 271, right = 282),
            OcrTextLine("2 Ld'eau", top = 289, left = 149, bottom = 317, right = 267),
            OcrTextLine("4 C. à soupe de farine", top = 375, left = 77, bottom = 407, right = 342),
            OcrTextLine("• Mélangez la farine avec un", top = 990, left = 61, bottom = 1037, right = 536),
            OcrTextLine("VIN CONSEILLĖ", top = 1188, left = 1427, bottom = 1240, right = 1755),
            OcrTextLine("Muscadet de Sèvre-et-Maine sur Lie", top = 1293, left = 1248, bottom = 1329, right = 1758)
        )

        val density = Ocr2DSpatialLayoutSorter.analyzeSpatialDensity(linesArtichauts)

        assertWithMessage("La recette des Artichauts 2 colonnes doit être détectée avec columnCount = 2")
            .that(density.columnCount)
            .isEqualTo(2)

        assertWithMessage("La Tranche 1 (Gauche) doit être active")
            .that(density.densityCol1)
            .isGreaterThan(0)

        assertWithMessage("La Tranche 3 (Droite/Vin) doit être active")
            .that(density.densityCol3)
            .isGreaterThan(0)
    }
}
