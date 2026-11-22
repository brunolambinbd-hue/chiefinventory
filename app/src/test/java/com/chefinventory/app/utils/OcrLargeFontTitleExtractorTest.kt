package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant l'extraction de titres ayant une grande police de caractères 2D (166px) même placés tardivement par ML Kit.
 */
class OcrLargeFontTitleExtractorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_WithLargeFontHeight2DBoundingBox_SelectsTitleEvenIfPositionedLate() {
        val lines = listOf(
            "INGRÉDIENTS",
            "300 g de crevettes roses",
            "8 c. à soupe de yaourt",
            "torevettes grises aux épinards"
        )

        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 130, left = 112, bottom = 175, right = 405),
            OcrTextLine("300 g de crevettes roses", top = 268, left = 99, bottom = 304, right = 423),
            OcrTextLine("8 c. à soupe de yaourt", top = 307, left = 113, bottom = 345, right = 418),
            OcrTextLine("torevettes grises aux épinards", top = 832, left = 570, bottom = 998, right = 2357) // 166px height!
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary, lines2D)

        val beautifiedTitle = Ocr5BeautifierTitle.beautifyTitle(result.title)

        assertWithMessage("La ligne 'torevettes grises aux épinards' avec une hauteur de police de 166px doit être sélectionnée comme Titre et corrigée")
            .that(beautifiedTitle)
            .isEqualTo("Crevettes grises aux épinards")
    }
}
