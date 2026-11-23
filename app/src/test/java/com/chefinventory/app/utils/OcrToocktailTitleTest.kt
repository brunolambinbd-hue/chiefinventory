package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que 'Toocktail de crevettes aux concombres.' est réparé en 'Cocktail de crevettes aux concombres' et sélectionné comme titre.
 */
class OcrToocktailTitleTest {

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
    fun testTitleExtraction_ToocktailDeCrevettesAuxConcombres_IsSelectedAndRepairedAsTitle() {
        val rawLines = listOf(
            "Toocktail de crevettes aux concombres.",
            "Ingrédients pour 4 personnes",
            "200 g de crevettes",
            "1 concombre",
            "Préparation : 15 min"
        )

        val result = RecipeOcrParser.parse(rawLines, vocabulary, offTaxonomyList = listOf("crevettes", "concombre"))

        assertWithMessage("Le titre 'Toocktail de crevettes aux concombres.' doit être sélectionné et réparé en 'Cocktail de crevettes aux concombres'")
            .that(result.title)
            .isEqualTo("Cocktail de crevettes aux concombres")
    }
}
