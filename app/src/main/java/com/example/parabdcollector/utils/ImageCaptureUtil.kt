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

class ImageCaptureUtil(
    private val activity: AppCompatActivity,
    private val onImageCropped: (Uri) -> Unit
) {

    private var photoUri: Uri? = null

    @Suppress("DEPRECATION")
    private val cropImageLauncher = activity.registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let(onImageCropped)
        } else {
            val exception = result.error
            Toast.makeText(activity, "Erreur de recadrage: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private val takePictureLauncher = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                val cropOptions = CropImageOptions().apply {
                    allowRotation = true
                    allowFlipping = true
                }
                val cropContractOptions = CropImageContractOptions(uri, cropOptions)
                cropImageLauncher.launch(cropContractOptions)
            }
        }
    }

    private val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(activity, R.string.toast_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    fun start() {
        when {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val imageFile = File(activity.filesDir, "images/capture_${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() }
        photoUri = FileProvider.getUriForFile(
            activity,
            "${activity.applicationContext.packageName}.fileprovider",
            imageFile
        )
        photoUri?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }
}
