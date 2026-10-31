package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.jsoup.Jsoup
import org.junit.Test

/**
 * Tests unitaires dédiés (1-à-1) pour [WebRecipeImporter].
 */
class WebRecipeImporterTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_ValidRecipeJsonObject() {
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
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_JsonArrayAndGraphAndStringInstructionsAndImageVariants() {
        // 1. JSONArray with non-recipe first, recipe second
        val jsonArrayStr = """
            [
              {"name": "NoTypeItem"},
              {"@type": "WebPage", "name": "Accueil"},
              {
                "@type": ["Thing", "Recipe"],
                "name": "Poulet Basquaise",
                "recipeCategory": ["Plat principal", "Volaille"],
                "recipeIngredient": ["1 poulet", "2 poivrons"],
                "recipeInstructions": "Découper le poulet.\nFaire revenir les poivrons.",
                "recipeYield": ["6 personnes"],
                "image": {"@type": "ImageObject", "url": "https://example.com/poulet.jpg"}
              }
            ]
        """.trimIndent()

        val result1 = WebRecipeImporter.parseJsonLdScript(jsonArrayStr, "https://example.com/poulet")
        assertThat(result1).isNotNull()
        assertThat(result1?.title).isEqualTo("Poulet Basquaise")
        assertThat(result1?.category).isEqualTo("Plat principal")
        assertThat(result1?.ingredients).hasSize(2)
        assertThat(result1?.instructions).containsExactly("Découper le poulet.", "Faire revenir les poivrons.")
        assertThat(result1?.servings).isEqualTo("6")
        assertThat(result1?.imageUrl).isEqualTo("https://example.com/poulet.jpg")

        // 2. @graph object with image array and string array instructions
        val jsonGraphStr = """
            {
              "@context": "https://schema.org",
              "@graph": [
                {"@type": "Organization", "name": "Marmiton"},
                {
                  "@type": "Recipe",
                  "name": "Gratin Dauphinois",
                  "recipeCategory": "Plat principal",
                  "recipeIngredient": ["1kg pommes de terre", "50cl crème"],
                  "recipeInstructions": ["Éplucher les pommes de terre.", "Enfourner à 180°C."],
                  "image": ["https://example.com/gratin.jpg"]
                }
              ]
            }
        """.trimIndent()

        val result2 = WebRecipeImporter.parseJsonLdScript(jsonGraphStr, "https://example.com/gratin")
        assertThat(result2).isNotNull()
        assertThat(result2?.title).isEqualTo("Gratin Dauphinois")
        assertThat(result2?.instructions).containsExactly("Éplucher les pommes de terre.", "Enfourner à 180°C.")
        assertThat(result2?.imageUrl).isEqualTo("https://example.com/gratin.jpg")
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testParseJsonLdScript_InvalidJsonOrNonRecipe_ReturnsNull() {
        val invalidJson = "{ invalid json }"
        assertThat(WebRecipeImporter.parseJsonLdScript(invalidJson, "https://example.com")).isNull()

        val nonRecipeJson = """{"@type": "Article", "name": "Article Culinaire"}"""
        assertThat(WebRecipeImporter.parseJsonLdScript(nonRecipeJson, "https://example.com")).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseIsoDurationToMinutes_Iso8601FormatVariants() {
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT15M")).isEqualTo("15")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT1H30M")).isEqualTo("90")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT2H")).isEqualTo("120")
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("PT0M")).isNull()
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("")).isNull()
        assertThat(WebRecipeImporter.parseIsoDurationToMinutes("Invalid")).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalizeRecipeCategory_CategorizesStartersMainsDesserts() {
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Entrée froide")).isEqualTo("Entrée")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Plat principal")).isEqualTo("Plat principal")
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Gâteau au chocolat")).isEqualTo("Dessert")
        assertThat(WebRecipeImporter.normalizeRecipeCategory(null)).isNull()
        assertThat(WebRecipeImporter.normalizeRecipeCategory("Autre catégorie inconnue")).isEqualTo("Autre catégorie inconnue")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseHtmlDocument_WithScriptTagAndFallback() {
        // Html with valid JSON-LD script
        val htmlWithScript = """
            <html>
                <head>
                    <script type="application/ld+json">
                        {"@type": "Recipe", "name": "Mousse au chocolat", "recipeIngredient": ["200g chocolat"]}
                    </script>
                </head>
                <body></body>
            </html>
        """.trimIndent()

        val resultWithScript = WebRecipeImporter.parseHtmlDocument(Jsoup.parse(htmlWithScript), "https://example.com/mousse")
        assertThat(resultWithScript.title).isEqualTo("Mousse au chocolat")

        // Html without JSON-LD script
        val html = "<html><head><title>Tarte Tatin - Marmiton</title></head><body></body></html>"
        val resultHtml = WebRecipeImporter.parseHtmlDocument(Jsoup.parse(html), "https://marmiton.org/tarte")
        assertThat(resultHtml.title).isEqualTo("Tarte Tatin")
        assertThat(resultHtml.ingredients).isEmpty()

        // Empty title fallback
        val emptyTitleHtml = "<html><head><title></title></head><body></body></html>"
        val resultEmpty = WebRecipeImporter.parseHtmlDocument(Jsoup.parse(emptyTitleHtml), "https://example.com")
        assertThat(resultEmpty.title).isEqualTo("Recette Web")
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testImportFromUrl_InvalidUrl_ReturnsFallbackResult() {
        val result = WebRecipeImporter.importFromUrl("https://invalid-non-existent-domain-12345.com/recette")
        assertThat(result.title).isNull()
        assertThat(result.ingredients).isEmpty()
    }
}
