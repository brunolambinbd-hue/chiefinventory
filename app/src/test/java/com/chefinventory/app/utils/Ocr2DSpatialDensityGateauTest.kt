package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant la mesure de densité spatiale sur la recette cible 1 colonne 'Gâteau au Chocolat Express'.
 */
class Ocr2DSpatialDensityGateauTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeSpatialDensity_GateauRecipe_DetectsOneColumnLayout() {
        val linesGateau = listOf(
            OcrTextLine("Gâteau au Chocolat Express", top = 50, left = 100, bottom = 90, right = 500),
            OcrTextLine("Mélangez 200 g de chocolat noir fondu avec 100 g de beurre et 3 œufs.", top = 100, left = 100, bottom = 130, right = 600),
            OcrTextLine("Ajoutez 80 g de farine et 100 g de sucre.", top = 140, left = 100, bottom = 170, right = 480),
            OcrTextLine("Faites cuire au four pendant 20 minutes à 180°C.", top = 180, left = 100, bottom = 210, right = 520)
        )

        val density = Ocr2DSpatialLayoutSorter.analyzeSpatialDensity(linesGateau)

        assertWithMessage("La recette du Gâteau au chocolat 1 colonne doit être détectée avec columnCount = 1")
            .that(density.columnCount)
            .isEqualTo(1)

        assertWithMessage("La Tranche 1 (Gauche) doit contenir toutes les lignes")
            .that(density.densityCol1)
            .isEqualTo(4)

        assertWithMessage("La Tranche 2 (Centre) doit être vide")
            .that(density.densityCol2)
            .isEqualTo(0)

        assertWithMessage("La Tranche 3 (Droite) doit être vide")
            .that(density.densityCol3)
            .isEqualTo(0)
    }
}
