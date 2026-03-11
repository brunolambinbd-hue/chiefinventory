package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class WineParserTest {

    @Mock
    private lateinit var mockResources: Resources

    private lateinit var wineRes: WineParser.WineResources

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Mock the Android Resources to return predefined arrays for tests
        `when`(mockResources.getStringArray(R.array.wine_appellations)).thenReturn(arrayOf("saumur", "chablis"))
        `when`(mockResources.getStringArray(R.array.wine_producers)).thenReturn(arrayOf("alban de saint-pé"))
        `when`(mockResources.getStringArray(R.array.wine_keywords)).thenReturn(arrayOf("merlot", "chardonnay", "château"))
        
        // Configuration des mots-clés de titre (en minuscules pour la logique interne)
        `when`(mockResources.getStringArray(R.array.wine_title_keywords)).thenReturn(arrayOf("vin conseillé", "accord", "suggestion"))
        
        `when`(mockResources.getString(R.string.wine_remove_pattern)).thenReturn("^(Notre vin conseillé|Suggestion)")

        // Load the mocked resources into the parser's resource holder
        wineRes = WineParser.loadResources(mockResources)
    }

    @Test
    fun `loadResources standard mapping`() {
        // Verify that the method correctly maps all Android XML resource arrays and strings 
        // into the WineResources data class properties.
        assertEquals(listOf("saumur", "chablis"), wineRes.appellations)
        assertEquals(listOf("alban de saint-pé"), wineRes.producers)
        assertEquals(listOf("merlot", "chardonnay", "château"), wineRes.keywords)
        assertEquals(listOf("vin conseillé", "accord", "suggestion"), wineRes.titleKeywords)
        assertEquals("^(Notre vin conseillé|Suggestion)", wineRes.removePattern)
    }

    @Test
    fun `loadResources empty resource arrays`() {
        // Test behavior when the provided Resources object contains empty arrays or empty strings 
        // for wine-related metadata.
        `when`(mockResources.getStringArray(R.array.wine_appellations)).thenReturn(emptyArray())
        `when`(mockResources.getStringArray(R.array.wine_producers)).thenReturn(emptyArray())
        val emptyWineRes = WineParser.loadResources(mockResources)
        assertTrue(emptyWineRes.appellations.isEmpty())
        assertTrue(emptyWineRes.producers.isEmpty())
    }

    @Test
    fun `isWineLine vinaigre exclusion`() {
        // Ensure that any line containing the word 'vinaigre' returns false immediately, 
        // even if it contains a valid year, volume, or wine keyword.
        assertFalse(WineParser.isWineLine("Un bon vinaigre de vin 2018", wineRes))
        assertFalse(WineParser.isWineLine("Vinaigre de Chardonnay", wineRes))
    }

    @Test
    fun `isWineLine year and volume match`() {
        // Check if a line containing both a valid year (19xx/20xx) and a volume indicator 
        // (e.g., '2015 75cl') returns true as a strong wine indicator.
        assertTrue(WineParser.isWineLine("Une bouteille de 2015, 75cl", wineRes))
        assertTrue(WineParser.isWineLine("Millésime 1998, 1,5L", wineRes))
    }

    @Test
    fun `isWineLine year format boundaries`() {
        // Verify that years outside the 1900-2099 range (e.g., '1899' or '2101') do not 
        // trigger the wineYearRegex match.
        assertFalse("Year too old", WineParser.isWineLine("Vin de 1899, 75cl", wineRes))
        assertFalse("Year in future", WineParser.isWineLine("Futur millésime 2101, 75cl", wineRes))
    }

    @Test
    fun `isWineLine volume format variations`() {
        // Test various volume formats like '75 cl', '750ml', '1,5 l', and '13% vol' 
        // to ensure wineVolRegex captures common OCR variants.
        assertTrue(WineParser.isWineLine("Format 75 cl, 1999", wineRes))
        assertTrue(WineParser.isWineLine("Bouteille de 750ml, 2005", wineRes))
        assertTrue(WineParser.isWineLine("Magnum 1.5 l, 2020", wineRes))
        assertTrue(WineParser.isWineLine("Alcool 13.5 vol, 2021", wineRes))
    }

    @Test
    fun `isWineLine appellation list match`() {
        // Verify that a line containing a string found in the appellations resource list 
        // returns true.
        assertTrue(WineParser.isWineLine("Un excellent Saumur Champigny", wineRes))
    }

    @Test
    fun `isWineLine producer list match`() {
        // Verify that a line containing a string found in the producers resource list 
        // returns true.
        assertTrue(WineParser.isWineLine("Produit par Alban de Saint-Pé", wineRes))
    }

    @Test
    fun `isWineLine keyword list match`() {
        // Verify that a line containing a string found in the general keywords resource list 
        // returns true.
        assertTrue(WineParser.isWineLine("Cépage 100% Merlot", wineRes))
    }

    @Test
    fun `isWineLine title keyword list match`() {
        // Verify that a line containing a string found in the titleKeywords resource list 
        // returns true.
        assertTrue(WineParser.isWineLine("Notre vin conseillé est...", wineRes))
        assertTrue("Detection of 'Suggestion' title keyword", WineParser.isWineLine("Suggestion", wineRes))
    }

    @Test
    fun `isWineLine case insensitivity for lists`() {
        // Ensure that matches against resource lists are case-insensitive.
        assertTrue(WineParser.isWineLine("Accord avec un CHARDONNAY", wineRes))
        assertTrue(WineParser.isWineLine("Un bon SAUMUR", wineRes))
    }

    @Test
    fun `isWineLine empty or blank string`() {
        // Check that passing an empty or whitespace-only string returns false without
        // throwing exceptions.
        assertFalse("Empty string", WineParser.isWineLine("", wineRes))
        assertFalse("Spaces only", WineParser.isWineLine("      ", wineRes))
        assertFalse("Tabulations", WineParser.isWineLine("\t\t", wineRes))
        assertFalse("New lines", WineParser.isWineLine("\n\n", wineRes))

        // Edge cases: very short strings or special characters only
        assertFalse("Single char", WineParser.isWineLine("a", wineRes))
        assertFalse("Single digit", WineParser.isWineLine("1", wineRes))
        assertFalse("Special chars", WineParser.isWineLine(".:!", wineRes))
    }

    @Test
    fun `cleanWineLine removePattern application`() {
        // Verify that the dynamic regex provided in WineResources.removePattern is 
        // correctly removed from the input string.
        val result = WineParser.cleanWineLine("Notre vin conseillé: Un bon Merlot", wineRes)
        assertEquals("Un bon Merlot", result.trim())
    }

    @Test
    fun `cleanWineLine extraWineCleanRegex removal`() {
        // Test removal of specific French prefix keywords like 'accord :', 'suggestion :', 
        // or 'boire' at the start of the line.
        val result = WineParser.cleanWineLine("Accord : Un vin blanc sec", wineRes)
        assertEquals("Un vin blanc sec", result)
    }

    @Test
    fun `cleanWineLine leading punctuation cleanup`() {
        // Ensure leading characters like colons, dashes, dots, and spaces are stripped 
        // from the beginning of the line after keyword removal.
        val result = WineParser.cleanWineLine(" : - . Un vin fruité", wineRes)
        assertEquals("Un vin fruité", result)
    }

    @Test
    fun `cleanWineLine spelling correction Bordeau`() {
        // Verify that the OCR-misread 'Bordeau' is corrected to 'Bordeaux' while 
        // ignoring case.
        val result = WineParser.cleanWineLine("Un grand Bordeau", wineRes)
        assertEquals("Un grand Bordeaux", result)
    }

    @Test
    fun `cleanWineLine spelling correction Atinum`() {
        // Verify that the OCR-misread 'Atinum' is corrected to 'Atinium' while 
        // ignoring case.
        val result = WineParser.cleanWineLine("Atinum, un vin spécial", wineRes)
        assertEquals("Atinium, un vin spécial", result)
    }

    @Test
    fun `cleanWineLine spelling word boundary check`() {
        // Ensure spelling corrections only apply to whole words.
        assertEquals("BordeauSuffix", WineParser.cleanWineLine("BordeauSuffix", wineRes))
    }

    @Test
    fun `cleanWineLine exhaustive multi step cleaning`() {
        // Test a complex string requiring removal of patterns, prefixes, leading symbols, 
        // and spelling corrections in a single pass.
        val complexLine = "Suggestion: - Un Bordeau Atinum exceptionnel "
        val result = WineParser.cleanWineLine(complexLine, wineRes)
        assertEquals("Un Bordeaux Atinium exceptionnel", result)
    }

    @Test
    fun `cleanWineLine result trimming`() {
        // Confirm that the final output string is properly trimmed.
        val result = WineParser.cleanWineLine("  Un vin avec des espaces  ", wineRes)
        assertEquals("Un vin avec des espaces", result)
    }
}
