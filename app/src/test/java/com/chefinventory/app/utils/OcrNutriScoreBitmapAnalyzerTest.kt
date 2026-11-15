package com.chefinventory.app.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test unitaire TDD d'isolation de niveau vision par ordinateur ([OcrNutriScoreBitmapAnalyzer])
 * vérifiant la détection automatique du Nutri-Score actif (A, B, C, D, E) par analyse de sous-image Bitmap.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrNutriScoreBitmapAnalyzerTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeNutriScoreFromBitmap_MultiLetterACD_ClassifiesGradeC() {
        // Crée une image Bitmap synthétique de 150x30 pixels avec 5 créneaux de 30x30 pixels (A, B, C, D, E)
        val bitmap = Bitmap.createBitmap(150, 30, Bitmap.Config.ARGB_8888)
        val bounds = Rect(0, 0, 150, 30)

        // Remplissage du 3ème créneau (Grade C, x de 60 à 90) avec des pixels noirs (fond saturé de couleur)
        for (y in 0 until 30) {
            for (x in 0 until 60) {
                bitmap.setPixel(x, y, Color.WHITE)
            }
            for (x in 60 until 90) {
                bitmap.setPixel(x, y, Color.BLACK)
            }
            for (x in 90 until 150) {
                bitmap.setPixel(x, y, Color.WHITE)
            }
        }

        val grade = OcrNutriScoreBitmapAnalyzer.analyzeNutriScoreFromBitmap(bitmap, bounds, "ACD")

        assertWithMessage("Le 3ème créneau foncé (Grade C) doit retourner le Nutri-Score 'C'")
            .that(grade)
            .isEqualTo("C")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeNutriScoreFromBitmap_SingleLetter_ReturnsLetter() {
        val grade = OcrNutriScoreBitmapAnalyzer.analyzeNutriScoreFromBitmap(null, null, "B")
        assertWithMessage("Une seule lettre 'B' doit retourner 'B'")
            .that(grade)
            .isEqualTo("B")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzeNutriScoreFromBitmap_NullBitmap_ReturnsNull() {
        val grade = OcrNutriScoreBitmapAnalyzer.analyzeNutriScoreFromBitmap(null, null, "ACD")
        assertWithMessage("Si le Bitmap est null sur un multi-lettres, doit retourner null")
            .that(grade)
            .isNull()
    }
}
