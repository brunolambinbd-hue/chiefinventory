package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire d'intégration anti-régression dédié à la recette des Salades de Langoustines au Curry.
 */
class RecipeOcrParserLangoustineTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun testLangoustineRecipeIngredientsMerge() {
        val rawIngredients = listOf(
            "24 langoustines",
            "cuites",
            "1 mangue",
            "à",
            "point",
            "4 c. à",
            "soupe",
            "de",
            "petits",
            "croûtons",
            "3 c. à",
            "soupe",
            "de",
            "mayonnaise",
            "1 c. à",
            "café",
            "de",
            "curry",
            "1/2 citron",
            "1 yaourt nature (125 g)",
            "4 belles",
            "feuilles",
            "de",
            "salade",
            "cresson",
            "sel",
            "et",
            "poivre"
        )

        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )

        assertThat(merged).containsExactly(
            "24 langoustines cuites",
            "1 mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "1 c. à café de curry",
            "1/2 citron",
            "1 yaourt nature (125 g)",
            "4 belles feuilles de salade",
            "cresson",
            "sel et poivre"
        )
    }

    @Test
    fun testLangoustineRecipeFullPipelineParse() {
        val fullText = """
            INGRÉDIENTS
            24 langoustines
            cuites
            1 mangue
            à
            point
            4 c. à
            soupe
            de
            petits
            croûtons
            3 c. à
            soupe
            de
            mayonnaise
            1 c. à
            café
            de
            curry
            1/2 citron
            1 yaourt nature (125 g)
            4 belles
            feuilles
            de
            salade
            cresson
            sel
            et
            poivre
            
            PRÉPARATION
            Disposez six langoustines sur chaque assiette, ajoutez tout autour des.
            lamelles de mangue. Décorez de salade et de cresson, ajoutez les
            croûtons.
        """.trimIndent()

        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(fullText, vocabulary)

        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()
        val instructionLines = result.instructions?.split("\n") ?: emptyList()

        assertThat(ingredientLines).containsExactly(
            "24 langoustines cuites",
            "1 mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "1 c. à café de curry",
            "1/2 citron",
            "1 yaourt nature (125 g)",
            "4 belles feuilles de salade",
            "cresson",
            "sel et poivre"
        )

        assertThat(instructionLines).containsExactly(
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des lamelles de mangue.",
            "Décorez de salade et de cresson, ajoutez les croûtons."
        )
    }
}
