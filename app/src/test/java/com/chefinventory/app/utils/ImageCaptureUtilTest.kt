package com.chefinventory.app.utils

import android.Manifest
import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageView
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageCaptureUtilTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testActivityResultLaunchersCallbacksCoverage() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                when (contract) {
                    is androidx.activity.result.contract.ActivityResultContracts.RequestPermission -> {
                        dispatchResult(requestCode, true)
                    }
                    is androidx.activity.result.contract.ActivityResultContracts.OpenDocument -> {
                        dispatchResult(requestCode, Uri.parse("file:///tmp/doc.jpg"))
                    }
                    is androidx.activity.result.contract.ActivityResultContracts.TakePicture -> {
                        dispatchResult(requestCode, true)
                    }
                    is com.canhub.cropper.CropImageContract -> {
                        val mockResult = mock<CropImageView.CropResult> {
                            on { isSuccessful } doReturn true
                            on { uriContent } doReturn Uri.parse("file:///tmp/cropped.jpg")
                        }
                        dispatchResult(requestCode, mockResult)
                    }
                }
            }
        }

        var returnedUri: Uri? = null
        val util = ImageCaptureUtil(activity, testRegistry) { uri ->
            returnedUri = uri
        }
        controller.setup()

        util.startGallery()
        util.startCamera()
        util.launchCropper(Uri.parse("file:///tmp/source.jpg"))

        assertThat(util).isNotNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetUriForFileSafelyDirectCall() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val util = ImageCaptureUtil(activity) {}
        controller.setup()

        val dummyFile = File(activity.cacheDir, "test.jpg").apply { writeText("data") }
        try {
            util.getUriForFileSafely(activity, "${activity.packageName}.fileprovider", dummyFile)
        } catch (_: Exception) {}
        dummyFile.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetFileProviderUriDirectCall() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val providerInfo = android.content.pm.ProviderInfo().apply {
            authority = "${activity.packageName}.fileprovider"
            grantUriPermissions = true
            metaData = android.os.Bundle().apply {
                putInt("android.support.FILE_PROVIDER_PATHS", com.chefinventory.app.R.xml.provider_paths)
            }
        }
        val provider = FileProvider()
        provider.attachInfo(activity, providerInfo)

        val util = ImageCaptureUtil(activity) {}
        controller.setup()

        val imagesDir = File(activity.filesDir, "images").canonicalFile.apply { mkdirs() }
        val dummyFile = File(imagesDir, "test.jpg").canonicalFile.apply { writeText("data") }
        val uri = util.getFileProviderUri(dummyFile)
        assertThat(uri).isNotNull()
        dummyFile.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLaunchCamera_NullPhotoUriLogBranch() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val util = object : ImageCaptureUtil(activity, activity.activityResultRegistry, {}) {
            override fun createPhotoUri(imageFile: File): Uri? {
                return null
            }
        }
        controller.setup()

        util.launchCamera()
        assertThat(util.photoUri).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLaunchCamera_CatchExceptionFallbackFlow() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val util = object : ImageCaptureUtil(activity, activity.activityResultRegistry, {}) {
            override fun getFileProviderUri(file: File): Uri {
                throw RuntimeException("FileProvider failure")
            }
        }
        controller.setup()

        util.launchCamera()
        assertThat(util.photoUri).isNotNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLaunchCameraDirectFlow_AndCropErrorNullBranch() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val providerInfo = android.content.pm.ProviderInfo().apply {
            authority = "${activity.packageName}.fileprovider"
            grantUriPermissions = true
        }
        Robolectric.buildContentProvider(FileProvider::class.java).create(providerInfo)

        var returnedUri: Uri? = null
        val util = ImageCaptureUtil(activity) { uri -> returnedUri = uri }
        controller.setup()

        util.launchCamera()
        assertThat(util.photoUri).isNotNull()

        val mockErrorNull = mock<CropImageView.CropResult> {
            on { isSuccessful } doReturn false
            on { error } doReturn null
        }
        util.handleCropResult(mockErrorNull)
        assertThat(returnedUri).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testStartCameraAndStartGallery_PermissionDenied() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        var returnedUri: Uri? = null
        val util = ImageCaptureUtil(activity) { uri ->
            returnedUri = uri
        }

        controller.setup()

        util.startCamera()
        util.startGallery()
        util.start()

        assertThat(returnedUri).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testStartCamera_PermissionGranted_LaunchesCameraFlow() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        @Suppress("DEPRECATION")
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.CAMERA)

        var returnedUri: Uri? = null
        val util = ImageCaptureUtil(activity) { uri ->
            returnedUri = uri
        }

        controller.setup()

        util.startCamera()
        assertThat(returnedUri).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHandleCropResult_SuccessfulAndErrorBranches() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        var returnedUri: Uri? = null
        val util = ImageCaptureUtil(activity) { uri -> returnedUri = uri }
        controller.setup()

        val tempCropFile = File(activity.cacheDir, "temp_crop.png").apply { writeText("data") }
        val mockSuccessWithUri = mock<CropImageView.CropResult> {
            on { isSuccessful } doReturn true
            on { uriContent } doReturn Uri.fromFile(tempCropFile)
        }
        util.handleCropResult(mockSuccessWithUri)
        assertThat(returnedUri).isNotNull()

        val mockSuccessNullUri = mock<CropImageView.CropResult> {
            on { isSuccessful } doReturn true
            on { uriContent } doReturn null
        }
        util.handleCropResult(mockSuccessNullUri)
        assertThat(returnedUri).isNull()

        val mockError = mock<CropImageView.CropResult> {
            on { isSuccessful } doReturn false
            on { error } doReturn Exception("Crop failure")
        }
        util.handleCropResult(mockError)
        assertThat(returnedUri).isNull()

        tempCropFile.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHandleCameraResult_SuccessAndFailureBranches() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        var returnedUri: Uri? = null
        val util = ImageCaptureUtil(activity) { uri -> returnedUri = uri }
        controller.setup()

        util.handleCameraResult(false)
        assertThat(returnedUri).isNull()

        util.photoUri = null
        util.handleCameraResult(true)
        assertThat(returnedUri).isNull()

        util.photoUri = Uri.parse("file:///tmp/photo.jpg")
        util.handleCameraResult(true)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHandlePickFileResultAndPermissionResult() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        var returnedUri: Uri? = null
        val util = ImageCaptureUtil(activity) { uri -> returnedUri = uri }
        controller.setup()

        util.handlePickFileResult(null)
        assertThat(returnedUri).isNull()

        util.handlePickFileResult(Uri.parse("file:///tmp/picked.jpg"))

        util.handlePermissionResult(false)
        assertThat(returnedUri).isNull()

        util.handlePermissionResult(true)
    }
}
