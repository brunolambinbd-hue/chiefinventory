package com.example.parabdcollector.utils

import android.graphics.*

/**
 * Interface for image processing utilities to allow mocking in tests.
 */
interface IImageProcessor {
    fun enhanceContrast(src: Bitmap): Bitmap
}

/**
 * Utility to enhance images for better AI recognition.
 */
object ImageProcessingUtils : IImageProcessor {


    /**
     * Applies an auto-contrast filter (histogram stretching equivalent) to a bitmap.
     */
    override fun enhanceContrast(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val dest = Bitmap.createBitmap(width, height, src.config ?: Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(dest)
        val paint = Paint()
        
        // Matrice pour augmenter le contraste de 50% et la luminosité légèrement
        val contrast = 1.5f 
        val brightness = 10f
        
        val cm = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        
        return dest
    }
}
