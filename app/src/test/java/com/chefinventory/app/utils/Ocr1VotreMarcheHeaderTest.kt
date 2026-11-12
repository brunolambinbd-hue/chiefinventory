package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant que les synonymes d'en-tête d'ingrédients "Votre marché", "Dans votre marché"
 * (y compris les variations d'accents "Votre marche", "votre marchė") ouvrent la section ingrédients à 100%.
 */
class Ocr1VotreMarcheHeaderTest {

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
    fun testParse_VotreMarcheHeader_OpensIngredientsSectionAndExtractsIngredients() {
        val rawOcrText = """
            Soupe à l'oignon
            Votre marché :
            4 oignons
            50 g de beurre
            sel et poivre
            INSTRUCTIONS
            • Faites revenir les oignons dans le beurre.
        """.trimIndent()

        val parsed = RecipeOcrParser.parse(rawOcrText, vocabulary)

        val ing = parsed.ingredients.orEmpty()

        assertWithMessage("La section ingrédients doit être ouverte par 'Votre marché :' et contenir '4 oignons'")
            .that(ing)
            .contains("4 oignons")

        assertWithMessage("La section ingrédients doit contenir '50 g de beurre'")
            .that(ing)
            .contains("50 g de beurre")

        assertWithMessage("L'en-tête 'Votre marché' ne doit pas fuiter dans le titre")
            .that(parsed.title)
            .isEqualTo("Soupe à l'oignon")

        assertWithMessage("L'en-tête 'Votre marché' ne doit pas figurer dans la liste des ingrédients")
            .that(ing.lowercase())
            .doesNotContain("votre marché")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_VotreMarcheWithDiacriticDot_OpensIngredientsSection() {
        val rawOcrText = """
            Tarte tatin
            votre marchė :
            6 pommes
            100 g de sucre
            INSTRUCTIONS
            • Faites caraméliser le sucre.
        """.trimIndent()

        val parsed = RecipeOcrParser.parse(rawOcrText, vocabulary)

        val ing = parsed.ingredients.orEmpty()

        assertWithMessage("L'en-tête avec point diacritique 'votre marchė :' doit ouvrir la section ingrédients")
            .that(ing)
            .contains("6 pommes")
    }
}
