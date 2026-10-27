package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la couverture de 100% des lignes et branches de [WebRecipeImporter].
 *
 * @see REQ-4.7.1.1
 */
class WebRecipeImporterTypeBTest {

    @Test
    fun `parseHtmlDocument should extract recipe from HTML script tag`() {
        val html = """
            <html>
              <head>
                <title>Tarte aux pommes - Marmiton</title>
                <script type="application/ld+json">
                  {
                    "@context": "https://schema.org",
                    "@type": "Recipe",
                    "name": "Tarte aux pommes",
                    "recipeIngredient": ["6 pommes", "100 g de beurre"]
                  }
                </script>
              </head>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = WebRecipeImporter.parseHtmlDocument(doc, "https://marmiton.org/tarte")

        assertThat(result.title).isEqualTo("Tarte aux pommes")
        assertThat(result.ingredients).hasSize(2)
    }

    @Test
    fun `parseHtmlDocument should handle empty script tags and fallback to HTML title`() {
        val html = """
            <html>
              <head>
                <title>Soupe au pistou - 750g</title>
                <script type="application/ld+json">
                  
                </script>
              </head>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = WebRecipeImporter.parseHtmlDocument(doc, "https://750g.com/soupe")

        assertThat(result.title).isEqualTo("Soupe au pistou")
        assertThat(result.ingredients).isEmpty()
    }

    @Test
    fun `parseHtmlDocument should fallback to Recette Web when title is blank`() {
        val html = "<html><head><title></title></head></html>"
        val doc = Jsoup.parse(html)
        val result = WebRecipeImporter.parseHtmlDocument(doc, "https://example.com")

        assertThat(result.title).isEqualTo("Recette Web")
    }

    @Test
    fun `parseJsonLdScript should handle graph array with null or non-recipe objects`() {
        val jsonLd = """
            {
              "@context": "https://schema.org",
              "@graph": [
                null,
                { "@type": "WebPage", "name": "Page Web" },
                {
                  "@type": "Recipe",
                  "name": "Goulash hongrois",
                  "recipeYield": ["6 personnes"],
                  "image": ["https://images.marmiton.org/goulash.jpg"],
                  "recipeIngredient": ["600 g de bœuf"]
                }
              ]
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(jsonLd, "https://marmiton.org/goulash")

        assertThat(result).isNotNull()
        assertThat(result!!.title).isEqualTo("Goulash hongrois")
        assertThat(result.servings).isEqualTo("6")
        assertThat(result.imageUrl).isEqualTo("https://images.marmiton.org/goulash.jpg")
    }

    @Test
    fun `parseJsonLdScript should return null and catch exception on malformed json string`() {
        val malformedJson = "{ malformed invalid json string: 123 }"
        val result = WebRecipeImporter.parseJsonLdScript(malformedJson)

        assertThat(result).isNull()
    }

    @Test
    fun `parseJsonLdScript should handle type as JSONArray and image as JSONObject`() {
        val jsonLd = """
            {
              "@context": "https://schema.org",
              "@type": ["WebPage", "Recipe"],
              "name": "Panna cotta",
              "image": { "@type": "ImageObject", "url": "https://images.marmiton.org/panna.jpg" },
              "recipeInstructions": "Étape 1: Faire chauffer la crème\nÉtape 2: Laisser refroidir",
              "recipeCategory": ["Dessert", "Gâteau"]
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(jsonLd)

        assertThat(result).isNotNull()
        assertThat(result!!.title).isEqualTo("Panna cotta")
        assertThat(result.imageUrl).isEqualTo("https://images.marmiton.org/panna.jpg")
        assertThat(result.instructions).hasSize(2)
        assertThat(result.category).isEqualTo("Dessert")
    }

    @Test
    fun `parseJsonLdScript should return null when type is not Recipe`() {
        val jsonLd = """
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "name": "Article sur la gastronomie"
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(jsonLd)

        assertThat(result).isNull()
    }

    @Test
    fun `normalizeRecipeCategory should handle all course types and fallbacks`() {
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Entrée")).isEqualTo("Entrée")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Starter")).isEqualTo("Entrée")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Plat principal")).isEqualTo("Plat principal")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Main course")).isEqualTo("Plat principal")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Dessert")).isEqualTo("Dessert")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Pâtisserie")).isEqualTo("Dessert")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Cuisine Belge")).isEqualTo("Cuisine Belge")
        assertThat(WebRecipeImporter.normalizeRecipeCategory(null)).isNull()
        assertThat(WebRecipeImporter.normalizeRecipeCategory("   ")).isNull()
    }

    @Test
    fun `parseIsoDurationToMinutes should handle hours and minutes ISO strings`() {
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT15M")).isEqualTo("15")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT1H")).isEqualTo("60")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT1H30M")).isEqualTo("90")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("invalid")).isNull()
    }

    @Test
    fun `parseJsonLdScript should handle recipeInstructions as JSONArray and yield as String`() {
        val jsonLd = """
            {
              "@context": "https://schema.org",
              "@type": "Recipe",
              "name": "Ratatouille",
              "recipeYield": "4 personnes",
              "image": "https://images.marmiton.org/ratatouille.jpg",
              "recipeInstructions": [
                { "@type": "HowToStep", "text": "Couper les légumes" },
                "Faire mijoter 30 minutes"
              ],
              "recipeCategory": "Plat principal"
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(jsonLd)

        assertThat(result).isNotNull()
        assertThat(result!!.title).isEqualTo("Ratatouille")
        assertThat(result.servings).isEqualTo("4")
        assertThat(result.imageUrl).isEqualTo("https://images.marmiton.org/ratatouille.jpg")
        assertThat(result.instructions).hasSize(2)
        assertThat(result.instructions).contains("Couper les légumes")
        assertThat(result.instructions).contains("Faire mijoter 30 minutes")
        assertThat(result.category).isEqualTo("Plat principal")
    }
}
