package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau B (Étape 1 Spatiale) vérifiant l'exécution de [Ocr1Corrector.correctLine]
 * sur le dataset réel scanné par la caméra du smartphone pour la recette "Nems croustillants au crabe".
 */
class Ocr1CorrectorSmartphoneDatasetTest {

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
    fun testCorrectLine_SmartphoneDataset_NemsAuCrabe_CorrectsDigitConfusionsAndPreserves2DBoundingBoxes() {
        val line1 = OcrTextLine("I petit piment", top = 300, left = 50, bottom = 325, right = 200)
        val line2 = OcrTextLine("I gousse d'ail", top = 330, left = 50, bottom = 355, right = 220)
        val line3 = OcrTextLine("I baguette de pain gris", top = 360, left = 50, bottom = 385, right = 300)

        val corrected1 = Ocr1Corrector.correctLine(line1, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs)
        val corrected2 = Ocr1Corrector.correctLine(line2, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs)
        val corrected3 = Ocr1Corrector.correctLine(line3, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs)

        assertWithMessage("'I petit piment' doit être corrigé en '1 petit piment'")
            .that(corrected1.text)
            .isEqualTo("1 petit piment")
        assertWithMessage("La BoundingBox top doit être préservée")
            .that(corrected1.top)
            .isEqualTo(300)

        assertWithMessage("'I gousse d'ail' doit être corrigé en '1 gousse d'ail'")
            .that(corrected2.text)
            .isEqualTo("1 gousse d'ail")
        assertWithMessage("La BoundingBox top doit être préservée")
            .that(corrected2.top)
            .isEqualTo(330)

        assertWithMessage("'I baguette de pain gris' doit être corrigé en '1 baguette de pain gris'")
            .that(corrected3.text)
            .isEqualTo("1 baguette de pain gris")
        assertWithMessage("La BoundingBox top doit être préservée")
            .that(corrected3.top)
            .isEqualTo(360)
    }
}
