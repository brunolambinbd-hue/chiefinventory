package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de l'Étape 3 (Catégorisation) vérifiant que l'en-tête "Votre marché" (et "Votre marché (4 personnes)")
 * ouvre la section ingrédients et est 100% purgé de la liste des ingrédients bruts.
 */
class OcrVotreMarchePhase3Test {

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
    fun testPhase3_Categorize_OpensSectionAndPurgesVotreMarcheHeader() {
        val cleanedLines = listOf(
            "Votre marché :",
            "4 oignons",
            "50 g de beurre",
            "INSTRUCTIONS",
            "• Faites revenir les oignons."
        )

        val result = Ocr3Categorizer.categorize(cleanedLines, vocabulary)

        assertWithMessage("La section ingrédients doit contenir les vrais aliments '4 oignons'")
            .that(result.rawIngredientsList)
            .contains("4 oignons")

        assertWithMessage("L'en-tête de section 'Votre marché :' ne doit PAS être ajouté aux ingrédients bruts")
            .that(result.rawIngredientsList)
            .doesNotContain("Votre marché :")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPhase3_Categorize_PurgesVotreMarcheHeaderWithServingsSuffix() {
        val cleanedLines = listOf(
            "Votre marché (4 personnes)",
            "4 oignons",
            "50 g de beurre",
            "INSTRUCTIONS",
            "• Faites revenir les oignons."
        )

        val result = Ocr3Categorizer.categorize(cleanedLines, vocabulary)

        assertWithMessage("La section ingrédients doit contenir les vrais aliments '4 oignons'")
            .that(result.rawIngredientsList)
            .contains("4 oignons")

        assertWithMessage("L'en-tête 'Votre marché (4 personnes)' ne doit PAS être ajouté aux ingrédients bruts")
            .that(result.rawIngredientsList)
            .doesNotContain("Votre marché (4 personnes)")
    }
}
