package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant la mesure de densité spatiale sur la recette cible 3 colonnes 'Bûchettes de melon au jambon italien et au porto'.
 */
class Ocr2DSpatialDensityMelonTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeSpatialDensity_MelonRecipe_DetectsThreeColumnsLayout() {
        val linesMelon = listOf(
            OcrTextLine("INGRÉDIENTS", top = 86, left = 114, bottom = 122, right = 380),
            OcrTextLine("I melon Galia", top = 208, left = 175, bottom = 238, right = 337),
            OcrTextLine("8 tranches de jambon", top = 255, left = 116, bottom = 283, right = 382),
            OcrTextLine("• Pelez le melon, épépinez-le et", top = 910, left = 93, bottom = 951, right = 623),
            OcrTextLine("• Mixez le porto, le vinaigre, le", top = 941, left = 686, bottom = 989, right = 1191),
            OcrTextLine("yoghourt, l'huile d'olive et la", top = 983, left = 682, bottom = 1037, right = 1165),
            OcrTextLine("• Disposez les bûchettes sur", top = 1212, left = 698, bottom = 1263, right = 1140),
            OcrTextLine("VIN CONSEILLE", top = 1169, left = 1454, bottom = 1217, right = 1781),
            OcrTextLine("Atinum, Bordeaux Graves", top = 1228, left = 1396, bottom = 1264, right = 1778)
        )

        val density = Ocr2DSpatialLayoutSorter.analyzeSpatialDensity(linesMelon)

        assertWithMessage("La recette du Melon à 3 colonnes doit être détectée avec columnCount = 3")
            .that(density.columnCount)
            .isEqualTo(3)

        assertWithMessage("La Tranche 1 (Gauche) doit contenir des lignes")
            .that(density.densityCol1)
            .isGreaterThan(0)

        assertWithMessage("La Tranche 2 (Centre) doit contenir des lignes")
            .that(density.densityCol2)
            .isGreaterThan(0)

        assertWithMessage("La Tranche 3 (Droite) doit contenir des lignes")
            .that(density.densityCol3)
            .isGreaterThan(0)
    }
}
