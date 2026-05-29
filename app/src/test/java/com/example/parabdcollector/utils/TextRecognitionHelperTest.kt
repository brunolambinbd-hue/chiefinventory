package com.example.parabdcollector.utils

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TextRecognitionHelperTest {

    @Test
    fun `extractText should filter words correctly`() = runTest {
        val mockBitmap = mock(Bitmap::class.java)
        val mockImage = mock(InputImage::class.java)
        val mockText = mock(Text::class.java)
        val mockTask = mock(Task::class.java) as Task<Text>
        val mockRecognizer = mock(TextRecognizer::class.java)

        // Simule le texte brut renvoyé par ML Kit
        whenever(mockText.text).thenReturn("Dupuis 2024 BD 118/300 45 Album a")

        mockStatic(TextRecognition::class.java).use { textRecognitionMock ->
            textRecognitionMock.`when`<TextRecognizer> { TextRecognition.getClient(any()) }.thenReturn(mockRecognizer)

            mockStatic(InputImage::class.java).use { inputImageMock ->
                inputImageMock.`when`<InputImage> { InputImage.fromBitmap(any(), anyInt()) }.thenReturn(mockImage)
                whenever(mockRecognizer.process(mockImage)).thenReturn(mockTask)

                mockStatic(Tasks::class.java).use { tasksMock ->
                    tasksMock.`when`<Text> { Tasks.await(mockTask) }.thenReturn(mockText)

                    val result = TextRecognitionHelper.extractText(mockBitmap, UnconfinedTestDispatcher(testScheduler))

                    // Vérifie le filtrage :
                    // - "dupuis", "bd", "album" sont gardés
                    // - "2024", "45" sont filtrés (chiffres)
                    // - "118/300" est filtré (regex)
                    // - "a" est filtré (trop court)
                    val expected = listOf("dupuis", "bd", "album")
                    assertEquals(expected, result)
                }
            }
        }
    }

    @Test
    fun `filterWords should cleanup and filter text correctly`() {
        val raw = "  TEST  123 12/34  bd  "
        val result = TextRecognitionHelper.filterWords(raw)
        assertEquals(listOf("test", "bd"), result)
    }
}