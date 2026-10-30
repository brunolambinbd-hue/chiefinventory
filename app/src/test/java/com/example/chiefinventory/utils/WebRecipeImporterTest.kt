package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import org.junit.Test

/**
 * Tests unitaires dédiés (1-à-1) pour [WebRecipeImporter].
 */
class WebRecipeImporterTest {

    @Test
    fun `parseJsonLdScript should parse valid Recipe json object`() {
        val json = """
            {
              "@context": "https://schema.org",
              "@type": "Recipe",
              "name": "Tarte aux pommes",
              "recipeCategory": "Dessert",
              "recipeIngredient": ["3 pommes", "100g de sucre"],
              "recipeInstructions": [{"@type": "HowToStep", "text": "Couper les pommes."}, {"@type": "HowToStep", "text": "Cuire au four."}],
              "prepTime": "PT15M",
              "cookTime": "PT30M",
              "recipeYield": "4 personnes",
              "image": "https://example.com/tarte.jpg"
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(json, "https://example.com/recette")
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Tarte aux pommes")
        assertThat(result?.category).isEqualTo("Dessert")
        assertThat(result?.ingredients).containsExactly("3 pommes", "100g de sucre")
        assertThat(result?.instructions).containsExactly("Couper les pommes.", "Cuire au four.")
        assertThat(result?.prepTimeMinutes).isEqualTo("15")
        assertThat(result?.cookTimeMinutes).isEqualTo("30")
        assertThat(result?.servings).isEqualTo("4")
        assertThat(result?.imageUrl).isEqualTo("https://example.com/tarte.jpg")
    }

    @Test
    fun `parseJsonLdScript with invalid json or non-recipe should log and return null`() {
        val invalidJson = "{ invalid json }"
        assertThat(WebRecipeImporter.parseJsonLdScript(invalidJson, "https://example.com")).isNull()

        val nonRecipeJson = """{"@type": "Article", "name": "Article Culinaire"}"""
        assertThat(WebRecipeImporter.parseJsonLdScript(nonRecipeJson, "https://example.com")).isNull()
    }

    @Test
    fun `parseIsoDurationToMinutes should parse ISO 8601 durations correctly`() {
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT15M")).isEqualTo("15")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT1H30M")).isEqualTo("90")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT2H")).isEqualTo("120")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("")).isNull()
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("Invalid")).isNull()
    }

    @Test
    fun `normalizeRecipeCategory should categorize starters mains and desserts`() {
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Entrée froide")).isEqualTo("Entrée")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Plat principal")).isEqualTo("Plat principal")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Gâteau au chocolat")).isEqualTo("Dessert")
        assertThat(WebRecipeImporter.normalizeRecipeCategory(null)).isNull()
    }

    @Test
    fun `parseHtmlDocument should fallback to page title when no JSON-LD is present`() {
        val html = "<html><head><title>Tarte Tatin - Marmiton</title></head><body></body></html>"
        val doc = Jsoup.parse(html)

        val result = WebRecipeImporter.parseHtmlDocument(doc, "https://marmiton.org/tarte")
        assertThat(result.title).isEqualTo("Tarte Tatin")
        assertThat(result.ingredients).isEmpty()
    }

    @Test
    fun `importFromUrl with invalid url should log error and return fallback result`() {
        val result = WebRecipeImporter.importFromUrl("https://invalid-non-existent-domain-12345.com/recette")
        assertThat(result.title).isNull()
        assertThat(result.ingredients).isEmpty()
    }
}
