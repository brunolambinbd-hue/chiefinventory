package com.example.parabdcollector.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log

/**
 * A utility object for handling [Bitmap] related operations.
 */
object BitmapUtils {

    private const val TAG = "BitmapUtils"

    /**
     * Safely decodes a [Bitmap] from a given [Uri] and ensures it is in the ARGB_8888 format.
     *
     * This function handles both modern and legacy methods of decoding bitmaps and performs a color
     * space conversion if necessary, as required by libraries like MediaPipe.
     * It returns null if the decoding fails for any reason (e.g., invalid image format, corrupted file).
     *
     * @param context The application context, used to access the ContentResolver.
     * @param uri The Uri of the image to decode.
     * @return The decoded and correctly formatted [Bitmap], or null on failure.
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            // MediaPipe requiert le format ARGB_8888. On convertit si nécessaire.
            if (originalBitmap.config == Bitmap.Config.ARGB_8888) {
                originalBitmap
            } else {
                originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap from URI: $uri", e)
            null
        }
    }
}
