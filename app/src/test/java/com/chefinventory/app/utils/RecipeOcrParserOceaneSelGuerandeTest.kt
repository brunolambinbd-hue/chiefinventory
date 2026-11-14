package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'expérimentation d'isolation basé sur la recette de référence "Salade Océane",
 * remplaçant l'assaisonnement standard 'sel et poivre' par 'sel de Guérande' et 'poivre du moulin'.
 */
class RecipeOcrParserOceaneSelGuerandeTest {

    private val rawOcrScanText = listOf(
        "INGRÉDIENTS",
        "POUR",
        "4 PERSONNES",
        "250 g de crevettes",
        "grises décortiquées",
        "4 petites tomates",
        "(ou 2 grosses)",
        "l beau chicon",
        "I2 salade frisée",
        "l orange",
        "250 g de yaourt nature",
        "2 c. à soupe de",
        "mayonnaise",
        "2 c. à soupe de ketchup",
        "lc.à soupe de",
        "ciboulette hachée",
        "Tabasco",
        "sel",
        "de Guérande",
        "poivre du moulin",
        "Salade océane",
        "• Prélevez finement le zeste de",
        "l'orange, taillez-le en fins bâton-",
        "nets que vous plongez 1 minute",
        "dans un fond d'eau bouillante.",
        "Rafraîchissez à l'eau froide et lais-",
        "sez bien égoutter.",
        "• Mélangez le yaourt, le ketchup,",
        "la mayonnaise et la ciboulette.",
        "Ajoutez du Tabasco selon vos",
        "goûts, salez et poivrez. Réservez.",
        "• Lavez et essorez la salade, ajou-",
        "tez-y le chicon nettoyé et émincé.",
        "Disposez sur de grandes assiettes",
        "et saupoudrez de zeste d'orange.",
        "•Coupez les tomates en huit,",
        "répartissez-les sur les assiettes et",
        "garnissez le centre de crevettes",
        "grises.",
        "• Servez frais (ne laissez pas trop",
        "longtemps en attente); présentez la",
        "sauce à part.",
        "VIN CONSEILLÉ",
        "Touraine,",
        "Sauvignon blanc, 1995, 75 cl"
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
    fun testOceaneRecipe_WithSelDeGuerandeAndPoivreDuMoulin_ParsesIngredientsCleanly() {
        OcrVocabularyLoader.clearCache()
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        System.out.println("TEST DEBUG: saltVarietyOrigins = ${vocabulary.saltVarietyOrigins}")
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()

        assertWithMessage("La recette doit extraire l'ingrédient 'Sel de Guérande'")
            .that(ingredientLines)
            .contains("Sel de Guérande")

        assertWithMessage("La recette doit extraire l'ingrédient 'Poivre du moulin'")
            .that(ingredientLines)
            .contains("Poivre du moulin")
    }
}
