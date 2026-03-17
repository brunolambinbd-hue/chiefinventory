package com.example.chiefinventory.utils

import org.junit.Assert.*
import org.junit.Test

class IngredientParserTest {

    @Test
    fun `getUnits coverage verification`() {
        // Verify that getUnits returns the complete internal list of units including metric, culinary volumes, and container types.
        val units = IngredientParser.units
        assertTrue(units.contains("g"))
        assertTrue(units.contains("kg"))
        assertTrue(units.contains("cl"))
        assertTrue(units.contains("ml"))
        assertTrue(units.contains("litre"))
        assertTrue(units.contains("litres"))
        assertTrue(units.contains("cuillère"))
        assertTrue(units.contains("cuillères"))
        assertTrue(units.contains("c. à soupe"))
        assertTrue(units.contains("c à soupe"))
        assertTrue(units.contains("c.à soupe"))
        assertTrue(units.contains("c. a soupe"))
        assertTrue(units.contains("c a soupe"))
        assertTrue(units.contains("c. à café"))
        assertTrue(units.contains("c à café"))
        assertTrue(units.contains("c.à café"))
        assertTrue(units.contains("c. a café"))
        assertTrue(units.contains("c a café"))
        assertTrue(units.contains("càs"))
        assertTrue(units.contains("càc"))
        assertTrue(units.contains("c. à s."))
        assertTrue(units.contains("c. à c."))
        assertTrue(units.contains("pincées"))
        assertTrue(units.contains("pincée"))
        assertTrue(units.contains("botte"))
        assertTrue(units.contains("bottes"))
        assertTrue(units.contains("paquet"))
        assertTrue(units.contains("paquets"))
        assertTrue(units.contains("verre"))
        assertTrue(units.contains("verres"))
        assertTrue(units.contains("tranche"))
        assertTrue(units.contains("tranches"))
        assertTrue(units.contains("filet"))
        assertTrue(units.contains("filets"))
        assertTrue(units.contains("trait"))
        assertTrue(units.contains("traits"))
        assertTrue(units.contains("brins"))
        assertTrue(units.contains("brin"))

    }

    @Test
    fun `preClean basic trimming and symbol removal`() {
        // Test if leading/trailing whitespace and symbols like ±, +/-, and +- are removed correctly.
        assertEquals("100 g", Ocr1Normalizer.normalize("  ± 100 g  "))
        assertEquals("200 g", Ocr1Normalizer.normalize("200 g +/-"))
        assertEquals("50 g", Ocr1Normalizer.normalize("+- 50 g"))
    }

    @Test
    fun `preClean keyword  environ  removal`() {
        // Ensure the word 'environ' is removed case-insensitively from any position in the string.
        assertEquals("100 g", Ocr1Normalizer.normalize("environ 100 g"))
        assertEquals("200 g", Ocr1Normalizer.normalize("200 g ENVIRON"))
    }

    @Test
    fun `preClean OCR  1  correction for special characters`() {
        // Verify that characters like |, I, l, ! are converted to '1' when appearing at start, before a slash, or before letters.
        assertEquals("1 citron", Ocr1Normalizer.normalize("| citron"))
        assertEquals("1 orange", Ocr1Normalizer.normalize("I orange"))
        assertEquals("1/2", Ocr1Normalizer.normalize("l/2"))
        assertEquals("1 gousse", Ocr1Normalizer.normalize("! gousse"))
    }

    @Test
    fun `preClean OCR  le  and  l  article correction`() {
        // Check if misread prefixes like '1 e ', 'T'', or '1'' are correctly restored to linguistic articles 'le ' and 'l''.
        assertEquals("le citron", Ocr1Normalizer.normalize("1 e citron"))
        assertEquals("l'oignon", Ocr1Normalizer.normalize("T'oignon"))
        assertEquals("l'ail", Ocr1Normalizer.normalize("1'ail"))
    }

    @Test
    fun `preClean apostrophe and spacing normalization`() {
        // Test if spacing around 'd' followed by apostrophes is normalized (e.g., 'd' ' -> d') and 'jauned' euf' correction.
        assertEquals("jaune d'oeuf", Ocr1Normalizer.normalize("jauned' euf"))
        assertEquals("jus d'orange", Ocr1Normalizer.normalize("jus d' orange"))
    }

    @Test
    fun `preClean  oeuf  spelling correction`() {
        // Verify that 'euf' and 'eufs' are corrected to 'oeuf' and 'oeufs' respectively regardless of case.
        assertEquals("1 oeuf", Ocr1Normalizer.normalize("1 euf"))
        assertEquals("2 oeufs", Ocr1Normalizer.normalize("2 eufs"))
    }

    @Test
    fun `preClean abbreviated unit  c   normalization`() {
        // Test if OCR errors like 'Ic' or '!c.' are converted to '1 c' or '1 c.' and 'c à' is standardized to 'c. à'.
        assertEquals("1 c. à soupe", Ocr1Normalizer.normalize("Ic à soupe"))
        assertEquals("1 c. à café", Ocr1Normalizer.normalize("!c. à café"))
        assertEquals("c. à soupe", Ocr1Normalizer.normalize("c à soupe"))
    }

