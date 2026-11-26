package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la détection des fragments de phrases d'instructions narratives
 * composées par l'assistant [Ocr3NarrativeCompoundHelper].
 */
class Ocr3NarrativeCompoundHelperTest {

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
    fun testIsNarrativeCompoundInstruction_NarrativeSentenceFragments_ReturnsTrue() {
        val line1 = "de jus d'orange et une pincée de sel."
        val line2 = "d'un jus de citron et d'une pincée de poivre."
        val line3 = "de 1 cuillère de miel et d'une pincée de sel."

        assertWithMessage("Le fragment 'de jus d'orange et une pincée de sel.' doit être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line1))
            .isTrue()

        assertWithMessage("Le fragment 'd'un jus de citron et d'une pincée de poivre.' doit être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line2))
            .isTrue()

        assertWithMessage("Le fragment 'de 1 cuillère de miel et d'une pincée de sel.' doit être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line3))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeCompoundInstruction_ValidStandaloneIngredients_ReturnsFalse() {
        val line1 = "1 pincée de sel"
        val line2 = "200 g de farine"
        val line3 = "4 c. à soupe de jus d'orange"

        assertWithMessage("L'ingrédient '1 pincée de sel' ne doit PAS être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line1))
            .isFalse()

        assertWithMessage("L'ingrédient '200 g de farine' ne doit PAS être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line2))
            .isFalse()

        assertWithMessage("L'ingrédient '4 c. à soupe de jus d'orange' ne doit PAS être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line3))
            .isFalse()
    }

}
