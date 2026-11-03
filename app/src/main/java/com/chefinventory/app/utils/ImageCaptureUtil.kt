@file:Suppress("DEPRECATION")

package com.chefinventory.app.utils

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.chefinventory.app.R
import com.parabd.lib.traceability.utils.LogManager
import java.io.File

/**
 * A utility class to handle image acquisition (Capture or Pick) and cropping workflow.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
open class ImageCaptureUtil(
    private val activity: AppCompatActivity,
    registry: ActivityResultRegistry = activity.activityResultRegistry,
    private val onImageReady: (Uri?) -> Unit
) {

    internal var photoUri: Uri? = null

    // 1. Cropping Launcher
    private val cropImageLauncher = registry.register("crop_image_key", activity, CropImageContract()) { result ->
        handleCropResult(result)
    }

    // 2. Camera Launcher
    private val takePictureLauncher = registry.register("take_picture_key", activity, ActivityResultContracts.TakePicture()) { success ->
        handleCameraResult(success)
    }

    // 3. File Picker Launcher (Using OpenDocument for better access to Downloads)
    private val pickFileLauncher = registry.register("pick_file_key", activity, ActivityResultContracts.OpenDocument()) { uri ->
        handlePickFileResult(uri)
    }

    // 4. Permission Launcher
    private val requestPermissionLauncher = registry.register("request_permission_key", activity, ActivityResultContracts.RequestPermission()) { isGranted ->
        handlePermissionResult(isGranted)
    }

    internal fun handleCropResult(result: CropImageView.CropResult) {
        if (result.isSuccessful) {
            val tempUri = result.uriContent
            if (tempUri != null) {
                val permanentUri = ImageStorageHelper.saveImageToInternalStorage(activity, tempUri)
                onImageReady(permanentUri)
            } else {
                onImageReady(null)
            }
        } else {
            val exception = result.error
            Toast.makeText(activity, activity.getString(R.string.toast_crop_error, exception?.message.orEmpty()), Toast.LENGTH_SHORT).show()
            onImageReady(null)
        }
    }

    internal fun handleCameraResult(success: Boolean) {
        if (success) {
            val uri = photoUri
            if (uri != null) {
                launchCropper(uri)
            } else {
                onImageReady(null)
            }
        } else {
            onImageReady(null)
        }
    }

    internal fun handlePickFileResult(uri: Uri?) {
        if (uri != null) {
            launchCropper(uri)
        } else {
            onImageReady(null)
        }
    }

    internal fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(activity, R.string.toast_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    internal fun launchCropper(sourceUri: Uri) {
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

    internal open fun getUriForFileSafely(activity: AppCompatActivity, authority: String, file: File): Uri {
        return FileProvider.getUriForFile(activity, authority, file)
    }

    internal open fun getFileProviderUri(file: File): Uri {
        val authority = "${activity.applicationContext.packageName}.fileprovider"
        return try {
            getUriForFileSafely(activity, authority, file)
        } catch (_: Exception) {
            Uri.fromFile(file)
        }
    }

    internal open fun createPhotoUri(imageFile: File): Uri? {
        return try {
            getFileProviderUri(imageFile)
        } catch (e: Exception) {
            LogManager.e("ImageCaptureUtil", "Failed to get FileProvider URI for file: ${imageFile.absolutePath}", e)
            Uri.fromFile(imageFile)
        }
    }

    internal fun launchCamera() {
        val imagesDir = File(activity.filesDir, "images").apply { mkdirs() }
        val imageFile = File(imagesDir, "capture_${System.currentTimeMillis()}.jpg")
        photoUri = createPhotoUri(imageFile)
        val uriToLaunch = photoUri
        if (uriToLaunch != null) {
            takePictureLauncher.launch(uriToLaunch)
        } else {
            LogManager.e("ImageCaptureUtil", "photoUri est nul lors du lancement de la caméra")
        }
    }
}
