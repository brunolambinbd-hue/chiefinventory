package com.chefinventory.app.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect

/**
 * Assistant de vision par ordinateur dédié à l'analyse de luminance et de densité de pixels foncés
 * sur le pavé 2D d'indice de prix (pictogrammes d'euros € ○ ○, € € ○, € € €).
 */
object OcrPriceBitmapAnalyzer {

    private const val DARK_LUMINANCE_THRESHOLD = 160
    private const val MIN_DARK_RATIO_ACTIVE = 0.08

    /**
     * Analyse l'image Bitmap rognée aux coordonnées 2D [bounds] et détermine le niveau de prix (1, 2 ou 3).
     *
     * @param bitmap L'image Bitmap source complète scannée.
     * @param bounds Les coordonnées BoundingBox 2D du pavé de prix.
     * @return L'indice de prix sous forme de chaîne ("1", "2", "3"), ou null si indéterminable.
     */
    fun analyzePriceRatingFromBitmap(bitmap: Bitmap?, bounds: Rect?): String? {
        if (bitmap == null || bounds == null || bounds.width() <= 0 || bounds.height() <= 0) return null

        val cropLeft = bounds.left.coerceIn(0, bitmap.width - 1)
        val cropTop = bounds.top.coerceIn(0, bitmap.height - 1)
        val cropRight = bounds.right.coerceIn(cropLeft + 1, bitmap.width)
        val cropBottom = bounds.bottom.coerceIn(cropTop + 1, bitmap.height)

        val cropWidth = cropRight - cropLeft
        val cropHeight = cropBottom - cropTop
        if (cropWidth < 10 || cropHeight < 5) return null

        val croppedBitmap = try {
            Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropWidth, cropHeight)
        } catch (_: Exception) {
            return null
        }

        val slotWidth = cropWidth / 3
        if (slotWidth <= 0) return null

        var activeSlotsCount = 0

        for (slotIdx in 0..2) {
            val slotStart = slotIdx * slotWidth
            val slotEnd = if (slotIdx == 2) cropWidth else (slotIdx + 1) * slotWidth

            var totalPixels = 0
            var darkPixels = 0

            for (y in 0 until cropHeight) {
                for (x in slotStart until slotEnd) {
                    val pixel = croppedBitmap.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)

                    val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
                    if (luminance < DARK_LUMINANCE_THRESHOLD) {
                        darkPixels++
                    }
                    totalPixels++
                }
            }

            val darkRatio = if (totalPixels > 0) darkPixels.toDouble() / totalPixels else 0.0
            if (darkRatio >= MIN_DARK_RATIO_ACTIVE) {
                activeSlotsCount++
            }
        }

        return when (activeSlotsCount) {
            1 -> "1"
            2 -> "2"
            3 -> "3"
            else -> if (activeSlotsCount > 0) "1" else null
        }
    }
}
