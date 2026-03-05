@file:Suppress("DEPRECATION")

package com.example.chiefinventory.utils

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
import com.example.chiefinventory.R
import java.io.File

/**
 * A utility class to handle image acquisition (Capture or Pick) and cropping workflow.
 */
class ImageCaptureUtil(
    private val activity: AppCompatActivity,
    private val onImageReady: (Uri?) -> Unit
) {

    private var photoUri: Uri? = null

    // 1. Cropping Launcher
    private val cropImageLauncher = activity.registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { tempUri ->
                val permanentUri = ImageStorageHelper.saveImageToInternalStorage(activity, tempUri)
                onImageReady(permanentUri)
            } ?: onImageReady(null)
        } else {
            val exception = result.error
            Toast.makeText(activity, activity.getString(R.string.toast_crop_error, exception?.message), Toast.LENGTH_SHORT).show()
            onImageReady(null)
        }
    }

    // 2. Camera Launcher
    private val takePictureLauncher = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri -> launchCropper(uri) }
        } else {
            onImageReady(null)
        }
    }

    // 3. File Picker Launcher (Using OpenDocument for better access to Downloads)
    private val pickFileLauncher = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            launchCropper(uri)
        } else {
            onImageReady(null)
        }
    }

    // 4. Permission Launcher
    private val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchCamera() else Toast.makeText(activity, R.string.toast_camera_permission_denied, Toast.LENGTH_SHORT).show()
    }

    private fun launchCropper(sourceUri: Uri) {
        val cropOptions = CropImageOptions().apply {
            allowRotation = true
            allowFlipping = true
        }
        cropImageLauncher.launch(CropImageContractOptions(sourceUri, cropOptions))
    }

    /** Starts the camera capture flow. */
    fun startCamera() {
        when {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /** Starts the file picker flow. */
    fun startGallery() {
        pickFileLauncher.launch(arrayOf("image/*"))
    }

    /** Legacy entry point. */
    fun start(): Unit = startCamera()

    private fun launchCamera() {
        val imageFile = File(activity.filesDir, "images/capture_${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() }
        photoUri = FileProvider.getUriForFile(activity, "${activity.applicationContext.packageName}.fileprovider", imageFile)
        photoUri?.let { takePictureLauncher.launch(it) }
    }
}
