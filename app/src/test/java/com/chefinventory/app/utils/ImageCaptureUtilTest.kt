package com.chefinventory.app.utils

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
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
    fun testGetFileProviderUriDirectCall() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        // Reset FileProvider static cache to ensure fresh provider paths lookup
        try {
            val field = FileProvider::class.java.getDeclaredField("sCache")
            field.isAccessible = true
            (field.get(null) as? MutableMap<*, *>)?.clear()
        } catch (_: Exception) {}

        val providerInfo = android.content.pm.ProviderInfo().apply {
            authority = "${activity.packageName}.fileprovider"
            grantUriPermissions = true
            metaData = android.os.Bundle().apply {
                putInt("android.support.FILE_PROVIDER_PATHS", com.chefinventory.app.R.xml.provider_paths)
            }
        }
        Robolectric.buildContentProvider(FileProvider::class.java).create(providerInfo)

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

        val util = object : ImageCaptureUtil(activity, {}) {
            override fun createPhotoUri(imageFile: File): Uri? {
                return null
            }
        }
        controller.setup()

        // Force launchCamera -> createPhotoUri returns null, executes L148 LogManager.e else branch!
        util.launchCamera()
        assertThat(util.photoUri).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLaunchCamera_CatchExceptionFallbackFlow() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val util = object : ImageCaptureUtil(activity, {}) {
            override fun getFileProviderUri(file: File): Uri {
                throw RuntimeException("FileProvider failure")
            }
        }
        controller.setup()

        // Force launchCamera -> catch block executes Uri.fromFile(imageFile)
        util.launchCamera()
        assertThat(util.photoUri).isNotNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLaunchCameraDirectFlow_AndCropErrorNullBranch() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        val activity = controller.get()
        activity.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        // Register FileProvider in Robolectric to make FileProvider.getUriForFile succeed (L124)
        val providerInfo = android.content.pm.ProviderInfo().apply {
            authority = "${activity.packageName}.fileprovider"
            grantUriPermissions = true
        }
        Robolectric.buildContentProvider(FileProvider::class.java).create(providerInfo)

        var returnedUri: Uri? = null
        val util = ImageCaptureUtil(activity) { uri -> returnedUri = uri }
        controller.setup()

        // 1. Direct call to launchCamera() (executes FileProvider.getUriForFile L124 successfully)
        util.launchCamera()
        assertThat(util.photoUri).isNotNull()

        // 2. Crop error with null exception error (covers exception?.message.orEmpty() null branch in L63)
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

        // 1. Success with valid tempUri
        val tempCropFile = File(activity.cacheDir, "temp_crop.png").apply { writeText("data") }
        val mockSuccessWithUri = mock<CropImageView.CropResult> {
            on { isSuccessful } doReturn true
            on { uriContent } doReturn Uri.fromFile(tempCropFile)
        }
        util.handleCropResult(mockSuccessWithUri)
        assertThat(returnedUri).isNotNull()

        // 2. Success with null uriContent
        val mockSuccessNullUri = mock<CropImageView.CropResult> {
            on { isSuccessful } doReturn true
            on { uriContent } doReturn null
        }
        util.handleCropResult(mockSuccessNullUri)
        assertThat(returnedUri).isNull()

        // 3. Error result
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

        // Camera failed
        util.handleCameraResult(false)
        assertThat(returnedUri).isNull()

        // Camera succeeded with null photoUri
        util.photoUri = null
        util.handleCameraResult(true)
        assertThat(returnedUri).isNull()

        // Camera succeeded with valid photoUri
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

        // File pick canceled (null URI)
        util.handlePickFileResult(null)
        assertThat(returnedUri).isNull()

        // File pick valid URI
        util.handlePickFileResult(Uri.parse("file:///tmp/picked.jpg"))

        // Permission denied
        util.handlePermissionResult(false)
        assertThat(returnedUri).isNull()

        // Permission granted
        util.handlePermissionResult(true)
    }
}
