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

    @Test
    fun `isWineLine should detect Spanish producer Marques de Riscal and DO rueda`() {
        val line = "Marqués de Riscal, DO rueda 2010"
        assertTrue(
            "Détection producteur/appellation espagnole échouée pour '$line'",
            WineParser.isWineLine(line, vocabulary)
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
     * Vérifie l'effacement du motif d'en-tête de section vin ("Notre vin conseillé: Un bon Merlot" -> "Un bon Merlot").
     */
    @Test
    fun `cleanWineLine removePattern application`() {
        val input = "Notre vin conseillé: Un bon Merlot"
        val expected = "Un bon Merlot"
        val actual = WineParser.cleanWineLine(input, vocabulary).trim()
        assertEquals("Nettoyage du motif d'en-tête vin échoué pour '$input'", expected, actual)
    }

    /**
     * Vérifie l'effacement de tous les motifs d'en-tête de section vin via OcrRegexes.Wines.REMOVE_HEADER_PATTERN.
     */
    @Test
    fun `cleanWineLine should remove wine section header patterns`() {
        val testCases = mapOf(
            "Notre vin conseillé : Un Merlot" to "Un Merlot",
            "Vin suggéré : Châteauneuf-du-Pape" to "Châteauneuf-du-Pape",
            "Conseil vin : Pinot Noir" to "Pinot Noir",
            "Vin recommandé : Riesling" to "Riesling",
            "vin conseillé: Bordeaux" to "Bordeaux"
        )

        testCases.forEach { (input, expected) ->
            val actual = WineParser.cleanWineLine(input, vocabulary)
            assertEquals("Nettoyage d'en-tête vin échoué pour '$input'", expected, actual)
        }
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

    @Test
    fun `cleanWineLine should preserve hyphen in Haut-Médoc`() {
        val input = "Haut-Médoc 1995"
        val actual = WineParser.cleanWineLine(input, vocabulary)
        System.err.println("CLEANED WINE = '$actual'")
        assertEquals("Haut-Médoc 1995", actual)
    }

    @Test
    fun `loadResources should populate WineResources from Android Resources`() {
        val mockRes = org.mockito.kotlin.mock<android.content.res.Resources>()
        org.mockito.kotlin.whenever(mockRes.getStringArray(com.example.chiefinventory.R.array.wine_appellations)).thenReturn(arrayOf("Bordeaux", "Chianti"))
        org.mockito.kotlin.whenever(mockRes.getStringArray(com.example.chiefinventory.R.array.wine_producers)).thenReturn(arrayOf("Château Margaux"))
        org.mockito.kotlin.whenever(mockRes.getStringArray(com.example.chiefinventory.R.array.wine_keywords)).thenReturn(arrayOf("Merlot"))
        org.mockito.kotlin.whenever(mockRes.getStringArray(com.example.chiefinventory.R.array.wine_title_keywords)).thenReturn(arrayOf("accord vin"))
        org.mockito.kotlin.whenever(mockRes.getString(com.example.chiefinventory.R.string.wine_remove_pattern)).thenReturn("vin conseillé")

        val wineRes = WineParser.loadResources(mockRes)

        assertEquals(2, wineRes.appellations.size)
        assertEquals("Bordeaux", wineRes.appellations.first())
        assertEquals("Château Margaux", wineRes.producers.first())
        assertEquals("Merlot", wineRes.keywords.first())
        assertEquals("accord vin", wineRes.titleKeywords.first())
        assertEquals("vin conseillé", wineRes.removePattern)
    }

    @Test
    fun `loadResources should return empty removePattern when string resource throws exception`() {
        val mockRes = org.mockito.kotlin.mock<android.content.res.Resources>()
        org.mockito.kotlin.whenever(mockRes.getStringArray(com.example.chiefinventory.R.array.wine_appellations)).thenReturn(emptyArray())
        org.mockito.kotlin.whenever(mockRes.getStringArray(com.example.chiefinventory.R.array.wine_producers)).thenReturn(emptyArray())
        org.mockito.kotlin.whenever(mockRes.getStringArray(com.example.chiefinventory.R.array.wine_keywords)).thenReturn(emptyArray())
        org.mockito.kotlin.whenever(mockRes.getStringArray(com.example.chiefinventory.R.array.wine_title_keywords)).thenReturn(emptyArray())
        org.mockito.kotlin.whenever(mockRes.getString(com.example.chiefinventory.R.string.wine_remove_pattern)).thenThrow(RuntimeException("Resource not found"))

        val wineRes = WineParser.loadResources(mockRes)

        assertEquals("", wineRes.removePattern)
    }

    @Test
    fun `isWineLine should return false for generic wine words`() {
        val genericWords = listOf("vin", "vin blanc", "vin rouge", "du vin blanc", "du vin rouge")
        genericWords.forEach { word ->
            assertFalse("Mots génériques de vin doivent être rejetés : '$word'", WineParser.isWineLine(word, vocabulary))
        }
    }

    @Test
    fun `isWineLine should return true for short single year line`() {
        assertTrue("Ligne courte avec année seule doit être détectée comme vin", WineParser.isWineLine("2015", vocabulary))
    }

    @Test
    fun `hasWineVolume should return true when volume or bottle format is detected`() {
        assertTrue(WineParser.hasWineVolume("75 cl"))
        assertTrue(WineParser.hasWineVolume("1.5 l"))
        assertFalse(WineParser.hasWineVolume("Sans volume"))
    }

    @Test
    fun `cleanWineLine should catch exception gracefully when removePattern is invalid regex`() {
        val invalidRegexResources = WineParser.WineResources(
            appellations = emptyList(),
            producers = emptyList(),
            keywords = emptyList(),
            titleKeywords = emptyList(),
            removePattern = "["
        )

        val cleaned = WineParser.cleanWineLine("Un Bordeau 1996", invalidRegexResources)

        assertEquals("Un Bordeaux 1996", cleaned)
    }
}
