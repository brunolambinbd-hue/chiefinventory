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
 * A utility class to handle the entire image capture and cropping workflow within an [AppCompatActivity].
 *
 * This class encapsulates the modern Activity Result APIs for a complex user flow:
 * 1. It checks for and requests the `CAMERA` permission.
 * 2. It creates a temporary file and launches the system's camera activity.
 * 3. Upon successful capture, it launches the `CropImageActivity` for image editing.
 * 4. Upon successful cropping, it saves the resulting image to permanent internal storage using [ImageStorageHelper].
 * 5. Finally, it invokes a callback with the permanent content URI of the saved image.
 *
 * @param activity The [AppCompatActivity] that will host the camera and cropping activities. This is required to register the activity result launchers.
 * @param onImageReady A callback lambda that is invoked with the permanent [Uri] of the saved image,
 *                     or null if the process fails, is cancelled by the user, or results in an error.
 */
class ImageCaptureUtil(
    private val activity: AppCompatActivity,
    private val onImageReady: (Uri?) -> Unit
) {

    // Temporary URI for the high-resolution photo taken by the camera, before cropping.
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
            // If picture is taken successfully, launch the cropping activity with the captured image URI.
            photoUri?.let { uri ->
                val cropOptions = CropImageOptions().apply {
                    allowRotation = true
                    allowFlipping = true
                    // Add any other desired cropping options here.
                }
                val cropContractOptions = CropImageContractOptions(uri, cropOptions)
                cropImageLauncher.launch(cropContractOptions)
            }
        } else {
            // User cancelled the camera app.
            onImageReady(null)
        }
    }

    // Activity result launcher for requesting the CAMERA permission.
    private val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // If permission is granted, proceed with launching the camera.
            launchCamera()
        } else {
            // If permission is denied, inform the user via a Toast.
            Toast.makeText(activity, R.string.toast_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Starts the image capture process.
     *
     * This is the main entry point for the utility. It checks for camera permission and either
     * launches the camera directly if the permission is already granted, or requests it otherwise.
     */
    fun start() {
        when {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                // We don't have permission, so we request it. The result will be handled by the requestPermissionLauncher.
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Creates a temporary file and launches the camera activity.
     *
     * This method generates a unique temporary file in the app's private storage, obtains a content
     * URI for it via a [FileProvider], and then launches the camera activity with that URI as the output target.
     */
    private fun launchCamera() {
        // Create a unique file in the app's private storage to avoid conflicts.
        val imageFile = File(activity.filesDir, "images/capture_${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() }
        
        // Get a content URI for the file, which is required for sharing it with the camera app securely.
        photoUri = FileProvider.getUriForFile(
            activity,
            "${activity.applicationContext.packageName}.fileprovider",
            imageFile
        )
        
        // Launch the camera, passing the URI where the full-resolution image should be saved.
        photoUri?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }
}
