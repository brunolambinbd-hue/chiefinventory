package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la qualification des tournures "poivre [de | du | d'] <item>"
 * par l'assistant [Ocr4MergerPepperHelper].
 */
class Ocr4MergerPepperHelperTest {

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
    fun testIsPepperWithPrepositionOrigin_ValidPrepositionOrigins_ReturnsTrue() {
        val lineCayenne = "de Cayenne"
        val lineMoulin = "du moulin"
        val lineSichuan = "de Sichuan"

        assertWithMessage("'poivre de Cayenne' doit être qualifié comme poivre d'origine valide")
            .that(Ocr4MergerSeasoningHelper.isPepperWithPrepositionOrigin(lineCayenne, vocabulary.pepperDeOriginItems))
            .isTrue()

        assertWithMessage("'poivre du moulin' doit être qualifié comme poivre d'origine valide")
            .that(Ocr4MergerSeasoningHelper.isPepperWithPrepositionOrigin(lineMoulin, vocabulary.pepperDeOriginItems))
            .isTrue()

        assertWithMessage("'poivre de Sichuan' doit être qualifié comme poivre d'origine valide")
            .that(Ocr4MergerSeasoningHelper.isPepperWithPrepositionOrigin(lineSichuan, vocabulary.pepperDeOriginItems))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsPepperWithPrepositionOrigin_InvalidOriginsOrNoPreposition_ReturnsFalse() {
        val lineInvalidOrigin = "poivre de jus d'orange"
        val lineAdjective = "poivre moulu"

        assertWithMessage("'poivre de jus d'orange' ne doit PAS être qualifié comme poivre d'origine valide")
            .that(Ocr4MergerSeasoningHelper.isPepperWithPrepositionOrigin(lineInvalidOrigin, vocabulary.pepperDeOriginItems))
            .isFalse()

        assertWithMessage("'poivre moulu' (sans préposition de/du/d') doit retourner false sur la vérification de préposition")
            .that(Ocr4MergerSeasoningHelper.isPepperWithPrepositionOrigin(lineAdjective, vocabulary.pepperDeOriginItems))
            .isFalse()
    }
}