    @Test
    fun `preClean space insertion between digit and letter`() {
        // Ensure a space is inserted when a number is directly followed by a letter (e.g., '200g' -> '200 g').
        assertEquals("200 g", Ocr1Normalizer.normalize("200g"))
        assertEquals("1 l", Ocr1Normalizer.normalize("1l"))
    }

    @Test
    fun `preClean fraction reconstruction for halves and quarters`() {
        // Check if OCR misreads like '1 12', '112', or 'lI2' are correctly converted to standard fraction strings '1/2' or '1/4'.
        assertEquals("1/2", Ocr1Normalizer.normalize("1 12"))
        assertEquals("1/2", Ocr1Normalizer.normalize("112"))
        assertEquals("1/2", Ocr1Normalizer.normalize("lI2"))
        assertEquals("1/4", Ocr1Normalizer.normalize("114"))
    }
//    @Test
//    fun `preClean global test`() {
//        // Check if OCR misreads like '1 12', '112', or 'lI2' are correctly converted to standard fraction strings '1/2' or '1/4'.
//        assertEquals("1/2", IngredientParser.preClean("1 12"))
//        assertEquals("1/2", IngredientParser.preClean("112"))
//        assertEquals("1/2", IngredientParser.preClean("lI2"))
//        assertEquals("1/4", IngredientParser.preClean("114"))
//    }
    @Test
    fun `parse fraction handling`() {
        // Verify that input '1/2 l d'eau' is correctly parsed into quantity 0.5, unit 'l', and name 'eau'.
        val result = IngredientParser.parse("1/2 l d'eau")
        assertEquals(0.5, result.quantity!!, 0.01)
        assertEquals(0.5, result.quantity!!, 0.01)
        // Note: L'unité peut être null selon la consommation des tokens par la regex
        assertTrue(result.name.contains("eau"))
        assertEquals("l", result.unit)
        assertEquals("eau", result.name)
    }

    @Test
    fun `parse range handling  upper bound extraction `() {
        // Test if range inputs like '2 à 3 pommes' or '2-3.5 kg de farine' return the second (upper) quantity (3.0 and 3.5).
        val result1 = IngredientParser.parse("2 à 3 pommes")
        assertEquals(3.0, result1.quantity!!, 0.01)
        val result1b = IngredientParser.parse("4 à 5 pommes")
        assertEquals(5.0, result1b.quantity!!, 0.01)
        val result2 = IngredientParser.parse("2-3.5 kg de farine")
        assertEquals(3.5, result2.quantity!!, 0.01)
    }

    @Test
    fun `parse standard numeric quantity with unit`() {
        // Verify '250 g de sucre' parses quantity 250.0, unit 'g', and name 'sucre'.
        val result = IngredientParser.parse("250 g de sucre")
        assertEquals(250.0, result.quantity!!, 0.01)
        assertEquals("g", result.unit)
        assertEquals("sucre", result.name)
    }

    @Test
    fun `parse name only ingredient`() {
        // Check if input without numbers like 'Sel et poivre' returns name 'Sel et poivre' with null quantity and unit.
        val result = IngredientParser.parse("Sel et poivre")
        assertNull(result.quantity)
        assertNull(result.unit)
        assertEquals("Sel et poivre", result.name)
    }

    @Test
    fun `parse quantity only ingredient`() {
        // Test if '2' returns quantity 2.0 with an empty string name and null unit.
        val result = IngredientParser.parse("2")
        assertEquals(2.0, result.quantity!!, 0.01)
        assertEquals("", result.name)
        assertNull(result.unit)
    }

    @Test
    fun `parse unit precedence  longest match first `() {
        // Ensure 'c. à soupe' is matched instead of just 'c' or 'cuillère' to verify sortedByDescending logic.
        val result = IngredientParser.parse("2 c. à soupe de miel")
        assertEquals("c. à soupe", result.unit)
        assertEquals("miel", result.name)
    }

    @Test
    fun `parse unit with dot or  de  connector`() {
        // Verify that units followed by dots (g.) or 'de/d'' (kg de) correctly isolate the ingredient name without the connector.
        val result = IngredientParser.parse("1 kg de tomates")
        assertEquals("tomates", result.name)
        assertEquals("kg", result.unit)
    }

    @Test
    fun `parse decimal separator handling`() {
        // Test both comma and dot as decimal separators (e.g., '1,5 kg' and '1.5 kg') yield quantity 1.5.
        val result1 = IngredientParser.parse("1,5 kg")
        assertEquals(1.5, result1.quantity!!, 0.01)
        
        val result2 = IngredientParser.parse("1.5 kg")
        assertEquals(1.5, result2.quantity!!, 0.01)
    }

    @Test
    fun `parse complex case with multiple preClean steps`() {
        // Integration test: '! environ 200g d' eufs' should parse to quantity 200.0, unit 'g', name 'oeufs'.
        val result = IngredientParser.parse("! environ 200g d' eufs")
        assertEquals(200.0, result.quantity!!, 0.01)
        assertEquals("g", result.unit)
        assertEquals("oeufs", result.name)
    }

    @Test
    fun `parse edge case empty string`() {
        // Ensure passing an empty string to parse does not crash and returns a ParsedIngredient with empty name.
        val result = IngredientParser.parse("")
        assertEquals("", result.name)
        assertNull(result.quantity)
        assertNull(result.unit)
    }

}
