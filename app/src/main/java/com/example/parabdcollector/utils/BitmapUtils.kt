package com.example.parabdcollector.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/**
 * A utility object for handling [Bitmap] related operations.
 */
object BitmapUtils {

    /**
     * Decodes a [Bitmap] from a given [Uri] and ensures it is in the ARGB_8888 format.
     *
     * This function handles both modern and legacy methods of decoding bitmaps and performs a color
     * space conversion if necessary, as required by libraries like MediaPipe.
     *
     * @param context The application context, used to access the ContentResolver.
     * @param uri The Uri of the image to decode.
     * @return The decoded and correctly formatted [Bitmap].
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        // MediaPipe requiert le format ARGB_8888. On convertit si nécessaire.
        if (originalBitmap.config == Bitmap.Config.ARGB_8888) {
            return originalBitmap
        }
        return originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
    }
}
