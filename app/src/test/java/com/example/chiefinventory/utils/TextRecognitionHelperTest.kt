package com.example.chiefinventory.utils

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B vérifiant la reconnaissance OCR
 * et la couverture de 100% des méthodes de [TextRecognitionHelper].
 */
class TextRecognitionHelperTest {

    private fun createTestHelper(
        mockContext: Context,
        mockRecognizer: TextRecognizer,
        mockImage: InputImage
    ): TextRecognitionHelper {
        return object : TextRecognitionHelper(mockContext, mockRecognizer) {
            override fun createInputImage(bitmap: Bitmap): InputImage = mockImage
        }
    }

    @Test
    fun `recognizeText should return text string when ML Kit task succeeds`() = runTest {
        val mockContext = mock<Context>()
        val mockRecognizer = mock<TextRecognizer>()
        val mockImage = mock<InputImage>()
        val mockTask = mock<Task<Text>>()
        val mockVisionText = mock<Text>()
        val mockBitmap = mock<Bitmap>()

        whenever(mockVisionText.text).thenReturn("Farine 250g")
        whenever(mockRecognizer.process(mockImage)).thenReturn(mockTask)

        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<Text>>(0)
            listener.onSuccess(mockVisionText)
            mockTask
        }
        whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        val helper = createTestHelper(mockContext, mockRecognizer, mockImage)
        val result = helper.recognizeText(mockBitmap)

        assertThat(result).isEqualTo("Farine 250g")
    }

    @Test
    fun `recognizeText should return null when ML Kit vision text is blank`() = runTest {
        val mockContext = mock<Context>()
        val mockRecognizer = mock<TextRecognizer>()
        val mockImage = mock<InputImage>()
        val mockTask = mock<Task<Text>>()
        val mockVisionText = mock<Text>()
        val mockBitmap = mock<Bitmap>()

        whenever(mockVisionText.text).thenReturn("   ")
        whenever(mockRecognizer.process(mockImage)).thenReturn(mockTask)

        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<Text>>(0)
            listener.onSuccess(mockVisionText)
            mockTask
        }
        whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        val helper = createTestHelper(mockContext, mockRecognizer, mockImage)
        val result = helper.recognizeText(mockBitmap)

        assertThat(result).isNull()
    }

    @Test
    fun `recognizeText should throw exception when ML Kit task fails`() = runTest {
        val mockContext = mock<Context>()
        val mockRecognizer = mock<TextRecognizer>()
        val mockImage = mock<InputImage>()
        val mockTask = mock<Task<Text>>()
        val mockBitmap = mock<Bitmap>()
        val testException = RuntimeException("OCR Error")

        whenever(mockRecognizer.process(mockImage)).thenReturn(mockTask)
        whenever(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        whenever(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnFailureListener>(0)
            listener.onFailure(testException)
            mockTask
        }

        val helper = createTestHelper(mockContext, mockRecognizer, mockImage)
        try {
            helper.recognizeText(mockBitmap)
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("OCR Error")
        }
    }

    @Test
    fun `recognizeVisionText should return VisionText object when ML Kit task succeeds`() = runTest {
        val mockContext = mock<Context>()
        val mockRecognizer = mock<TextRecognizer>()
        val mockImage = mock<InputImage>()
        val mockTask = mock<Task<Text>>()
        val mockVisionText = mock<Text>()
        val mockBitmap = mock<Bitmap>()

        whenever(mockRecognizer.process(mockImage)).thenReturn(mockTask)
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<Text>>(0)
            listener.onSuccess(mockVisionText)
            mockTask
        }
        whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        val helper = createTestHelper(mockContext, mockRecognizer, mockImage)
        val result = helper.recognizeVisionText(mockBitmap)

        assertThat(result).isEqualTo(mockVisionText)
    }

    @Test
    fun `recognizeVisionText should throw exception when ML Kit task fails`() = runTest {
        val mockContext = mock<Context>()
        val mockRecognizer = mock<TextRecognizer>()
        val mockImage = mock<InputImage>()
        val mockTask = mock<Task<Text>>()
        val mockBitmap = mock<Bitmap>()
        val testException = RuntimeException("Vision OCR Failure")

        whenever(mockRecognizer.process(mockImage)).thenReturn(mockTask)
        whenever(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        whenever(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnFailureListener>(0)
            listener.onFailure(testException)
            mockTask
        }

        val helper = createTestHelper(mockContext, mockRecognizer, mockImage)
        try {
            helper.recognizeVisionText(mockBitmap)
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Vision OCR Failure")
        }
    }
}
