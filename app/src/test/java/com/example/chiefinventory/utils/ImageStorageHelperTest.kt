package com.example.chiefinventory.utils

import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class ImageStorageHelperTest {

    @Test
    fun saveImageToInternalStorage_withMockedContext_returnsNullOnFailure() {
        val mockContext = mock<Context>()
        val result = ImageStorageHelper.saveImageToInternalStorage(mockContext, mock())
        assertThat(result).isNull()
    }
}
