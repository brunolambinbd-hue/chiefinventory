package com.chefinventory.app.utils

import android.content.Context
import android.content.res.Resources
import com.chefinventory.app.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests unitaires pour le vérificateur d'intégrité des ressources [ResourceHealthChecker].
 */
class ResourceHealthCheckerTest {

    @Test
    fun `check should report healthy when all resources are valid`() {
        val mockContext = mock<Context>()
        val mockRes = mock<Resources>()
        whenever(mockContext.resources).thenReturn(mockRes)

        val sampleArray = arrayOf("val1", "val2", "val3", "val4", "val5", "val6", "val7", "val8", "val9", "val10")
        whenever(mockRes.getStringArray(R.array.step_start_verbs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.step_secondary_verbs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.culinary_measurement_units)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.common_ingredients_no_qty)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_connectors)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr_spelling_repairs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr_diacritic_repairs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.duration_units)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.title_keywords)).thenReturn(sampleArray)

        whenever(mockRes.getString(R.string.ocr1_canonical_ingredients_header)).thenReturn("ingrédients")
        whenever(mockRes.getString(R.string.ocr1_canonical_instructions_header)).thenReturn("instructions")

        val report = ResourceHealthChecker.check(mockContext)
        assertThat(report.isHealthy).isTrue()
        assertThat(report.issues).isEmpty()
    }

    @Test
    fun `check should report issues when resources are missing or empty`() {
        val mockContext = mock<Context>()
        val mockRes = mock<Resources>()
        whenever(mockContext.resources).thenReturn(mockRes)

        whenever(mockRes.getStringArray(R.array.step_start_verbs)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.ocr1_canonical_ingredients_header)).thenReturn("")

        val report = ResourceHealthChecker.check(mockContext)
        assertThat(report.isHealthy).isFalse()
        assertThat(report.issues).isNotEmpty()
    }
}
