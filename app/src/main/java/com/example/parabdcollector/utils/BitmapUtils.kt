package com.example.parabdcollector.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object BitmapUtils {

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
