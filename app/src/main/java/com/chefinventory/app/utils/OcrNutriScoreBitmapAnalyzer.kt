package com.chefinventory.app.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect

/**
 * Assistant de vision par ordinateur dédié à l'analyse de sous-image Bitmap et de détection par delta
 * de taille et de contraste du Nutri-Score réel (A, B, C, D, E) sur un pavé multi-lettres (ex: "ACD", "ABCDE").
 */
object OcrNutriScoreBitmapAnalyzer {

    private const val DARK_LUMINANCE_THRESHOLD = 180

    /**
     * Analyse l'image Bitmap rognée aux coordonnées 2D [bounds] et identifie la lettre de grade Nutri-Score active.
     *
     * @param bitmap L'image Bitmap source complète scannée.
     * @param bounds Les coordonnées BoundingBox 2D du pavé de Nutri-Score.
     * @param rawOcrText Le texte brut scanné par ML Kit (ex: "ACD", "ABCDE", "B").
     * @return La lettre de grade Nutri-Score retenue ("A", "B", "C", "D", "E"), ou null si indéterminable.
     */
    fun analyzeNutriScoreFromBitmap(bitmap: Bitmap?, bounds: Rect?, rawOcrText: String): String? {
        val textWithoutHeader = rawOcrText.replace(Regex("(?i)\\bnutri[-_\\s]*sc[o0]re\\b|\\bscore\\b|\\bnutri\\b"), "").trim()
        val cleanText = textWithoutHeader.replace(Regex("[^a-eA-E]"), "").uppercase()

        // 1. Si l'OCR a lu une seule lettre valide (A, B, C, D ou E)
        if (cleanText.length == 1 && cleanText in listOf("A", "B", "C", "D", "E")) {
            return cleanText
        }

        if (bitmap == null || bounds == null || bounds.width() <= 0 || bounds.height() <= 0) {
            return null
        }

        val cropLeft = bounds.left.coerceIn(0, bitmap.width - 1)
        val cropTop = bounds.top.coerceIn(0, bitmap.height - 1)
        val cropRight = bounds.right.coerceIn(cropLeft + 1, bitmap.width)
        val cropBottom = bounds.bottom.coerceIn(cropTop + 1, bitmap.height)

        val cropWidth = cropRight - cropLeft
        val cropHeight = cropBottom - cropTop
        if (cropWidth < 15 || cropHeight < 5) {
            return null
        }

        val croppedBitmap = try {
            Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropWidth, cropHeight)
        } catch (_: Exception) {
            return null
        }

        // 2. Découpage en 5 créneaux virtuels pour les grades A, B, C, D, E
        val slotWidth = cropWidth / 5
        if (slotWidth <= 0) return null

        val gradeLetters = listOf("A", "B", "C", "D", "E")
        var bestGrade: String? = null
        var maxDarkRatio = -1.0

        for (slotIdx in 0..4) {
            val slotStart = slotIdx * slotWidth
            val slotEnd = if (slotIdx == 4) cropWidth else (slotIdx + 1) * slotWidth

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
            if (darkRatio > maxDarkRatio && darkRatio > 0.05) {
                maxDarkRatio = darkRatio
                bestGrade = gradeLetters[slotIdx]
            }
        }

        return bestGrade
    }
}
