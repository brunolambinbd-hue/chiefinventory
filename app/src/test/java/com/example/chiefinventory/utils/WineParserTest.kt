package com.example.chiefinventory.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WineParserTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        // Charge le VRAI vocabulaire de production depuis les fichiers XML (app/src/main/res/values/*.xml)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `isWineLine vinaigre exclusion`() {
        val line1 = "Un bon vinaigre de vin 2018"
        assertFalse(
            "Exclusion culinaire : la ligne '$line1' ne doit PAS être identifiée comme du vin (contient 'vinaigre')",
            WineParser.isWineLine(line1, vocabulary)
        )

        val line2 = "Vinaigre de Chardonnay"
        assertFalse(
            "Exclusion culinaire : la ligne '$line2' ne doit PAS être identifiée comme du vin (contient 'vinaigre')",
            WineParser.isWineLine(line2, vocabulary)
        )
    }

    @Test
    fun `isWineLine year and volume match`() {
        val line1 = "Une bouteille de 2015, 75cl"
        assertTrue(
            "Détection année + volume échouée pour '$line1'",
            WineParser.isWineLine(line1, vocabulary)
        )

        val line2 = "Millésime 1998, 1,5L"
        assertTrue(
            "Détection année + volume échouée pour '$line2'",
            WineParser.isWineLine(line2, vocabulary)
        )
    }

    @Test
    fun `isWineLine year format boundaries`() {
        // Utilisation d'une chaîne neutre "Produit" sans aucun mot-clé de dictionnaire ("vin", "boisson", etc.)
        val line1 = "Produit de 1899, 500ml"
        assertFalse(
            "Année trop ancienne (hors plage 1900-2099) : '$line1' ne devrait pas être du vin",
            WineParser.isWineLine(line1, vocabulary)
        )

        val line2 = "Produit de 2101, 500ml"
        assertFalse(
            "Année future (hors plage 1900-2099) : '$line2' ne devrait pas être du vin",
            WineParser.isWineLine(line2, vocabulary)
        )
    }

    @Test
    fun `isWineLine volume format variations`() {
        val testLines = listOf(
            "Format 75 cl, 1999",
            "Bouteille de 750ml, 2005",
            "Magnum 1.5 l, 2020",
            "Alcool 13.5 vol, 2021"
        )
        testLines.forEach { line ->
            assertTrue(
                "Variation de format de volume non reconnue pour : '$line'",
                WineParser.isWineLine(line, vocabulary)
            )
        }
    }

    @Test
    fun `isWineLine appellation list match`() {
        val line = "Un excellent Saumur Champigny"
        assertTrue(
            "Appellation 'Saumur' non reconnue dans : '$line'",
            WineParser.isWineLine(line, vocabulary)
        )
    }

    @Test
    fun `isWineLine international appellations match`() {
        val internationalWines = listOf(
            "Chianti Classico 2018, 75 cl",
            "Rioja Reserva, 750ml",
            "Barolo DOCG, 2016",
            "Prosecco Superiore",
            "Napa Valley Cabernet Sauvignon",
            "Malbec de Mendoza",
            "Riesling Kabinett, 75 cl",
            "Vinho Verde, 75 cl"
        )
        internationalWines.forEach { line ->
            assertTrue(
                "Appellation internationale non reconnue pour : '$line'",
                WineParser.isWineLine(line, vocabulary)
            )
        }
    }

    @Test
    fun `isWineLine producer list match`() {
        val line = "Produit par Alban de Saint-Pé"
        assertTrue(
            "Producteur 'Alban de Saint-Pé' non reconnu dans : '$line'",
            WineParser.isWineLine(line, vocabulary)
        )
    }

    @Test
    fun `isWineLine keyword list match`() {
        val line = "Cépage 100% Merlot"
        assertTrue(
            "Mot-clé vin 'Merlot' non reconnu dans : '$line'",
            WineParser.isWineLine(line, vocabulary)
        )
    }

    @Test
    fun `isWineLine title keyword list match`() {
        val line1 = "Notre vin conseillé est..."
        assertTrue(
            "En-tête 'vin conseillé' non reconnu dans : '$line1'",
            WineParser.isWineLine(line1, vocabulary)
        )

        val line2 = "Suggestion"
        assertTrue(
            "En-tête 'Suggestion' non reconnu pour : '$line2'",
            WineParser.isWineLine(line2, vocabulary)
        )
    }

    @Test
    fun `isWineLine case insensitivity for lists`() {
        val line1 = "Accord avec un CHARDONNAY"
        assertTrue("Insensibilité à la casse échouée pour : '$line1'", WineParser.isWineLine(line1, vocabulary))

        val line2 = "Un bon SAUMUR"
        assertTrue("Insensibilité à la casse échouée pour : '$line2'", WineParser.isWineLine(line2, vocabulary))
    }

    @Test
    fun `isWineLine empty or blank string`() {
        val invalidLines = mapOf(
            "" to "Chaîne vide",
            "      " to "Espaces uniquement",
            "\t\t" to "Tabulations",
            "\n\n" to "Sauts de ligne",
            "a" to "Caractère unique",
            "1" to "Chiffre unique",
            ".:!" to "Caractères spéciaux"
        )
        invalidLines.forEach { (line, reason) ->
            assertFalse(
                "Refus de ligne invalide échoué ($reason) pour : '$line'",
                WineParser.isWineLine(line, vocabulary)
            )
        }
    }

    @Test
    fun `cleanWineLine removePattern application`() {
        val input = "Notre vin conseillé: Un bon Merlot"
        val expected = "Un bon Merlot"
        val actual = WineParser.cleanWineLine(input, vocabulary).trim()
        assertEquals("Nettoyage du motif d'en-tête vin échoué pour '$input'", expected, actual)
    }

    @Test
    fun `cleanWineLine extraWineCleanRegex removal`() {
        val input = "Accord : Un vin blanc sec"
        val expected = "Un vin blanc sec"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Suppression du préfixe 'Accord :' échouée pour '$input'", expected, actual)
    }

    @Test
    fun `cleanWineLine leading punctuation cleanup`() {
        val input = " : - . Un vin fruité"
        val expected = "Un vin fruité"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Nettoyage de la ponctuation initiale échoué pour '$input'", expected, actual)
    }

    @Test
    fun `cleanWineLine spelling correction Bordeau`() {
        val input = "Un grand Bordeau"
        val expected = "Un grand Bordeaux"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Correction orthographique de 'Bordeau' -> 'Bordeaux' échouée", expected, actual)
    }

    @Test
    fun `cleanWineLine spelling correction Atinum`() {
        val input = "Atinum, un vin spécial"
        val expected = "Atinium, un vin spécial"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Correction orthographique de 'Atinum' -> 'Atinium' échouée", expected, actual)
    }

    @Test
    fun `cleanWineLine spelling word boundary check`() {
        val input = "BordeauSuffix"
        val expected = "BordeauSuffix"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("La correction ne doit pas altérer les mots composés contenant 'Bordeau'", expected, actual)
    }

    @Test
    fun `cleanWineLine exhaustive multi step cleaning`() {
        val complexLine = "Suggestion: - Un Bordeau Atinum exceptionnel "
        val expected = "Un Bordeaux Atinium exceptionnel"
        val actual = WineParser.cleanWineLine(complexLine, vocabulary)
        assertEquals("Nettoyage multi-étapes combiné échoué pour '$complexLine'", expected, actual)
    }

    @Test
    fun `cleanWineLine result trimming`() {
        val input = "  Un vin avec des espaces  "
        val expected = "Un vin avec des espaces"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Suppression des espaces inutiles en début/fin échouée pour '$input'", expected, actual)
    }
}
