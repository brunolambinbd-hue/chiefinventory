package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la création, les propriétés calculées
 * et la rétro-compatibilité du modèle de données spatiales 2D [OcrTextLine].
 */
class OcrTextLineTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOcrTextLine_CreationAndCalculatedProperties() {
        val line = OcrTextLine(
            text = "180 g de feta",
            top = 200,
            left = 100,
            bottom = 230,
            right = 450
        )

        assertWithMessage("Le texte de la ligne doit correspondre à la chaîne fournie")
            .that(line.text)
            .isEqualTo("180 g de feta")

        assertWithMessage("Le centre vertical centerY doit valoir (200 + 230) / 2 = 215")
            .that(line.centerY)
            .isEqualTo(215)

        assertWithMessage("La hauteur doit valoir (230 - 200) = 30")
            .that(line.height)
            .isEqualTo(30)

        assertWithMessage("La largeur doit valoir (450 - 100) = 350")
            .that(line.width)
            .isEqualTo(350)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOcrTextLine_FromTextFactoryHelper_ProvidesBackwardCompatibility() {
        val line0 = OcrTextLine.fromText("Ingrédients", lineIndex = 0)
        val line1 = OcrTextLine.fromText("180 g de feta", lineIndex = 1)

        assertWithMessage("Le texte de line0 doit valoir 'Ingrédients'")
            .that(line0.text)
            .isEqualTo("Ingrédients")

        assertWithMessage("Le top de line0 (index 0) doit valoir 0")
            .that(line0.top)
            .isEqualTo(0)

        assertWithMessage("Le top de line1 (index 1) doit valoir 30")
            .that(line1.top)
            .isEqualTo(30)

        assertWithMessage("Le right de line1 doit valoir length * 10 = 130")
            .that(line1.right)
            .isEqualTo(130)
    }
}
