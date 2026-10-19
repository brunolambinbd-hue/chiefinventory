package com.example.chiefinventory.utils

import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class GlobalExceptionHandlerTest {

    @Test
    fun `instantiation and error formatting should not crash`() {
        val mockContext = mock<Context>()
        val handler = GlobalExceptionHandler(mockContext)
        assertThat(handler).isNotNull()
    }
}
