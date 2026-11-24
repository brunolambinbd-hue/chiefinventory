package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 0, le trieur spatial Ocr2DSpatialLayoutSorter
 * met en réserve les bannières candidates au sommet et ordonne les colonnes de densité (Col 1 -> Col 2 -> Col 3).
 */
class Ocr2DSpatialColumnOrderingTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSortSpatially_ThreeColumnsMelonRecipe_OrdersBannersThenCol1ThenCol2ThenCol3() {
        val linesMelon = listOf(
            OcrTextLine("Büchettes de melon", top = 608, left = 454, bottom = 778, right = 1392),
            OcrTextLine("au jambon italien et au porto", top = 740, left = 672, bottom = 865, right = 1827),
            OcrTextLine("I melon Galia", top = 208, left = 175, bottom = 238, right = 337),
            OcrTextLine("8 tranches de jambon", top = 255, left = 116, bottom = 283, right = 382),
            OcrTextLine("• Pelez le melon, épépinez-le et", top = 910, left = 93, bottom = 951, right = 623),
            OcrTextLine("• Mixez le porto, le vinaigre, le", top = 941, left = 686, bottom = 989, right = 1191),
            OcrTextLine("• Disposez les bûchettes sur", top = 1212, left = 698, bottom = 1263, right = 1140),
            OcrTextLine("une assiette avec la vinaigrette.", top = 1260, left = 686, bottom = 1305, right = 1195),
            OcrTextLine("VIN CONSEILLE", top = 1169, left = 1454, bottom = 1217, right = 1781)
        )

        val sorted = Ocr2DSpatialLayoutSorter.sortSpatially(linesMelon)
        val texts = sorted.map { it.text }

        assertWithMessage("Les bannières de titre doivent être conservées au tout début du flux")
            .that(texts.take(2))
            .containsExactly("Büchettes de melon", "au jambon italien et au porto")
            .inOrder()

        val idx1 = texts.indexOf("• Disposez les bûchettes sur")
        val idx2 = texts.indexOf("une assiette avec la vinaigrette.")
        assertWithMessage("La consigne 'une assiette avec la vinaigrette.' doit suivre immédiatement '• Disposez les bûchettes sur'")
            .that(idx2)
            .isEqualTo(idx1 + 1)

        assertWithMessage("Le vin doit figurer à la fin du flux réordonné")
            .that(texts.last())
            .isEqualTo("VIN CONSEILLE")
    }
}
