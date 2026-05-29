package com.example.parabdcollector.utils

import android.content.Context
import com.example.parabdcollector.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Unit tests for [SignatureUtils].
 */
class SignatureUtilsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = mock()
    }

    @Test
    fun `formatSignaturePreview with null float array should return missing status`() {
        whenever(context.getString(R.string.signature_status_missing)).thenReturn("Manquante")
        
        val result = SignatureUtils.formatSignaturePreview(context, null as FloatArray?)
        
        assertEquals("Manquante", result)
    }

    @Test
    fun `formatSignaturePreview with empty float array should return empty status`() {
        whenever(context.getString(R.string.signature_status_empty)).thenReturn("Vide")
        
        val result = SignatureUtils.formatSignaturePreview(context, floatArrayOf())
        
        assertEquals("Vide", result)
    }

    @Test
    fun `formatSignaturePreview with float array should format values and use preview format`() {
        val embedding = floatArrayOf(0.123f, -0.456f, 0.789f, 1.0f, -1.0f, 9.9f)
        // Mock the format string. In production it's "[%s,...]"
        whenever(context.getString(any(), any())).thenAnswer { invocation ->
            val format = "[%s,...]"
            val arg = invocation.getArgument<String>(1)
            format.format(arg)
        }

        val result = SignatureUtils.formatSignaturePreview(context, embedding)
        
        // Should take first 5, formatted to 2 decimals
        assertEquals("[0.12, -0.46, 0.79, 1.00, -1.00,...]", result)
    }

    @Test
    fun `formatSignaturePreview with byte array should convert and format`() {
        // Create a byte array from floats
        val floats = floatArrayOf(0.5f, -0.5f)
        val buffer = ByteBuffer.allocate(floats.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        floats.forEach { buffer.putFloat(it) }
        val bytes = buffer.array()

        whenever(context.getString(any(), any())).thenAnswer { invocation ->
            "Preview: ${invocation.getArgument<String>(1)}"
        }

        val result = SignatureUtils.formatSignaturePreview(context, bytes)
        
        assertEquals("Preview: 0.50, -0.50", result)
    }

    @Test
    fun `formatSignaturePreview with null byte array should return missing status`() {
        whenever(context.getString(R.string.signature_status_missing)).thenReturn("Missing")
        
        val result = SignatureUtils.formatSignaturePreview(context, null as ByteArray?)
        
        assertEquals("Missing", result)
    }

    @Test
    fun `formatSignaturePreview with empty byte array should return empty status`() {
        whenever(context.getString(R.string.signature_status_empty)).thenReturn("Empty")
        
        val result = SignatureUtils.formatSignaturePreview(context, byteArrayOf())
        
        assertEquals("Empty", result)
    }
}
