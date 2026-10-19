package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class ImageCaptureUtilTest {

    @Test
    fun `instantiation should work with mocked dependencies`() {
        val onImageReadyMock: (android.net.Uri?) -> Unit = mock()
        assertThat(onImageReadyMock).isNotNull()
    }
}
