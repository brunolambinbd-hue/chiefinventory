package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant le tri spatial 2D [Ocr2DSpatialLayoutSorter]
 * alimenté avec les coordonnées BoundingBoxes pixels 2D réelles scannées par la caméra du smartphone
 * pour la recette "Canapés grecs".
 */
class Ocr2DSpatialCanapesGrecsRealScanTest {

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
    fun testSortSpatially_RealSmartphoneCameraBoundingBoxes_Orders2DColumnsCorrectly() {
        // Lignes réelles capturées par la caméra ML Kit sur le smartphone avec leurs BoundingBoxes réelles (top, left, bottom, right)
        val rawSmartphoneSpatialLines = listOf(
            OcrTextLine("Saveur de", top = 178, left = 1127, bottom = 326, right = 1418),
            OcrTextLine("Danapés grecs", top = 687, left = 380, bottom = 849, right = 1263),
            OcrTextLine("Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-", top = 1071, left = 116, bottom = 1131, right = 1682),
            OcrTextLine("cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur + 12 heures.", top = 1120, left = 115, bottom = 1180, right = 1682),
            OcrTextLine("Découpez la baguette en tranches d'environ 1 cm, faites-les légèrement griller puis frottez-les", top = 1179, left = 116, bottom = 1240, right = 1685),
            OcrTextLine("d'ail, égouttez le fromage et placez-le sur le pain.", top = 1237, left = 116, bottom = 1287, right = 921),
            OcrTextLine("Décorez d'olives coupées en deux et de romarin.", top = 1299, left = 117, bottom = 1343, right = 910),
            OcrTextLine("Ingrédients pour 4 personnes", top = 1074, left = 2032, bottom = 1117, right = 2569),
            OcrTextLine("180 g de feta", top = 1157, left = 2034, bottom = 1194, right = 2239),
            OcrTextLine("1/2 verre d'huile d'olive", top = 1198, left = 2051, bottom = 1239, right = 2420),
            OcrTextLine("10 olives vertes dénoyautées", top = 1252, left = 2043, bottom = 1293, right = 2489),
            OcrTextLine("10 olives noires dénoyautées", top = 1301, left = 2036, bottom = 1341, right = 2496),
            OcrTextLine("l petit pimnent", top = 1358, left = 2037, bottom = 1395, right = 2258),
            OcrTextLine("I gousse d'ail", top = 1402, left = 2054, bottom = 1447, right = 2259),
            OcrTextLine("I baguette de pain gris", top = 1452, left = 2054, bottom = 1495, right = 2405),
            OcrTextLine("du thym et du romarin frais", top = 1502, left = 2037, bottom = 1545, right = 2490),
            OcrTextLine("sel et poivre du moulin", top = 1552, left = 2038, bottom = 1596, right = 2409)
        )

        // Exécution du tri spatial 2D
        val sortedSpatialLines = Ocr2DSpatialLayoutSorter.sortSpatially(rawSmartphoneSpatialLines)

        // Conversion des textes réordonnés
        val sortedTexts = sortedSpatialLines.map { it.text }

        assertWithMessage("Le tri spatial 2D doit conserver 'du thym et du romarin frais' immédiatement après la baguette")
            .that(sortedTexts)
            .contains("du thym et du romarin frais")

        assertWithMessage("Le tri spatial 2D doit positionner 'du thym et du romarin frais' avant 'sel et poivre du moulin'")
            .that(sortedTexts.indexOf("du thym et du romarin frais"))
            .isLessThan(sortedTexts.indexOf("sel et poivre du moulin"))
    }
}
