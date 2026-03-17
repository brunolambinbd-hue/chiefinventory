package com.example.chiefinventory.utils

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class Ocr1NormalizerTest {

    @Test
    fun `Blank and empty input handling`() {
        assertEquals("", Ocr1Normalizer.normalize(""))
        assertEquals("", Ocr1Normalizer.normalize("   "))
        assertEquals("", Ocr1Normalizer.normalize("\n\t"))
    }

    @Test
    fun `Vietnamese to French accent mapping`() {
        assertEquals("é", Ocr1Normalizer.normalize("ế"))
        assertEquals("è", Ocr1Normalizer.normalize("ề"))
        assertEquals("â", Ocr1Normalizer.normalize("ấ"))
        assertEquals("à", Ocr1Normalizer.normalize("ầ"))
        assertEquals("a", Ocr1Normalizer.normalize("ặ"))
    }

    @Test
    fun `Tolerance symbol and  environ  removal`() {
        assertEquals("100 g", Ocr1Normalizer.normalize("± 100 g"))
        assertEquals("200 g", Ocr1Normalizer.normalize("200 g +/-"))
        assertEquals("50 g", Ocr1Normalizer.normalize("+- 50 g"))
        assertEquals("250 g", Ocr1Normalizer.normalize("environ 250 g"))
        assertEquals("250 g", Ocr1Normalizer.normalize("250 g ENVIRON"))
    }

    @Test
    fun `OCR  t  prefix removal before digits`() {
        assertEquals("10 min", Ocr1Normalizer.normalize("t 10 min"))
        assertEquals("15 min", Ocr1Normalizer.normalize("t15 min"))
    }

    @Test
    fun `Misread digit 1 as symbol or letter  T `() {
        assertEquals("1 orange", Ocr1Normalizer.normalize("I orange"))
        assertEquals("1 citron", Ocr1Normalizer.normalize("| citron"))
        assertEquals("1 citron", Ocr1Normalizer.normalize("l citron"))
        assertEquals("1 citron", Ocr1Normalizer.normalize("L citron"))
        assertEquals("1 gousse", Ocr1Normalizer.normalize("! gousse"))
        assertEquals("1 kg", Ocr1Normalizer.normalize("l kg"))
    }


    @Test
    fun `Fraction normalization for 1 2`() {
        assertEquals("1/2", Ocr1Normalizer.normalize("ll2"))
        assertEquals("1/2", Ocr1Normalizer.normalize("lI2"))
        assertEquals("1/2", Ocr1Normalizer.normalize("!!2"))
        assertEquals("1/2", Ocr1Normalizer.normalize("U2"))
        assertEquals("1/2", Ocr1Normalizer.normalize("W2"))
        assertEquals("1/2", Ocr1Normalizer.normalize("1 12"))
    }

    @Test
    fun `Fraction normalization for 1 4`() {
        assertEquals("1/4", Ocr1Normalizer.normalize("ll4"))
        assertEquals("1/4", Ocr1Normalizer.normalize("!!4"))
        assertEquals("1/4", Ocr1Normalizer.normalize("1 14"))
    }

    @Test
    fun `Fraction context preservation for mid sentence 1 2`() {
        assertEquals("1/2 citron", Ocr1Normalizer.normalize("112 citron"))
        assertEquals("1/2 oignon", Ocr1Normalizer.normalize("12 oignon"))
    }

    @Test
    fun `Number correction for  del  and  ld eau `() {
        assertEquals("de 1", Ocr1Normalizer.normalize("del"))
        assertEquals("1 l d'eau", Ocr1Normalizer.normalize("ld'eau"))
        assertEquals("1 l d'eau", Ocr1Normalizer.normalize("ld’eau"))
    }

    @Test
    fun `Article normalization for leading  1 e  and  T  `() {
        assertEquals("le citron", Ocr1Normalizer.normalize("1 e citron"))
        assertEquals("l'oignon", Ocr1Normalizer.normalize("T'oignon"))
        assertEquals("l'ail", Ocr1Normalizer.normalize("1'ail"))
    }

    @Test
    fun `NUMBER_CORRECTIONS list validation`() {
        // ld'eau → 1 l d'eau
        assertEquals("1 l d'eau", Ocr1Normalizer.normalize("ld'eau"))
        // 1 ajout → l'ajout (correction OCR inverse)
        assertEquals("l'ajout", Ocr1Normalizer.normalize("1 ajout"))
        // début de ligne avec symbole pipe | → 1
        assertEquals("1 citron", Ocr1Normalizer.normalize("| citron"))
        // faux 1 (I) avant un slash
        assertEquals("1/2", Ocr1Normalizer.normalize("I/2"))
        // pipe | isolé entre espaces → 1
        assertEquals("dose 1 dose", Ocr1Normalizer.normalize("dose | dose"))
        // del → de 1
        assertEquals("de 1 citron", Ocr1Normalizer.normalize("del citron"))
        // l ou I suivi d'une unité technique (kg, ml...) → 1
        assertEquals("1 kg", Ocr1Normalizer.normalize("l kg"))
        assertEquals("1 ml", Ocr1Normalizer.normalize("I ml"))
    }

    @Test
    fun `Linguistic spacing for apostrophes`() {
        assertEquals("jus d'orange", Ocr1Normalizer.normalize("jus d' orange"))
        assertEquals("jaune d'oeuf", Ocr1Normalizer.normalize("jauned'oeuf"))
    }

    @Test
    fun `Spelling correction for  oeuf  and  l ajout `() {
        assertEquals("1 oeuf", Ocr1Normalizer.normalize("1 euf"))
        assertEquals("2 oeufs", Ocr1Normalizer.normalize("2 eufs"))
        assertEquals("l'ajout", Ocr1Normalizer.normalize("lajout"))
    }

    @Test
    fun `OCR correction for  longueur    les   and  gris `() {
        assertEquals("longueur", Ocr1Normalizer.normalize("1ongueur"))
        assertEquals("les", Ocr1Normalizer.normalize("1 es"))
        assertEquals("gris", Ocr1Normalizer.normalize("grib"))
        assertEquals("gris", Ocr1Normalizer.normalize("gri6"))
    }

    @Test
    fun `Spoon unit normalization   Soup`() {
        assertEquals("c. à soupe", Ocr1Normalizer.normalize("cas"))
        assertEquals("c. à soupe", Ocr1Normalizer.normalize("c. à supe"))
        assertEquals("c. à soupe", Ocr1Normalizer.normalize("c. à sope"))
        assertEquals("c. à soupe", Ocr1Normalizer.normalize("c à soupe"))
    }

    @Test
    fun `Spoon unit normalization   Coffee`() {
        assertEquals("c. à café", Ocr1Normalizer.normalize("cac"))
        assertEquals("c. à café", Ocr1Normalizer.normalize("c. à café"))
        assertEquals("c. à café", Ocr1Normalizer.normalize("c à fé"))
    }

    @Test
    fun `Spoon shorthand at line start`() {
        assertEquals("1 c. à soupe", Ocr1Normalizer.normalize("lc à soupe"))
        assertEquals("1 c. à café", Ocr1Normalizer.normalize("!c. à café"))
    }

    @Test
    fun `Digit and unit separation`() {
        assertEquals("500 g", Ocr1Normalizer.normalize("500g"))
        assertEquals("1 l", Ocr1Normalizer.normalize("1l"))
    }

    @Test
    fun `Whitespace collapse and trimming`() {
        assertEquals("100 g de sucre", Ocr1Normalizer.normalize("  100   g   de   sucre  "))
    }

    @Test
    fun `Negative lookahead lookbehind for isolated fractions`() {
        // 'II2' au milieu d'un mot ne doit pas être changé
        assertEquals("HAII2A", Ocr1Normalizer.normalize("HAII2A"))
    }

    @Test
    fun `Case insensitivity check for regex rules`() {
        assertEquals("de 1", Ocr1Normalizer.normalize("DEL"))
        assertEquals("250 G", Ocr1Normalizer.normalize("250 G ENVIRON"))
        assertEquals("c. à soupe", Ocr1Normalizer.normalize("CAS"))
        assertEquals("oeuf", Ocr1Normalizer.normalize("EUF"))
    }

    @Test
    fun`Vietnamese to French accent mapping preserving case`() {
        assertEquals("é", Ocr1Normalizer.normalize("ế"))
        assertEquals("É", Ocr1Normalizer.normalize("Ế"))
        assertEquals("â", Ocr1Normalizer.normalize("ấ"))
        assertEquals("Â", Ocr1Normalizer.normalize("Ấ"))
    }

    @Test
    fun `Tolerance symbol and environ removal preserving case`() {
        assertEquals("100 g", Ocr1Normalizer.normalize("± 100 g"))
        assertEquals("200 G", Ocr1Normalizer.normalize("200 G +/-"))
        // Vérifie que 'ENVIRON' est supprimé mais que le 'G' est normalisé en 'g'
        assertEquals("250 G", Ocr1Normalizer.normalize("250 G ENVIRON"))
    }

    @Test
    fun `OCR t prefix removal before digits`() {
        assertEquals("10 min", Ocr1Normalizer.normalize("t 10 min"))
        assertEquals("15 min", Ocr1Normalizer.normalize("T 15 min"))
    }

    @Test
    fun `Misread digit 1 as symbol or letter isolated`() {
        assertEquals("1 orange", Ocr1Normalizer.normalize("I orange"))
        assertEquals("1 citron", Ocr1Normalizer.normalize("| citron"))
        assertEquals("1 gousse", Ocr1Normalizer.normalize("! gousse"))
        assertEquals("1 kg", Ocr1Normalizer.normalize("l kg"))
    }

    @Test
    fun `Misread digit 1 should NOT touch protected headers`() {
        // Le 'I' de INGRÉDIENTS doit être préservé (ou uniformisé par la règle PROTECTED)
        // mais pas transformé en '1' par erreur
        val result = Ocr1Normalizer.normalize("INGRÉDIENTS")
        Assert.assertTrue(result.contains("ingrédients", ignoreCase = true))
        Assert.assertFalse(result.startsWith("1"))
    }

    @Test
    fun `Fraction normalization`() {
        assertEquals("1/2", Ocr1Normalizer.normalize("ll2"))
        assertEquals("1/2", Ocr1Normalizer.normalize("!!2"))
        assertEquals("1/2", Ocr1Normalizer.normalize("U2"))
        assertEquals("1/4", Ocr1Normalizer.normalize("1 14"))
    }

    @Test
    fun `Number correction for del and ld eau`() {
        assertEquals("de 1", Ocr1Normalizer.normalize("del"))
        assertEquals("1 l d'eau", Ocr1Normalizer.normalize("ld'eau"))
    }

    @Test
    fun `Article normalization`() {
        assertEquals("le citron", Ocr1Normalizer.normalize("1 e citron"))
        assertEquals("l'oignon", Ocr1Normalizer.normalize("T'oignon"))
    }

    @Test
    fun `Spoon unit normalization case insensitive`() {
        assertEquals("c. à soupe", Ocr1Normalizer.normalize("CAS"))
        assertEquals("c. à café", Ocr1Normalizer.normalize("cac"))
        assertEquals("c. à café", Ocr1Normalizer.normalize("c à fé"))
    }

    @Test
    fun `Digit and unit separation with lowercase normalization`() {
        assertEquals("500 g", Ocr1Normalizer.normalize("500g"))
        assertEquals("200 g", Ocr1Normalizer.normalize("200G"))
        assertEquals("1 l", Ocr1Normalizer.normalize("1L"))
    }

    @Test
    fun `Proper name preservation`() {
        // Les noms de chefs ou d'hôtels doivent garder leur casse
        val result = Ocr1Normalizer.normalize("Nahit YILMAZ de l'Hôtel CONRAD")
        Assert.assertTrue(result.contains("Nahit"))
        Assert.assertTrue(result.contains("YILMAZ"))
        Assert.assertTrue(result.contains("Hôtel"))
        Assert.assertTrue(result.contains("CONRAD"))
    }

    @Test
    fun `Negative lookup for codes like HAII2A`() {
        // Un chiffre au milieu d'un mot ne doit pas déclencher la séparation des unités
        assertEquals("HAII2A", Ocr1Normalizer.normalize("HAII2A"))
    }

    @Test
    fun `Historical noise removal`() {
        // Vérifie la suppression des dates et prix en fin de ligne
        assertEquals("Michel Laroche", Ocr1Normalizer.normalize("Michel Laroche, 14/12/94: 209 F"))
    }


}
