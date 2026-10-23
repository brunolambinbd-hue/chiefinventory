package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrCanapesGrecsRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse full canapes grecs recipe should extract ingredients and instructions cleanly`() {
        val ocrText = """
            Danapés grecs
            Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-
            cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur + 1/2 heures.
            Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les
            d'ail, égouttez le fromage et placez-le sur le pain.
            Décorez d'olives coupées en deux et de romarin.
            ingrédients pour 4 personnes
            180 g de feta
            1/2 verre d'huile d'olive
            10 olives vertes dénoyautées
            10 olives noires dénoyautées
            1 petit piment
            1 gousse d'ail
            1 baguette de pain gris
            du thym et du romarin frais
            sel et poivre du moulin
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        System.err.println("=== DEBUG INGREDIENTS ===\n$ing")

        // 1. Validation du nombre de personnes
        assertThat(result.servings).isEqualTo("4")

        // 2. Validation des ingrédients
        assertThat(ing).ignoringCase().contains("180 g de feta")
        assertThat(ing).ignoringCase().contains("du thym et du romarin frais")
        assertThat(ing).ignoringCase().contains("sel et poivre du moulin")

        // 3. Validation des instructions (les consignes du haut doivent être dans les instructions)
        assertThat(inst).ignoringCase().contains("Mélangez l'huile d'olive")
        assertThat(inst).ignoringCase().doesNotContain("du thym et du romarin frais")
    }

    @Test
    fun `parse canapes grecs without title at top`() {
        val ocrTextWithoutTitleAtTop = """
            Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-
            cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur + 1/2 heures.
            Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les
            d'ail, égouttez le fromage et placez-le sur le pain.
            Décorez d'olives coupées en deux et de romarin.
            ingrédients pour 4 personnes
            180 g de feta
            1/2 verre d'huile d'olive
            10 olives vertes dénoyautées
            10 olives noires dénoyautées
            1 petit piment
            1 gousse d'ail
            1 baguette de pain gris
            du thym et du romarin frais
            sel et poivre du moulin
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrTextWithoutTitleAtTop, vocabulary)
        System.err.println("=== INGREDIENTS WITHOUT TITLE AT TOP ===\n" + result.ingredients)
        assertThat(result.ingredients).ignoringCase().contains("du thym et du romarin frais")
    }
}
