package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 4 (Ocr4Merger),
 * les consignes d'instructions fluides et les ingrédients scindés issus des colonnes sont reconstitués sans césure.
 */
class Ocr4MergerMultiColumnTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeInstructions_HangingPrepositionSurAndHyphenation_FusesCleanly() {
        val rawInstructions = listOf(
            "• Disposez les bûchettes sur",
            "une assiette avec la vinaigrette.",
            "• Nappez les lan-",
            "goustines d'un peu de sauce au curry."
        )

        val merged = Ocr4Merger.mergeInstructions(rawInstructions)

        assertWithMessage("La consigne avec préposition suspendue 'sur' doit être fusionnée en 1 seule ligne")
            .that(merged)
            .contains("• Disposez les bûchettes sur une assiette avec la vinaigrette.")

        assertWithMessage("Le mot césuré 'lan-' + 'goustines' doit être reconstitué sans tiret en 'langoustines'")
            .that(merged)
            .contains("• Nappez les langoustines d'un peu de sauce au curry.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMergeIngredients_MultiBulletLine_SplitsIntoMultipleIngredients() {
        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val rawIngredients = listOf(
            "• 1 melon Galia • 8 tranches de jambon"
        )

        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients = rawIngredients,
            preparationModifiers = vocab.ingredientPreparationModifiers,
            commonIngredients = vocab.commonIngredientsNoQty,
            mandatoryComplements = vocab.mandatoryComplementIngredients
        )

        assertWithMessage("La ligne à puces multiples doit être scindée en 2 ingrédients distincts")
            .that(merged)
            .containsExactly("1 melon Galia", "8 tranches de jambon")
            .inOrder()
    }
}
