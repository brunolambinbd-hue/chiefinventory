package com.example.chiefinventory.utils

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class BitmapUtilsTest {

    @Test
    fun getBitmapFromUri_withMockedContext_catchesExceptionAndReturnsNull() {
        val mockContext: Context = mock()
        val mockUri: Uri = mock()

        val result = BitmapUtils.getBitmapFromUri(mockContext, mockUri)

        assertThat(result).isNull()
    }
}
