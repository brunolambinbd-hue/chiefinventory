package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la fusion d'ingrédients se terminant par un mot numérique ("une petite pointe de cinq")
 * avec la ligne d'aliment suivante ("épices chinoises").
 */
class Ocr4MergerCinqEpicesTest {

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
    fun testIsHanging_LineEndingWithNumberWord_ReturnsTrue() {
        val isHangingCinq = Ocr4Merger.isHanging("une petite pointe de cinq")
        assertWithMessage("La ligne 'une petite pointe de cinq' se terminant par le mot numérique 'cinq' doit être reconnue comme suspendue")
            .that(isHangingCinq)
            .isTrue()

        val isHangingTrois = Ocr4Merger.isHanging("2 ou 3")
        assertWithMessage("La ligne '2 ou 3' se terminant par le chiffre '3' doit être reconnue comme suspendue")
            .that(isHangingTrois)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_MergesLineEndingWithNumberWordWithNextLine() {
        val inputLines = listOf(
            "1 petite pointe de cinq",
            "épices chinoises"
        )

        val merged = Ocr4Merger.mergeIngredients(
            inputLines,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )

        assertWithMessage("La ligne '1 petite pointe de cinq' doit fusionner avec 'épices chinoises'")
            .that(merged)
            .containsExactly("1 petite pointe de cinq épices chinoises")
    }
}
