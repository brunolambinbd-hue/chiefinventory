package com.chefinventory.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.parabd.lib.traceability.utils.LogManager

/**
 * A utility object for handling [Bitmap] related operations, particularly for use with MediaPipe.
 */
object BitmapUtils {

    private const val TAG = "BitmapUtils"

    /**
     * Safely decodes a [Bitmap] from a given [Uri] and ensures it is in the ARGB_8888 format.
     *
     * This function is critical for preparing images for MediaPipe, which requires the ARGB_8888
     * color space. It handles both modern (API 28+) and legacy methods of decoding bitmaps.
     * If the decoding fails for any reason (e.g., invalid image format, corrupted file),
     * it logs the error and returns null instead of crashing.
     *
     * @param context The application context, used to access the ContentResolver.
     * @param uri The Uri of the image to decode.
     * @return The decoded and correctly formatted [Bitmap], or null on failure.
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } catch (_: Exception) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            // Ensure the bitmap is in the ARGB_8888 format required by MediaPipe.
            if (originalBitmap.config == Bitmap.Config.ARGB_8888) {
                originalBitmap
            } else {
                originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to decode bitmap from URI: $uri", e)
            null
        }
    }

    /**
     * Redimensionne un [Bitmap] pour que ses dimensions ne dépassent pas [maxDimension] pixels (par défaut 1920px Full HD).
     *
     * @param bitmap Le Bitmap d'origine.
     * @param maxDimension La dimension maximale tolérée en largeur ou hauteur.
     * @return Le Bitmap redimensionné en conserve de ratio d'aspect.
     */
    fun scaleBitmapToMaxDimension(bitmap: Bitmap, maxDimension: Int = 1920): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = minOf(maxDimension.toDouble() / width, maxDimension.toDouble() / height)
        val targetWidth = (width * ratio).toInt()
        val targetHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Enregistre un [Bitmap] optimisé Full HD (1920px max) dans le stockage interne privé de l'application.
     *
     * Garantit un accès permanent et sécurisé 100% persistant à l'image sans expiration de permissions Android.
     *
     * @param context Le contexte Android.
     * @param bitmap Le Bitmap à sauvegarder.
     * @param fileName Le nom du fichier image.
     * @param maxDimension La dimension maximale en pixels (par défaut 1920).
     * @return L'Uri locale [Uri] du fichier sauvegardé en stockage interne privé.
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, fileName: String, maxDimension: Int = 1920): Uri {
        val scaledBitmap = scaleBitmapToMaxDimension(bitmap, maxDimension)
        val imagesDir = java.io.File(context.filesDir, "recipe_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val imageFile = java.io.File(imagesDir, fileName)
        java.io.FileOutputStream(imageFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        return imageFile.toUri()
    }
}
