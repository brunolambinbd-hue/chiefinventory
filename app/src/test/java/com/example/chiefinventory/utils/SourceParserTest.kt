package com.example.chiefinventory.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et la précision de [SourceParser].
 */
class SourceParserTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setup() {
        // Charge le VRAI vocabulaire de production depuis les fichiers XML (app/src/main/res/values/*.xml)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie l'identification des numéros de téléphone aux formats nationaux et internationaux.
     */
    @Test
    fun `isSourceLine should return true for valid phone numbers`() {
        assertTrue(SourceParser.isSourceLine("02/542.42.42", vocabulary))
        assertTrue(SourceParser.isSourceLine("+32 2 542 42 42", vocabulary))
        assertTrue(SourceParser.isSourceLine("Tel: 01 45 67 89 10", vocabulary))
        assertTrue(SourceParser.isSourceLine("06.12.34.56.78", vocabulary))
    }

    /**
     * Vérifie l'identification des lignes contenant des mots-clés d'établissements ou d'adresses issus de `source_keywords.xml`.
     */
    @Test
    fun `isSourceLine should return true for lines with source keywords`() {
        assertTrue(SourceParser.isSourceLine("Restaurant La Bonne Fourchette", vocabulary))
        assertTrue(SourceParser.isSourceLine("12 rue des Fleurs", vocabulary))
        assertTrue(SourceParser.isSourceLine("Hôtel de Ville", vocabulary))
    }

    /**
     * Vérifie l'identification des lignes courtes contenant des codes postaux.
     */
    @Test
    fun `isSourceLine should return true for short lines with zip codes`() {
        assertTrue(SourceParser.isSourceLine("75001 Paris", vocabulary))
        assertTrue(SourceParser.isSourceLine("CP: 1000", vocabulary))
    }

    /**
     * Vérifie le rejet des lignes d'instructions ou d'ingrédients ne contenant aucune information de source.
     */
    @Test
    fun `isSourceLine should return false for lines without source info`() {
        assertFalse(SourceParser.isSourceLine("Couper les oignons", vocabulary)) 
        assertFalse(SourceParser.isSourceLine("Sel et poivre", vocabulary))
        assertFalse(SourceParser.isSourceLine("Voici une ligne longue de preparation avec un chiffre qui ne devrait pas etre consideree source.", vocabulary))
    }

    /**
     * Vérifie que des titres de recettes en majuscules avec des ingrédients ("RICOTTA ET ÉPINARDS", "CANAPÉS GRECS") ne sont jamais pris pour des sources.
     */
    @Test
    fun `isLikelyProperNameOrSource should return false for recipe titles containing food words`() {
        assertFalse(OcrHelperUtils.isLikelyProperNameOrSource("RICOTTA ET ÉPINARDS", vocabulary))
        assertFalse(OcrHelperUtils.isLikelyProperNameOrSource("CANAPÉS GRECS", vocabulary))

        assertTrue(OcrHelperUtils.isLikelyProperNameOrSource("MICHEL LAROCHE", vocabulary))
        assertTrue(OcrHelperUtils.isLikelyProperNameOrSource("HÔTEL CONRAD", vocabulary))
    }

    /**
     * Vérifie le nettoyage et la suppression des préfixes téléphoniques ("Tel:", "Tél.", "Fax", "GSM").
     */
    @Test
    fun `cleanSourceLine should remove phone prefixes`() {
        assertEquals("01 23 45 67 89", SourceParser.cleanSourceLine("Tel: 01 23 45 67 89", vocabulary))
        assertEquals("02 54 88 99", SourceParser.cleanSourceLine("Tél. 02 54 88 99", vocabulary))
        assertEquals("04 55", SourceParser.cleanSourceLine("Fax 04 55", vocabulary))
        assertEquals("0612345678", SourceParser.cleanSourceLine("GSM: 0612345678", vocabulary))
    }

    /**
     * Vérifie que le nettoyage des préfixes n'altère pas les mots se terminant par les mêmes lettres (ex: "Hôtel").
     */
    @Test
    fun `cleanSourceLine should not break words ending with prefix`() {
        assertEquals("Hôtel 012345", SourceParser.cleanSourceLine("Hôtel 012345", vocabulary))
        assertEquals("Chantel 012345", SourceParser.cleanSourceLine("Chantel 012345", vocabulary))
    }

    /**
     * Vérifie l'extraction précoce des marques/enseignes connues dans les bannières publicitaires.
     */
    @Test
    fun `extractSourceFromAdLine should extract known brand Maxi GB from ad line`() {
        assertEquals("Maxi GB", SourceParser.extractSourceFromAdLine("Assiette de présentation disponible dans les Maxi GB", vocabulary))
        assertEquals("Delhaize", SourceParser.extractSourceFromAdLine("Recette proposée par les magasins Delhaize", vocabulary))
        assertEquals("Cuisine Actuelle", SourceParser.extractSourceFromAdLine("Extrait du magazine Cuisine Actuelle n°45", vocabulary))
    }

    /**
     * Vérifie le retour null (0 faux positif) pour les bannières sans marque de la liste blanche.
     */
    @Test
    fun `extractSourceFromAdLine should return null for ad lines without known brand`() {
        assertEquals(null, SourceParser.extractSourceFromAdLine("Assiette de présentation disponible dans les supermarchés", vocabulary))
        assertEquals(null, SourceParser.extractSourceFromAdLine("Splat disponible immédiatement", vocabulary))
    }
}
