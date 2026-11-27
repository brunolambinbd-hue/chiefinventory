package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr4MergerSeasoningHelper].
 */
class Ocr4MergerSeasoningHelperTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertWithMessage("Le composant Ocr4MergerSeasoningHelper doit générer une représentation textuelle valide")
            .that(Ocr4MergerSeasoningHelper.toString())
            .contains("Ocr4MergerSeasoningHelper")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSeasoningWithPrepositionOrigin_AllBranches() {
        val saltOrigins = vocabulary.saltVarietyOrigins
        val pepperOrigins = vocabulary.pepperDeOriginItems

        // 1. Valid seasoning with preposition origin
        assertWithMessage("Le sel 'sel de Guérande' doit être reconnu")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin("sel de Guérande", saltOrigins))
            .isTrue()

        assertWithMessage("Le poivre 'poivre de Cayenne' doit être reconnu")
            .that(Ocr4MergerSeasoningHelper.isPepperWithPrepositionOrigin("poivre de Cayenne", pepperOrigins))
            .isTrue()

        // 2. Line not starting with preposition or non-matching origin
        assertWithMessage("Le mot 'farine de blé' ne doit pas être qualifié de sel d'origine")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin("farine de blé", saltOrigins))
            .isFalse()

        // 3. Blank line -> false
        assertWithMessage("Une ligne vide doit retourner faux")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin("", saltOrigins))
            .isFalse()

        assertWithMessage("Une ligne d'espaces doit retourner faux")
            .that(Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin("   ", saltOrigins))
            .isFalse()

        // 4. Empty origins list -> false
        assertWithMessage("Une liste d'origines vide doit retourner faux")
            .that(Ocr4MergerSeasoningHelper.isSeasoningWithPrepositionOrigin("sel de Guérande", "sel", emptyList()))
            .isFalse()
    }
}
