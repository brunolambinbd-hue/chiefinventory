package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant le tri spatial 2D [Ocr2DSpatialLayoutSorter]
 * sur des BoundingBoxes mélangées (Haut en Bas, Gauche à Droite).
 */
class Ocr2DSpatialLayoutSorterTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSortSpatially_ReordersMixed2DBoundingBoxLinesInHumanReadingOrder() {
        val lineBottom = OcrTextLine("Mélangez le tout.", top = 400, left = 50, bottom = 430, right = 300)
        val lineMiddleRight = OcrTextLine("50 g de beurre", top = 200, left = 300, bottom = 230, right = 500)
        val lineTop = OcrTextLine("Tarte aux pommes", top = 100, left = 50, bottom = 130, right = 400)
        val lineMiddleLeft = OcrTextLine("200 g de farine", top = 200, left = 50, bottom = 230, right = 250)

        val outOfOrderInput = listOf(
            lineBottom,
            lineMiddleRight,
            lineTop,
            lineMiddleLeft
        )

        val sorted = Ocr2DSpatialLayoutSorter.sortSpatially(outOfOrderInput)

        val sortedTexts = sorted.map { it.text }

        assertWithMessage("Le tri spatial 2D doit réordonner les lignes du haut vers le bas et de gauche à droite")
            .that(sortedTexts)
            .containsExactly(
                "Tarte aux pommes",
                "200 g de farine",
                "50 g de beurre",
                "Mélangez le tout."
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSortSpatially_HandlesSingleOrEmptyList() {
        assertWithMessage("Une liste vide doit retourner une liste vide")
            .that(Ocr2DSpatialLayoutSorter.sortSpatially(emptyList()))
            .isEmpty()

        val singleLine = listOf(OcrTextLine("Titre seul", top = 10, left = 10, bottom = 30, right = 100))
        assertWithMessage("Une liste à un seul élément doit être retournée inchangée")
            .that(Ocr2DSpatialLayoutSorter.sortSpatially(singleLine))
            .containsExactlyElementsIn(singleLine)
    }
}
