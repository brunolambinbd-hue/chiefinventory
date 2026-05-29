package com.example.parabdcollector.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyFloat
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [ImageProcessingUtils].
 * Note: Since [Bitmap], [Canvas], etc. are part of the Android SDK and return default values
 * in unit tests (via isReturnDefaultValues = true), we can mainly test that the process 
 * flows correctly and calls the expected Android APIs.
 */
class ImageProcessingUtilsTest {

    @Test
    fun `enhanceContrast should create new bitmap and draw on it`() {
        // GIVEN
        // In a pure unit test with isReturnDefaultValues = true, 
        // Bitmap.createBitmap will return a mock-like object.
        // We can't easily test the actual pixel changes without Robolectric,
        // but we can verify that the method runs without crashing.

        val mockSrc = mock<Bitmap>()
        val mockDest = mock<Bitmap>()
        whenever(mockSrc.width).thenReturn(100)
        whenever(mockSrc.height).thenReturn(100)


        // Simulation de la méthode statique Bitmap.createBitmap
        mockStatic(Bitmap::class.java).use { mockedBitmap ->
            mockedBitmap.`when`<Bitmap> {
                Bitmap.createBitmap(anyInt(), anyInt(), any())
            }.thenReturn(mockDest)

            // Simulation du Canvas (classe finale/native)
            mockConstruction(Canvas::class.java).use {
                val result = ImageProcessingUtils.enhanceContrast(mockSrc)
                assertNotNull(result)
                assertEquals(mockDest, result)
            }
        }
    }
}
