package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation basé sur le logcat brut réel du smartphone pour la recette "Canapés grecs",
 * constatant que les métadonnées spatiales de BoundingBoxes ne sont pas encore présentes dans le logcat brut.
 */
class OcrCanapesGrecsLogcatTest {

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
    fun testParse_LogcatSmartphoneScan_ConfirmsBoundingBoxInfoNotYetEmittedInRawLog() {
        val rawScanLines = listOf(
            "Saveur de",
            "Danapés grecs",
            "Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-",
            "cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur + 12 heures.",
            "Découpez la baguette en tranches d'environ 1 cm, faites-les légèrement griller puis frottez-les",
            "d'ail, égouttez le fromage et placez-le sur le pain.",
            "Décorez d'olives coupées en deux et de romarin.",
            "Ingrédients pour 4 personnes",
            "180 g de feta",
            "1/2 verre d'huile d'olive",
            "10 olives vertes dénoyautées",
            "10 olives noires dénoyautées",
            "l petit pimnent",
            "I gousse d'ail",
            "I baguette de pain gris",
            "du thym et du romarin frais",
            "sel et poivre du moulin"
        )

        // Conversion en OcrTextLine virtuelles sans BoundingBoxes 2D réelles (top/left = 0)
        val textLines = rawScanLines.map { OcrTextLine.fromText(it) }

        assertWithMessage("Les lignes de test actuelles sans BoundingBoxes réelles ont top=0 et left=0")
            .that(textLines.first().top)
            .isEqualTo(0)

        val parsed = RecipeOcrParser.parse(rawScanLines.joinToString("\n"), vocabulary)

        assertWithMessage("Le titre extrait doit valoir 'Canapés grecs'")
            .that(parsed.title)
            .ignoringCase()
            .isEqualTo("Canapés grecs")

        assertWithMessage("L'ingrédient 'Du thym et du romarin frais' doit figurer dans les ingrédients")
            .that(parsed.ingredients.orEmpty())
            .contains("Du thym et du romarin frais")
    }
}
