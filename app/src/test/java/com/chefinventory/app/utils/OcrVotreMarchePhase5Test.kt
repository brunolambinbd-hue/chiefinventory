package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de l'Étape 5 (Beautifier) vérifiant le filtrage ultime de sécurité
 * sur toute variante résiduelle d'en-tête "Votre marché" ou "Votre marché (4 personnes)".
 */
class OcrVotreMarchePhase5Test {

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
    fun testPhase5_BeautifyIngredients_FiltersAnyResidualVotreMarcheHeader() {
        val mergedIngredientsWithResidualHeader = listOf(
            "Votre marché :",
            "Votre marché (4 personnes)",
            "4 oignons",
            "50 g de beurre"
        )

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(
            mergedIngredientsWithResidualHeader,
            vocabulary.baseIngredientsExpectingVariety,
            vocabulary.ingredientVarietyQualifiers
        )

        assertWithMessage("L'Étape 5 doit filtrer 'Votre marché :' s'il est présent")
            .that(beautified)
            .doesNotContain("Votre marché :")

        assertWithMessage("L'Étape 5 doit filtrer 'Votre marché (4 personnes)' s'il est présent")
            .that(beautified)
            .doesNotContain("Votre marché (4 personnes)")

        assertWithMessage("Les ingrédients valides doivent être conservés")
            .that(beautified)
            .containsExactly("4 oignons", "50 g de beurre")
            .inOrder()
    }
}
