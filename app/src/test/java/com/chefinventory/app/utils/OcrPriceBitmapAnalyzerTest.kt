package com.chefinventory.app.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [OcrPriceBitmapAnalyzer].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrPriceBitmapAnalyzerTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertWithMessage("Le composant OcrPriceBitmapAnalyzer doit générer une représentation textuelle valide")
            .that(OcrPriceBitmapAnalyzer.toString())
            .contains("OcrPriceBitmapAnalyzer")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzePriceRatingFromBitmap_NullOrInvalidBounds_ReturnsNull() {
        val bitmap = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)

        assertThat(OcrPriceBitmapAnalyzer.analyzePriceRatingFromBitmap(null, Rect(0, 0, 100, 50))).isNull()
        assertThat(OcrPriceBitmapAnalyzer.analyzePriceRatingFromBitmap(bitmap, null)).isNull()
        assertThat(OcrPriceBitmapAnalyzer.analyzePriceRatingFromBitmap(bitmap, Rect(0, 0, 0, 0))).isNull()
        assertThat(OcrPriceBitmapAnalyzer.analyzePriceRatingFromBitmap(bitmap, Rect(0, 0, 5, 2))).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAnalyzePriceRatingFromBitmap_SlotAnalysis_ReturnsCorrectRating() {
        val width = 90
        val height = 30
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 1. All white pixels -> returns null
        for (y in 0 until height) {
            for (x in 0 until width) {
                bitmap.setPixel(x, y, Color.WHITE)
            }
        }
        assertThat(OcrPriceBitmapAnalyzer.analyzePriceRatingFromBitmap(bitmap, Rect(0, 0, width, height))).isNull()

        // 2. Slot 0 black, slots 1 & 2 white -> returns "1"
        for (y in 0 until height) {
            for (x in 0 until 30) {
                bitmap.setPixel(x, y, Color.BLACK)
            }
        }
        assertThat(OcrPriceBitmapAnalyzer.analyzePriceRatingFromBitmap(bitmap, Rect(0, 0, width, height))).isEqualTo("1")

        // 3. Slots 0 & 1 black, slot 2 white -> returns "2"
        for (y in 0 until height) {
            for (x in 30 until 60) {
                bitmap.setPixel(x, y, Color.BLACK)
            }
        }
        assertThat(OcrPriceBitmapAnalyzer.analyzePriceRatingFromBitmap(bitmap, Rect(0, 0, width, height))).isEqualTo("2")

        // 4. All 3 slots black -> returns "3"
        for (y in 0 until height) {
            for (x in 60 until 90) {
                bitmap.setPixel(x, y, Color.BLACK)
            }
        }
        assertThat(OcrPriceBitmapAnalyzer.analyzePriceRatingFromBitmap(bitmap, Rect(0, 0, width, height))).isEqualTo("3")
    }
}
