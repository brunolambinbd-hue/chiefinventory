package com.example.chiefinventory.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * A utility object for managing the permanent storage of images within the app's internal' files directory.
 * This ensures that images captured by the user are not lost when the app's cache is cleared.
 */
object ImageStorageHelper {

    /**
     * Saves an image from a temporary URI (e.g., from a camera or cropper)
     * to a permanent location in the app's internal storage.
     *
     * This prevents the file from being lost if the temporary content is cleaned up by the system.
     * It creates a unique file name for each image to avoid collisions.
     *
     * @param context The application context, needed to access the content resolver and internal storage directory.
     * @param tempUri The temporary URI of the image to be saved.
     * @return A permanent content URI for the newly saved file, or null if the copy operation fails.
     */
    fun saveImageToInternalStorage(context: Context, tempUri: Uri): Uri? {
        // The directory within internal storage where we save the images.
        val imageDir = File(context.filesDir, "images")
        // Ensure the directory exists.
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }

        // Create a destination file with a unique name based on the current timestamp.
        val destinationFile = File(imageDir, "img_${System.currentTimeMillis()}.jpg")

        try {
            // Open an input stream from the temporary URI.
            context.contentResolver.openInputStream(tempUri)?.use { inputStream ->
                // Open an output stream to the destination file.
                FileOutputStream(destinationFile).use { outputStream ->
                    // Copy the data from the input stream to the output stream.
                    inputStream.copyTo(outputStream)
                }
            }
            // Return the content URI for the new permanent file.
            return Uri.fromFile(destinationFile)
        } catch (e: IOException) {
            // Log the error and return null if any part of the process fails.
            e.printStackTrace()
            return null
        }
    }
}
