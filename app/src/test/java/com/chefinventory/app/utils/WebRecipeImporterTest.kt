package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.jsoup.Jsoup
import org.junit.Test
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket

/**
 * Tests unitaires dédiés (1-à-1) pour [WebRecipeImporter].
 */
class WebRecipeImporterTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsRecipeResultValid_AllCases() {
        assertThat(WebRecipeImporter.isRecipeResultValid(null)).isFalse()

        val nullTitleRes = WebRecipeImporter.WebRecipeResult(title = null, ingredients = emptyList(), instructions = emptyList(), prepTimeMinutes = null, cookTimeMinutes = null, servings = null, imageUrl = null, sourceUrl = "")
        assertThat(WebRecipeImporter.isRecipeResultValid(nullTitleRes)).isFalse()

        val blankTitleRes = WebRecipeImporter.WebRecipeResult(title = "   ", ingredients = emptyList(), instructions = emptyList(), prepTimeMinutes = null, cookTimeMinutes = null, servings = null, imageUrl = null, sourceUrl = "")
        assertThat(WebRecipeImporter.isRecipeResultValid(blankTitleRes)).isFalse()

        val validRes = WebRecipeImporter.WebRecipeResult(title = "Crêpes", ingredients = emptyList(), instructions = emptyList(), prepTimeMinutes = null, cookTimeMinutes = null, servings = null, imageUrl = null, sourceUrl = "")
        assertThat(WebRecipeImporter.isRecipeResultValid(validRes)).isTrue()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testFetchHtmlDocumentImpl_DefaultLambdaFlow() {
        val originalDefault = WebRecipeImporter.fetchDefaultHtmlDocumentImpl
        val originalImpl = WebRecipeImporter.fetchHtmlDocumentImpl

        try {
            val html = "<html><head><title>Tarte Aux Fraises</title></head><body></body></html>"
            WebRecipeImporter.fetchDefaultHtmlDocumentImpl = { Jsoup.parse(html) }

            val doc = WebRecipeImporter.fetchHtmlDocumentImpl("https://example.com/tarte")
            assertThat(doc.title()).isEqualTo("Tarte Aux Fraises")
        } finally {
            WebRecipeImporter.fetchDefaultHtmlDocumentImpl = originalDefault
            WebRecipeImporter.fetchHtmlDocumentImpl = originalImpl
        }

        // Start local in-memory ServerSocket to test Jsoup.connect().get() nominal flow
        val serverSocket = ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"))
        val port = serverSocket.localPort

        val thread = Thread {
            try {
                while (!serverSocket.isClosed) {
                    val socket = try { serverSocket.accept() } catch (_: Exception) { break }
                    val reader = socket.getInputStream().bufferedReader()
                    while (true) {
                        val line = reader.readLine()
                        if (line.isNullOrBlank()) break
                    }
                    val responseHtml = "<html><head><title>Tarte Aux Pommes Web</title></head><body></body></html>"
                    val bytes = responseHtml.toByteArray(Charsets.UTF_8)
                    val httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n$responseHtml"
                    socket.getOutputStream().write(httpResponse.toByteArray(Charsets.UTF_8))
                    socket.getOutputStream().flush()
                    socket.close()
                }
            } catch (_: Exception) {}
        }
        thread.start()

        try {
            val url = "http://127.0.0.1:$port/test"
            val doc = WebRecipeImporter.fetchDefaultHtmlDocumentImpl(url)
            assertThat(doc.title()).isEqualTo("Tarte Aux Pommes Web")

            val docImpl = WebRecipeImporter.fetchHtmlDocumentImpl(url)
            assertThat(docImpl.title()).isEqualTo("Tarte Aux Pommes Web")
        } finally {
            try { serverSocket.close() } catch (_: Exception) {}
            try { thread.join(1000) } catch (_: Exception) {}
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_UnsupportedImageAndInstructionsType() {
        // Image is a number 123 and recipeInstructions is a number 123
        val json = """
            {
              "@type": "Recipe",
              "name": "Recette spéciale",
              "image": 123,
              "recipeInstructions": 123
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(json)
        assertThat(result).isNotNull()
        assertThat(result?.imageUrl).isNull()
        assertThat(result?.instructions).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_NonArrayGraphAndInvalidInstructionType() {
        // @graph is not a JSONArray (L125)
        val nonArrayGraphJson = """{"@graph": "pas_un_tableau_graph"}"""
        assertThat(WebRecipeImporter.parseJsonLdScript(nonArrayGraphJson)).isNull()

        // recipeInstructions array contains non-JSONObject and non-String element (e.g. number 123) (L171)
        val invalidInstTypeJson = """
            {
              "@type": "Recipe",
              "name": "Crêpes maison",
              "recipeInstructions": [123, "Faire sauter la crêpe."]
            }
        """.trimIndent()
        val result = WebRecipeImporter.parseJsonLdScript(invalidInstTypeJson)
        assertThat(result?.instructions).containsExactly("Faire sauter la crêpe.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_StringInstructionsWithBlankLines() {
        val json = """
            {
              "@type": "Recipe",
              "name": "Crêpes rapides",
              "recipeInstructions": "Étape 1\n  \nÉtape 2"
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(json)
        assertThat(result?.instructions).containsExactly("Étape 1", "Étape 2")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_WithNonObjectInArrayAndGraph() {
        // Empty array [] and empty @graph []
        assertThat(WebRecipeImporter.parseJsonLdScript("[]")).isNull()
        assertThat(WebRecipeImporter.parseJsonLdScript("{\"@graph\": []}")).isNull()
        assertThat(WebRecipeImporter.parseJsonLdScript("texte_brut_non_json")).isNull()

        // Array containing non-JSONObject string element (L112)
        val jsonArrayStr = """
            [
              "chaine_non_objet",
              {"@type": "Recipe", "name": "Soupe à l'oignon"}
            ]
        """.trimIndent()
        val result1 = WebRecipeImporter.parseJsonLdScript(jsonArrayStr)
        assertThat(result1?.title).isEqualTo("Soupe à l'oignon")

        // @graph containing non-JSONObject string element (L122)
        val jsonGraphStr = """
            {
              "@graph": [
                "chaine_non_objet",
                {"@type": "Recipe", "name": "Tarte Tatin"}
              ]
            }
        """.trimIndent()
        val result2 = WebRecipeImporter.parseJsonLdScript(jsonGraphStr)
        assertThat(result2?.title).isEqualTo("Tarte Tatin")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_WithBlankLinesAndNonNumericYieldAndCustomCategory() {
        val json = """
            {
              "@type": "Recipe",
              "name": "Plat sur-mesure",
              "recipeYield": "pour plusieurs convives",
              "recipeCategory": "SpecialCategory999",
              "image": "https://example.com/photo.jpg",
              "recipeIngredient": ["200 g de farine", "   "],
              "recipeInstructions": ["Cuire au four.", "   ", {"@type": "HowToStep", "text": "   "}]
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(json)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Plat sur-mesure")
        assertThat(result?.category).isEqualTo("SpecialCategory999")
        assertThat(result?.imageUrl).isEqualTo("https://example.com/photo.jpg")
        assertThat(result?.servings).isNull()
        assertThat(result?.ingredients).containsExactly("200 g de farine")
        assertThat(result?.instructions).containsExactly("Cuire au four.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_StringInstructionsAndImageBlank() {
        val json = """
            {
              "@type": "Recipe",
              "name": "Gâteau",
              "image": "   ",
              "recipeInstructions": "Couper les pommes.\nCuire au four."
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(json)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Gâteau")
        assertThat(result?.imageUrl).isNull()
        assertThat(result?.instructions).containsExactly("Couper les pommes.", "Cuire au four.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testWebRecipeResultDataClassGeneratedMethods() {
        val res1 = WebRecipeImporter.WebRecipeResult(
            title = "Tarte",
            ingredients = listOf("Pommes"),
            instructions = listOf("Cuire"),
            prepTimeMinutes = "15",
            cookTimeMinutes = "30",
            servings = "4",
            imageUrl = "https://example.com/img.jpg",
            sourceUrl = "https://example.com"
        )
        val res2 = res1.copy()
        val res3 = res1.copy(title = "Gâteau")

        assertThat(res1).isEqualTo(res2)
        assertThat(res1).isNotEqualTo(res3)
        assertThat(res1.hashCode()).isEqualTo(res2.hashCode())
        assertThat(res1.toString()).contains("WebRecipeResult")
        assertThat(res1.component1()).isEqualTo("Tarte")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_DefaultSourceUrlOverloadAndBlankTitleScript() {
        // 1. Call parseJsonLdScript with default argument sourceUrl = ""
        val json = """{"@type": "Recipe", "name": "Crêpes"}"""
        val result = WebRecipeImporter.parseJsonLdScript(json)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Crêpes")
        assertThat(result?.sourceUrl).isEmpty()

        // 2. HTML containing blank script tag + Recipe script with blank title
        val htmlWithBlankScript = """
            <html>
                <head>
                    <script type="application/ld+json">   </script>
                    <script type="application/ld+json">
                        {"@type": "Article", "name": "Article du Chef"}
                    </script>
                    <script type="application/ld+json">
                        {"@type": "Recipe", "name": "   "}
                    </script>
                    <script type="application/ld+json">
                        {"@type": "Recipe", "name": "Gaufres"}
                    </script>
                </head>
                <body></body>
            </html>
        """.trimIndent()

        val parsedHtml = WebRecipeImporter.parseHtmlDocument(Jsoup.parse(htmlWithBlankScript), "https://example.com/gaufres")
        assertThat(parsedHtml.title).isEqualTo("Gaufres")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseJsonLdScript_RecipeYieldVariantsAndImageEmptyAndArrayCategory() {
        val json = """
            {
              "@type": "Recipe",
              "name": "Soupe à l'oignon",
              "recipeYield": ["4 bols", "6 personnes"],
              "recipeCategory": ["  ", "Entrée"],
              "image": {"@type": "ImageObject"},
              "recipeIngredient": ["3 oignons"]
            }
        """.trimIndent()

        val result = WebRecipeImporter.parseJsonLdScript(json, "https://example.com/soupe")
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Soupe à l'oignon")
        assertThat(result?.servings).isEqualTo("4")
        assertThat(result?.category).isEqualTo("Entrée")
        assertThat(result?.imageUrl).isNull()
    }

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
        assertThat(WebRecipeImporter.normalizeRecipeCategory("   ")).isNull()
        assertThat(WebRecipeImporter.normalizeRecipeCategory("")).isNull()
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
    @CoveredRequirement("6.1.1.9")
    fun testImportFromUrl_SuccessfulFetch() {
        val html = "<html><head><title>Tarte aux Pommes</title></head><body></body></html>"
        val originalImpl = WebRecipeImporter.fetchHtmlDocumentImpl
        WebRecipeImporter.fetchHtmlDocumentImpl = { Jsoup.parse(html) }

        try {
            val result = WebRecipeImporter.importFromUrl("https://example.com/tarte")
            assertThat(result.title).isEqualTo("Tarte aux Pommes")
        } finally {
            WebRecipeImporter.fetchHtmlDocumentImpl = originalImpl
        }
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testImportFromUrl_InvalidUrl_ReturnsFallbackResult() {
        val result = WebRecipeImporter.importFromUrl("https://invalid-non-existent-domain-12345.com/recette")
        assertThat(result.title).isNull()
        assertThat(result.ingredients).isEmpty()
    }
}
