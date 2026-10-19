package com.example.chiefinventory.utils

import android.content.Context
import com.example.chiefinventory.R
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SignatureUtilsTest {

    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mock {
            on { getString(R.string.signature_status_empty) } doReturn "Empty"
            on { getString(R.string.signature_status_missing) } doReturn "Missing"
            on { getString(eq(R.string.signature_preview_format), anyString()) } doAnswer { invocation ->
                val preview = invocation.arguments[1] as String
                "Preview: $preview"
            }
        }
    }

    @Test
    fun formatSignaturePreview_nullByteArray_returnsMissing() {
        val result = SignatureUtils.formatSignaturePreview(mockContext, null as ByteArray?)
        assertThat(result).isEqualTo("Missing")
    }

    @Test
    fun formatSignaturePreview_emptyByteArray_returnsEmpty() {
        val result = SignatureUtils.formatSignaturePreview(mockContext, ByteArray(0))
        assertThat(result).isEqualTo("Empty")
    }

    @Test
    fun formatSignaturePreview_validByteArray_returnsFormattedString() {
        val floats = floatArrayOf(0.12f, -0.45f)
        val buffer = ByteBuffer.allocate(floats.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (f in floats) {
            buffer.putFloat(f)
        }
        val result = SignatureUtils.formatSignaturePreview(mockContext, buffer.array())
        val normalizedResult = result.replace(Regex("(?<=\\d),(?=\\d)"), ".")
        assertThat(normalizedResult).isEqualTo("Preview: 0.12, -0.45")
    }

    @Test
    fun formatSignaturePreview_nullFloatArray_returnsMissing() {
        val result = SignatureUtils.formatSignaturePreview(mockContext, null as FloatArray?)
        assertThat(result).isEqualTo("Missing")
    }

    @Test
    fun formatSignaturePreview_emptyFloatArray_returnsEmpty() {
        val result = SignatureUtils.formatSignaturePreview(mockContext, FloatArray(0))
        assertThat(result).isEqualTo("Empty")
    }

    @Test
    fun formatSignaturePreview_validFloatArray_returnsFormattedString() {
        val result = SignatureUtils.formatSignaturePreview(mockContext, floatArrayOf(0.12f, -0.45f, 1.0f, 2.0f, 3.0f, 4.0f))
        val normalizedResult = result.replace(Regex("(?<=\\d),(?=\\d)"), ".")
        assertThat(normalizedResult).isEqualTo("Preview: 0.12, -0.45, 1.00, 2.00, 3.00")
    }
}
