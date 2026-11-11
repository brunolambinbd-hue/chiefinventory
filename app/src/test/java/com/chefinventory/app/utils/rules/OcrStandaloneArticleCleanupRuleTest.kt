package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

class OcrStandaloneArticleCleanupRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe should discard standalone article noise lines like Du or De in ingredients`() {
        val ocrText = """
            Céleri au Stilton
            INGRÉDIENTS
            Du
            6 branches de céleri blanc
            140 g de fromage stilton
            50 g de fromage blanc
            1 c. à café de porto blanc
            sel et poivre
            • Préparation de la recette.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        assertWithMessage("L'ingrédient '6 branches de céleri blanc' doit être conservé")
            .that(ing)
            .contains("6 branches de céleri blanc")

        assertWithMessage("L'article orphelin de début de section 'Du' doit être filtré")
            .that(ing.lowercase().split(Regex("[\\s\n]+")))
            .doesNotContain("du")
    }
}
