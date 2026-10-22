package com.example.chiefinventory.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement, le nettoyage et la détection d'accords mets-vins de [WineParser].
 */
class WineParserTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        // Charge le VRAI vocabulaire de production depuis les fichiers XML (app/src/main/res/values/*.xml)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie l'exclusion des ingrédients culinaires ("vinaigre", "huile") qui contiennent le mot "vin".
     */
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

    /**
     * Vérifie la détection d'une ligne de vin par la combinaison d'une année millésimée et d'un volume ("2015, 75cl").
     */
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

    /**
     * Vérifie le rejet des années hors limites (1900-2099).
     */
    @Test
    fun `isWineLine year format boundaries`() {
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

    /**
     * Vérifie la détection des déclinaisons de formats de volumes ("75 cl", "750ml", "1.5 l", "13.5 vol").
     */
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

    /**
     * Vérifie la détection par la liste des appellations de vins du dictionnaire XML `wine_arrays.xml`.
     */
    @Test
    fun `isWineLine appellation list match`() {
        val line = "Un excellent Saumur Champigny"
        assertTrue(
            "Appellation 'Saumur' non reconnue dans : '$line'",
            WineParser.isWineLine(line, vocabulary)
        )
    }

    /**
     * Vérifie la détection des vins et appellations internationales (*Chianti*, *Rioja*, *Barolo*, *Prosecco*, *Riesling*).
     */
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

    /**
     * Vérifie la détection par la liste des producteurs et domaines du dictionnaire XML `wine_arrays.xml`.
     */
    @Test
    fun `isWineLine producer list match`() {
        val line = "Produit par Alban de Saint-Pé"
        assertTrue(
            "Producteur 'Alban de Saint-Pé' non reconnu dans : '$line'",
            WineParser.isWineLine(line, vocabulary)
        )
    }

    /**
     * Vérifie la détection par les cépages et mots-clés d'œnologie ("Merlot", "Chardonnay").
     */
    @Test
    fun `isWineLine keyword list match`() {
        val line = "Cépage 100% Merlot"
        assertTrue(
            "Mot-clé vin 'Merlot' non reconnu dans : '$line'",
            WineParser.isWineLine(line, vocabulary)
        )
    }

    /**
     * Vérifie la détection des en-têtes de recommandations de vins ("Notre vin conseillé est...", "Suggestion").
     */
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

    /**
     * Vérifie l'insensibilité à la casse lors du matching contre les dictionnaires XML.
     */
    @Test
    fun `isWineLine case insensitivity for lists`() {
        val line1 = "Accord avec un CHARDONNAY"
        assertTrue("Insensibilité à la casse échouée pour : '$line1'", WineParser.isWineLine(line1, vocabulary))

        val line2 = "Un bon SAUMUR"
        assertTrue("Insensibilité à la casse échouée pour : '$line2'", WineParser.isWineLine(line2, vocabulary))
    }

    /**
     * Vérifie le rejet des chaînes vides, blanches ou sans caractères alphabétiques significatifs.
     */
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

    /**
     * Vérifie l'effacement du motif d'en-tête de section vin issu de `wine_remove_pattern` ("Notre vin conseillé: Un bon Merlot" -> "Un bon Merlot").
     */
    @Test
    fun `cleanWineLine removePattern application`() {
        val input = "Notre vin conseillé: Un bon Merlot"
        val expected = "Un bon Merlot"
        val actual = WineParser.cleanWineLine(input, vocabulary).trim()
        assertEquals("Nettoyage du motif d'en-tête vin échoué pour '$input'", expected, actual)
    }

    /**
     * Vérifie la suppression des préfixes d'en-têtes ("Accord : Un vin blanc sec" -> "Un vin blanc sec").
     */
    @Test
    fun `cleanWineLine extraWineCleanRegex removal`() {
        val input = "Accord : Un vin blanc sec"
        val expected = "Un vin blanc sec"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Suppression du préfixe 'Accord :' échouée pour '$input'", expected, actual)
    }

    /**
     * Vérifie le nettoyage de la ponctuation initiale parasite (" : - . Un vin fruité" -> "Un vin fruité").
     */
    @Test
    fun `cleanWineLine leading punctuation cleanup`() {
        val input = " : - . Un vin fruité"
        val expected = "Un vin fruité"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Nettoyage de la ponctuation initiale échoué pour '$input'", expected, actual)
    }

    /**
     * Vérifie la correction orthographique de "Bordeau" -> "Bordeaux".
     */
    @Test
    fun `cleanWineLine spelling correction Bordeau`() {
        val input = "Un grand Bordeau"
        val expected = "Un grand Bordeaux"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Correction orthographique de 'Bordeau' -> 'Bordeaux' échouée", expected, actual)
    }

    /**
     * Vérifie la correction orthographique de "Atinum" -> "Atinium".
     */
    @Test
    fun `cleanWineLine spelling correction Atinum`() {
        val input = "Atinum, un vin spécial"
        val expected = "Atinium, un vin spécial"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Correction orthographique de 'Atinum' -> 'Atinium' échouée", expected, actual)
    }

    /**
     * Vérifie que les corrections orthographiques ne déforment pas les mots composés contenant ces suffixes.
     */
    @Test
    fun `cleanWineLine spelling word boundary check`() {
        val input = "BordeauSuffix"
        val expected = "BordeauSuffix"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("La correction ne doit pas altérer les mots composés contenant 'Bordeau'", expected, actual)
    }

    /**
     * Vérifie l'enchaînement multi-étapes complet de nettoyage et de correction d'une ligne de vin.
     */
    @Test
    fun `cleanWineLine exhaustive multi step cleaning`() {
        val complexLine = "Suggestion: - Un Bordeau Atinum exceptionnel "
        val expected = "Un Bordeaux Atinium exceptionnel"
        val actual = WineParser.cleanWineLine(complexLine, vocabulary)
        assertEquals("Nettoyage multi-étapes combiné échoué pour '$complexLine'", expected, actual)
    }

    /**
     * Vérifie la suppression des espaces inutiles en début et fin de chaîne.
     */
    @Test
    fun `cleanWineLine result trimming`() {
        val input = "  Un vin avec des espaces  "
        val expected = "Un vin avec des espaces"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        assertEquals("Suppression des espaces inutiles en début/fin échouée pour '$input'", expected, actual)
    }
}
