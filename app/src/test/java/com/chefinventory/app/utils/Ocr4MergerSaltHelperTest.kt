package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation distinct vérifiant la qualification des tournures "sel [de | du | d'] <item>"
 * par l'assistant [Ocr4MergerSeasoningHelper].
 */
class Ocr4MergerSaltHelperTest {

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
    fun testIsSaltWithPrepositionOrigin_ValidPrepositionOrigins_ReturnsTrue() {
        val lineGuerande = "sel de Guérande"
        val lineMer = "sel de mer"
        val lineCamargue = "sel de Camargue"

        assertWithMessage("'sel de Guérande' doit être qualifié comme sel d'origine valide")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin(lineGuerande, vocabulary.saltVarietyOrigins))
            .isTrue()

        assertWithMessage("'sel de mer' doit être qualifié comme sel d'origine valide")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin(lineMer, vocabulary.saltVarietyOrigins))
            .isTrue()

        assertWithMessage("'sel de Camargue' doit être qualifié comme sel d'origine valide")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin(lineCamargue, vocabulary.saltVarietyOrigins))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSaltWithPrepositionOrigin_InvalidOriginsOrNoPreposition_ReturnsFalse() {
        val lineInvalidOrigin = "sel de jus d'orange"
        val lineAdjective = "sel fin"

        assertWithMessage("'sel de jus d'orange' ne doit PAS être qualifié comme sel d'origine valide")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin(lineInvalidOrigin, vocabulary.saltVarietyOrigins))
            .isFalse()

        assertWithMessage("'sel fin' (sans préposition de/du/d') doit retourner false sur la vérification de préposition")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin(lineAdjective, vocabulary.saltVarietyOrigins))
            .isFalse()
    }
}
