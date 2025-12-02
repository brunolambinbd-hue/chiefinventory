@file:Suppress("DEPRECATION")

package com.example.parabdcollector.utils

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.parabdcollector.R
import java.io.File

/**
 * A utility class to handle the entire image capture and cropping workflow.
 *
 * This class encapsulates the logic for:
 * 1. Requesting camera permission.
 * 2. Launching the device's camera.
 * 3. Launching the image cropping activity.
 * 4. Saving the final cropped image to internal storage.
 * 5. Returning a permanent content URI for the saved image.
 *
 * It relies on the modern Activity Result APIs.
 *
 * @param activity The [AppCompatActivity] that will host the camera and cropping activities.
 * @param onImageReady A callback lambda that is invoked with the permanent [Uri] of the saved image,
 *                     or null if the process fails or is cancelled.
 */
class ImageCaptureUtil(
    private val activity: AppCompatActivity,
    private val onImageReady: (Uri?) -> Unit
) {

    // Temporary URI for the photo taken by the camera, before cropping.
    private var photoUri: Uri? = null

    // Activity result launcher for the cropping action.
    private val cropImageLauncher = activity.registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // If cropping is successful, save the temporary cropped image to permanent storage.
            result.uriContent?.let { tempUri ->
                val permanentUri = ImageStorageHelper.saveImageToInternalStorage(activity, tempUri)
                onImageReady(permanentUri)
            } ?: onImageReady(null) // Cropping resulted in a null URI.
        } else {
            // Handle cropping error.
            val exception = result.error
            Toast.makeText(activity, "Erreur de recadrage: ${exception?.message}", Toast.LENGTH_SHORT).show()
            onImageReady(null)
        }
    }

    // Activity result launcher for taking a picture.
    private val takePictureLauncher = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // If picture is taken successfully, launch the cropping activity.
            photoUri?.let { uri ->
                val cropOptions = CropImageOptions().apply {
                    allowRotation = true
                    allowFlipping = true
                }
                val cropContractOptions = CropImageContractOptions(uri, cropOptions)
                cropImageLauncher.launch(cropContractOptions)
            }
        } else {
            // User cancelled the camera.
            onImageReady(null)
        }
    }

    // Activity result launcher for requesting the camera permission.
    private val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(activity, R.string.toast_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Starts the image capture process.
     * It checks for camera permission and launches the camera if granted,
     * otherwise it requests the permission.
     */
    fun start() {
        when {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                // We don't have permission, so we request it.
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Creates a temporary file, gets a content URI for it via a FileProvider,
     * and launches the camera activity.
     */
    private fun launchCamera() {
        // Create a file in the app's private storage.
        val imageFile = File(activity.filesDir, "images/capture_${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() }
        
        // Get a content URI for the file, which is required for sharing with the camera app.
        photoUri = FileProvider.getUriForFile(
            activity,
            "${activity.applicationContext.packageName}.fileprovider",
            imageFile
        )
        
        // Launch the camera, passing the URI where the image should be saved.
        photoUri?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }
}
