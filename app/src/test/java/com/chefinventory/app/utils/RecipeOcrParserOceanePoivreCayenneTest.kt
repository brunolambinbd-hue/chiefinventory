package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la symétrie du poivre sur 2 lignes ('poivre' + 'de Cayenne')
 * dans le pipeline complet [RecipeOcrParser].
 */
class RecipeOcrParserOceanePoivreCayenneTest {

    private val rawOcrScanText = listOf(
        "INGRÉDIENTS",
        "POUR",
        "4 PERSONNES",
        "250 g de crevettes",
        "grises décortiquées",
        "4 petites tomates",
        "sel de Guérande",
        "poivre",
        "de Cayenne",
        "Salade océane",
        "• Prélevez finement le zeste de l'orange.",
        "• Mélangez et servez frais."
    ).joinToString("\n")

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
    fun testOceaneRecipe_WithPoivreDeCayenneOnTwoLines_ParsesIngredientsCleanly() {
        OcrVocabularyLoader.clearCache()
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()

        assertWithMessage("La recette doit extraire l'ingrédient 'Sel de Guérande'")
            .that(ingredientLines)
            .contains("Sel de Guérande")

        assertWithMessage("La recette doit fusionner et extraire l'ingrédient 'Poivre de Cayenne'")
            .that(ingredientLines)
            .contains("Poivre de Cayenne")
    }
}
