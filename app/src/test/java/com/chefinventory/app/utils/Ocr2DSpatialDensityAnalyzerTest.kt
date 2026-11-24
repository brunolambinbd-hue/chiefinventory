package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que la nouvelle fonction analyzeSpatialDensity
 * mesure la répartition de densité sur 3 tranches (33% W et 66% W) et calcule dynamiquement le nombre de colonnes (1, 2 ou 3).
 */
class Ocr2DSpatialDensityAnalyzerTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeSpatialDensity_SingleColumnRecipe_DetectsOneColumn() {
        val lines1D = listOf(
            OcrTextLine("Gâteau au Chocolat Express", top = 50, left = 100, bottom = 90, right = 500),
            OcrTextLine("Mélangez 200 g de chocolat noir", top = 100, left = 100, bottom = 130, right = 550),
            OcrTextLine("Ajoutez 80 g de farine", top = 140, left = 100, bottom = 170, right = 450),
            OcrTextLine("Faites cuire au four 20 min", top = 180, left = 100, bottom = 210, right = 500)
        )

        val density = Ocr2DSpatialLayoutSorter.analyzeSpatialDensity(lines1D)

        assertWithMessage("Une recette 1 colonne doit être détectée avec columnCount = 1")
            .that(density.columnCount)
            .isEqualTo(1)

        assertWithMessage("Tranche 2 doit valoir 0")
            .that(density.densityCol2)
            .isEqualTo(0)

        assertWithMessage("Tranche 3 doit valoir 0")
            .that(density.densityCol3)
            .isEqualTo(0)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeSpatialDensity_TwoColumnsRecipe_DetectsTwoColumns() {
        val lines2D = listOf(
            OcrTextLine("4 artichauts", top = 249, left = 132, bottom = 271, right = 282),
            OcrTextLine("2 Ld'eau", top = 289, left = 149, bottom = 317, right = 267),
            OcrTextLine("4 C. à soupe de farine", top = 375, left = 77, bottom = 407, right = 342),
            OcrTextLine("VIN CONSEILLĖ", top = 1188, left = 1427, bottom = 1240, right = 1755),
            OcrTextLine("Muscadet de Sèvre-et-Maine", top = 1293, left = 1248, bottom = 1329, right = 1758)
        )

        val density = Ocr2DSpatialLayoutSorter.analyzeSpatialDensity(lines2D)

        assertWithMessage("Une recette 2 colonnes doit être détectée avec columnCount = 2")
            .that(density.columnCount)
            .isEqualTo(2)

        assertWithMessage("Tranche 3 doit contenir le vin (densityCol3 > 0)")
            .that(density.densityCol3)
            .isGreaterThan(0)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeSpatialDensity_ThreeColumnsMelonRecipe_DetectsThreeColumns() {
        val linesMelon = listOf(
            OcrTextLine("I melon Galia", top = 208, left = 175, bottom = 238, right = 337),
            OcrTextLine("8 tranches de jambon", top = 255, left = 116, bottom = 283, right = 382),
            OcrTextLine("• Mixez le porto, le vinaigre", top = 941, left = 686, bottom = 989, right = 1191),
            OcrTextLine("yoghourt, l'huile d'olive", top = 983, left = 682, bottom = 1037, right = 1165),
            OcrTextLine("• Disposez les bûchettes sur", top = 1212, left = 698, bottom = 1263, right = 1140),
            OcrTextLine("VIN CONSEILLE", top = 1169, left = 1454, bottom = 1217, right = 1781),
            OcrTextLine("Atinum, Bordeaux Graves", top = 1228, left = 1396, bottom = 1264, right = 1778)
        )

        val density = Ocr2DSpatialLayoutSorter.analyzeSpatialDensity(linesMelon)

        assertWithMessage("La recette du Melon à 3 colonnes doit être détectée avec columnCount = 3")
            .that(density.columnCount)
            .isEqualTo(3)

        assertWithMessage("Tranche 1 (Gauche) doit être active")
            .that(density.densityCol1)
            .isGreaterThan(0)

        assertWithMessage("Tranche 2 (Centre) doit être active")
            .that(density.densityCol2)
            .isGreaterThan(0)

        assertWithMessage("Tranche 3 (Droite) doit être active")
            .that(density.densityCol3)
            .isGreaterThan(0)
    }
}
