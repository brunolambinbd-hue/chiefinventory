package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.Ocr3InvertedCategorizer
import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Test de règle (Type A) dédié au maintien de l'extraction des vins monolignes dans [Ocr3InvertedCategorizer].
 */
class RecipeOcrInvertedSingleLineWineRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `parse inverted recipe with single line wine header should capture wine correctly`() {
        val lines = listOf(
            "1. Découpez les légumes et faites-les rissoler.",
            "POUR 4 PERSONNES",
            "200 g de farine",
            "3 oeufs",
            "Vin conseillé : Bordeaux 1995, 75 cl"
        )

        val result = Ocr3InvertedCategorizer.categorizeInvertedRecipe(lines, vocabulary)

        assertWithMessage("Les ingrédients de la recette inversée doivent contenir '200 g de farine'")
            .that(result.rawIngredientsList)
            .contains("200 g de farine")

        assertWithMessage("Le vin monoligne 'Bordeaux 1995, 75 cl' sous en-tête doit être capturé")
            .that(result.detectedWineList)
            .contains("Bordeaux 1995, 75 cl")
    }
}
