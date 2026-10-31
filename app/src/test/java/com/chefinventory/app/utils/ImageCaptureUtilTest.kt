package com.chefinventory.app.utils

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
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
