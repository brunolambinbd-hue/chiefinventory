package com.example.chiefinventory.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Ocr3Categorizerv1Test {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setup() {
        vocabulary = OcrVocabulary(
            stepActionKeywords = listOf("cuire", "mélanger", "couper"),
            commonIngredientsNoQty = listOf("sel", "poivre", "farine", "sucre"),
            excludedOcrKeywords = listOf("publicité", "page"),
            wineAppellations = listOf("Bordeaux", "Bourgogne"),
            wineKeywords = listOf("vin", "rouge", "blanc"),
            wineRemovePattern = "Boisson: ",
            sourceKeywords = listOf("hôtel", "restaurant")
        )
    }

    @Test
    fun `categorize should properly sort ingredients and instructions without headers`() {
        val inputLines = listOf(
            "200 g de farine",
            "3 oeufs",
            "Mélanger la farine et les oeufs dans un bol.",
            "Couper les légumes en dés."
        )

        val result = Ocr3Categorizerv1.categorize(inputLines, vocabulary)

        assertEquals(2, result.rawIngredientsList.size)
        assertEquals("200 g de farine", result.rawIngredientsList[0])
        assertEquals("3 oeufs", result.rawIngredientsList[1])

        assertEquals(2, result.rawInstructionsList.size)
        assertTrue(result.rawInstructionsList[0].startsWith("Mélanger"))
    }

    @Test
    fun `categorize should detect and switch sections based on headers`() {
        val inputLines = listOf(
            "Ingrédients :",
            "Sel",
            "Poivre",
            "Préparation",
            "Cuire 20 minutes"
        )

        val result = Ocr3Categorizerv1.categorize(inputLines, vocabulary)

        assertEquals(2, result.rawIngredientsList.size)
        assertEquals("Sel", result.rawIngredientsList[0])
        
        assertEquals(1, result.rawInstructionsList.size)
        assertEquals("Cuire 20 minutes", result.rawInstructionsList[0])
    }

    @Test
    fun `categorize should extract servings and times`() {
        val inputLines = listOf(
            "Pour 4 personnes",
            "Préparation : 20 min",
            "Cuisson: 1h30",
            "Repos : 2 h",
            "500g de viande"
        )

        val result = Ocr3Categorizerv1.categorize(inputLines, vocabulary)

        assertEquals("4", result.detectedServings)
        assertEquals("20", result.detectedPrepTime)
        assertEquals("90", result.detectedCookTime)
        assertEquals("120", result.detectedRestingTime)
        
        assertEquals(1, result.rawIngredientsList.size)
    }

    @Test
    fun `categorize should correctly identify wine and source`() {
        val inputLines = listOf(
            "Un bon vin rouge de Bordeaux",
            "Restaurant Chez Michel",
            "200g de sucre"
        )

        val result = Ocr3Categorizerv1.categorize(inputLines, vocabulary)

        assertEquals(1, result.detectedWineList.size)
        assertTrue(result.detectedWineList[0].contains("Bordeaux"))

        assertEquals(1, result.detectedSourceList.size)
        assertTrue(result.detectedSourceList[0].contains("Restaurant"))

        assertEquals(1, result.rawIngredientsList.size)
    }

    @Test
    fun `categorize should ignore excluded keywords`() {
        val inputLines = listOf(
            "200g de publicité sel"
        )

        val result = Ocr3Categorizerv1.categorize(inputLines, vocabulary)
        assertEquals(1, result.rawIngredientsList.size)
        assertEquals("200g de sel", result.rawIngredientsList[0])
    }
}
