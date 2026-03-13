package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class OcrCleanerTest {

    @Mock
    private lateinit var mockResources: Resources

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // Mocks par défaut pour éviter les plantages lors du chargement des ressources
        `when`(mockResources.getStringArray(R.array.advertisement_exclusions)).thenReturn(emptyArray())
        `when`(mockResources.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(emptyArray())
    }

    @Test
    fun `clean with empty input list`() {
        // Verify that providing an empty list of lines returns an empty list without errors.
        val result = OcrCleaner.clean(emptyList(), mockResources)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `clean handles whitespace only lines`() {
        // Ensure lines consisting only of spaces, tabs, or newlines are filtered out correctly.
        val input = listOf(" ", "\t", "\n", "  \t  ")
        val result = OcrCleaner.clean(input, mockResources)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `clean handles Resources NotFoundException`() {
        // Verify that if res.getStringArray throws an exception, the method defaults to empty lists 
        // and continues processing lines rather than crashing.
        `when`(mockResources.getStringArray(R.array.advertisement_exclusions)).thenThrow(RuntimeException())
        val input = listOf("Ligne normale")
        val result = OcrCleaner.clean(input, mockResources)
        assertEquals(1, result.size)
        assertEquals("Ligne normale", result[0])
    }

    @Test
    fun `clean ad exclusion via valid regex`() {
        // Test if a line matching a regex pattern defined in advertisement_exclusions 
        // (e.g., '.*promo.*') is correctly removed.
        `when`(mockResources.getStringArray(R.array.advertisement_exclusions)).thenReturn(arrayOf(".*promo.*"))
        val input = listOf("Super promo ici", "Ingrédient normal")
        val result = OcrCleaner.clean(input, mockResources)
        assertEquals(1, result.size)
        assertEquals("Ingrédient normal", result[0])
    }

    @Test
    fun `clean ad exclusion via invalid regex fallback`() {
        // Test that if an exclusion pattern is an invalid regex, the code falls back to a 
        // simple case-insensitive string containment check.
        `when`(mockResources.getStringArray(R.array.advertisement_exclusions)).thenReturn(arrayOf("[invalid-regex"))
        val input = listOf("Ceci contient [invalid-regex", "Ligne normale")
        val result = OcrCleaner.clean(input, mockResources)
        assertEquals(1, result.size)
        assertEquals("Ligne normale", result[0])
    }

    @Test
    fun `clean keyword exclusion strict match`() {
        // Verify that lines exactly matching (case-insensitive) a keyword in excluded_ocr_keywords 
        // like 'PAGE' or 'SAVEUR' are removed.
        `when`(mockResources.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(arrayOf("PAGE", "SAVEUR"))
        val input = listOf("Page", "Saveur", "Oignon")
        val result = OcrCleaner.clean(input, mockResources)
        assertEquals(1, result.size)
        assertEquals("Oignon", result[0])
    }

    @Test
    fun `clean keyword exclusion partial match retention`() {
        // Ensure that a line containing an excluded keyword as a substring but not an exact match 
        // (e.g., 'PAGE_NUMBER' vs 'PAGE') is retained.
        `when`(mockResources.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(arrayOf("PAGE"))
        val input = listOf("PAGE_NUMBER", "PAGE")
        val result = OcrCleaner.clean(input, mockResources)
        assertEquals(1, result.size)
        assertEquals("PAGE_NUMBER", result[0])
    }

    @Test
    fun `clean numeric noise removal 1 to 3 digits`() {
        // Verify that isolated numeric strings of length 1, 2, or 3 (e.g., '1', '12', '123') 
        // are identified as noise and removed.
        val input = listOf("1", "12", "123", "Oignon")
        val result = OcrCleaner.clean(input, mockResources)
        assertEquals(1, result.size)
        assertEquals("Oignon", result[0])
    }

    @Test
    fun `clean numeric noise retention 4  digits`() {
        // Ensure that numeric strings with 4 or more digits (e.g., '2024') are preserved 
        // as they might represent years or important data.
        val input = listOf("2024", "123")
        val result = OcrCleaner.clean(input, mockResources)
        assertEquals(1, result.size)
        assertEquals("2024", result[0])
    }

    @Test
    fun `clean alpha numeric noise retention`() {
        // Verify that short strings containing non-digit characters (e.g., '12a', '5g') 
        // are not removed by the numeric noise logic.
        val input = listOf("5g", "12a", "12")
        val result = OcrCleaner.clean(input, mockResources)
        assertTrue(result.contains("5g"))
        assertTrue(result.contains("12a"))
        assertFalse(result.contains("12"))
    }

    @Test
    fun `isTechnicalDimension valid  x  connector`() {
        // Check if a string like '5 mm x 5 mm' or '10x10cm' is correctly identified 
        // as a technical dimension.
        assertTrue(OcrCleaner.isTechnicalDimension("5 mm x 5 mm"))
        assertTrue(OcrCleaner.isTechnicalDimension("10x10cm"))
    }

    @Test
    fun `isTechnicalDimension valid  sur  connector`() {
        // Check if a string like '5 mm sur 5 mm' is correctly identified as a technical dimension.
        assertTrue(OcrCleaner.isTechnicalDimension("5 mm sur 5 mm"))
    }

    @Test
    fun `isTechnicalDimension case insensitive connectors`() {
        // Verify that 'SUR' or 'X' in upper case are correctly handled as connectors.
        assertTrue(OcrCleaner.isTechnicalDimension("5 MM SUR 5 MM"))
        assertTrue(OcrCleaner.isTechnicalDimension("10 X 10 CM"))
    }

    @Test
    fun `isTechnicalDimension area units detection`() {
        // Ensure units like 'mm2' or 'cm2' (e.g., '100 cm2 x 50 cm2') trigger a positive match.
        assertTrue(OcrCleaner.isTechnicalDimension("100 cm2 x 50 cm2"))
        assertTrue(OcrCleaner.isTechnicalDimension("10 mm2 sur 10 mm2"))
    }

    @Test
    fun `isTechnicalDimension dimension without connector`() {
        // Verify that a string containing a dimension but no 'x' or 'sur' (e.g., '5 mm') 
        // returns false to avoid misidentifying ingredients.
        assertFalse(OcrCleaner.isTechnicalDimension("5 mm"))
        assertFalse(OcrCleaner.isTechnicalDimension("250 g"))
    }

    @Test
    fun `isTechnicalDimension connector without dimension unit`() {
        // Ensure that a string like '5 kg x 5 kg' returns false because 'kg' is not in the 
        // DIMENSION_REGEX allowed units (mm, cm, etc.).
        assertFalse(OcrCleaner.isTechnicalDimension("5 kg x 5 kg"))
    }

    @Test
    fun `isTechnicalDimension boundary match check`() {
        // Verify that the word boundary '\b' in regex prevents matching units inside other words 
        // (e.g., '100 mm' matches but '100 mms' or '100 mmt' does not).
        assertFalse(OcrCleaner.isTechnicalDimension("100 mms x 100 mms"))
        assertTrue(OcrCleaner.isTechnicalDimension("100 mm x 100 mm"))
    }
}
