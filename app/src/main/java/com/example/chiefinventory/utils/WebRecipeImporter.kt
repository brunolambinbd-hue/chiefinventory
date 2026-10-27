package com.example.chiefinventory.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup

/**
 * Moteur d'importation universelle de recettes Web basé sur le standard international Schema.org / JSON-LD Recipe.
 */
object WebRecipeImporter {

    private const val TAG = "WebRecipeImporter"

    /**
     * Résultat d'importation d'une recette Web.
     *
     * @property title Titre de la recette.
     * @property category Catégorie extraite et normalisée (Entrée, Plat principal, Dessert).
     * @property ingredients Liste des lignes d'ingrédients.
     * @property instructions Liste des consignes/étapes.
     * @property prepTimeMinutes Temps de préparation en minutes.
     * @property cookTimeMinutes Temps de cuisson en minutes.
     * @property servings Nombre de portions.
     * @property imageUrl URL de la photo d'illustration HD.
     * @property sourceUrl URL Web source de la recette.
     */
    data class WebRecipeResult(
        val title: String?,
        val category: String? = null,
        val ingredients: List<String>,
        val instructions: List<String>,
        val prepTimeMinutes: String?,
        val cookTimeMinutes: String?,
        val servings: String?,
        val imageUrl: String?,
        val sourceUrl: String
    )

    /**
     * Analyse un document HTML JSoup et extrait la recette depuis ses balises script JSON-LD ou ses balises meta.
     */
    fun parseHtmlDocument(doc: org.jsoup.nodes.Document, url: String): WebRecipeResult {
        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            val html = script.html().trim()
            if (html.isBlank()) continue

            val result = parseJsonLdScript(html, url)
            if (result != null && !result.title.isNullOrBlank()) {
                return result
            }
        }

        // Fallback meta tags HTML si pas de JSON-LD
        val titleFallback = doc.title().replace(Regex("(?i)\\s*[-–•]\\s*(marmiton|750g|cuisine az).*"), "").trim()
        return WebRecipeResult(
            title = titleFallback.ifBlank { "Recette Web" },
            ingredients = emptyList(),
            instructions = emptyList(),
            prepTimeMinutes = null,
            cookTimeMinutes = null,
            servings = null,
            imageUrl = null,
            sourceUrl = url
        )
    }

    /**
     * Télécharge la page Web via JSoup et extrait le bloc JSON-LD Schema.org Recipe.
     *
     * @param url L'URL de la recette Web.
     * @return L'objet [WebRecipeResult] structuré.
     */
    fun importFromUrl(url: String): WebRecipeResult {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeout(10000)
            .get()
        return parseHtmlDocument(doc, url)
    }

    /**
     * Parse une chaîne de script JSON-LD et cherche un objet de type "Recipe".
     */
    fun parseJsonLdScript(jsonStr: String, sourceUrl: String = ""): WebRecipeResult? {
        try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val res = parseRecipeJsonObject(obj, sourceUrl)
                    if (res != null) return res
                }
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                if (obj.has("@graph")) {
                    val graph = obj.optJSONArray("@graph")
                    if (graph != null) {
                        for (i in 0 until graph.length()) {
                            val graphObj = graph.optJSONObject(i) ?: continue
                            val res = parseRecipeJsonObject(graphObj, sourceUrl)
                            if (res != null) return res
                        }
                    }
                }
                return parseRecipeJsonObject(obj, sourceUrl)
            }
        } catch (_: Exception) {
            // Ignoré
        }
        return null
    }

    private fun parseRecipeJsonObject(obj: JSONObject, sourceUrl: String): WebRecipeResult? {
        val isRecipe = if (obj.has("@type")) {
            val typeVal = obj.get("@type")
            if (typeVal is JSONArray) {
                (0 until typeVal.length()).any { typeVal.optString(it).contains("Recipe", ignoreCase = true) }
            } else {
                typeVal.toString().contains("Recipe", ignoreCase = true)
            }
        } else false

        val name = obj.optString("name", "").trim()
        if (!isRecipe) return null
        val ingredientsList = mutableListOf<String>()
        val ingrArray = obj.optJSONArray("recipeIngredient")
        if (ingrArray != null) {
            for (i in 0 until ingrArray.length()) {
                val line = ingrArray.optString(i, "").trim()
                if (line.isNotBlank()) ingredientsList.add(line)
            }
        }

        val instructionsList = mutableListOf<String>()
        if (obj.has("recipeInstructions")) {
            val instVal = obj.get("recipeInstructions")
            if (instVal is JSONArray) {
                for (i in 0 until instVal.length()) {
                    val item = instVal.get(i)
                    if (item is JSONObject) {
                        val text = item.optString("text", "").trim()
                        if (text.isNotBlank()) instructionsList.add(text)
                    } else if (item is String) {
                        if (item.isNotBlank()) instructionsList.add(item.trim())
                    }
                }
            } else if (instVal is String) {
                instVal.split("\n").map { it.trim() }.filter { it.isNotBlank() }.forEach { instructionsList.add(it) }
            }
        }

        val prepTime = parseIsoDurationToMinutes(obj.optString("prepTime", ""))
        val cookTime = parseIsoDurationToMinutes(obj.optString("cookTime", ""))

        var yieldStr: String? = null
        if (obj.has("recipeYield")) {
            val yVal = obj.get("recipeYield")
            if (yVal is JSONArray) {
                yieldStr = yVal.optString(0, "")
            } else {
                yieldStr = yVal.toString()
            }
            yieldStr = yieldStr.replace(Regex("[^0-9]"), "").trim().ifBlank { null }
        }

        var imgUrl: String? = null
        if (obj.has("image")) {
            val imgVal = obj.get("image")
            if (imgVal is JSONArray) {
                imgUrl = imgVal.optString(0, "")
            } else if (imgVal is JSONObject) {
                imgUrl = imgVal.optString("url", "")
            } else if (imgVal is String) {
                imgUrl = imgVal
            }
            if (imgUrl.isNullOrBlank()) imgUrl = null
        }

        var rawCategory: String? = null
        if (obj.has("recipeCategory")) {
            val catVal = obj.get("recipeCategory")
            rawCategory = if (catVal is JSONArray) {
                (0 until catVal.length()).map { catVal.optString(it) }.firstOrNull { it.isNotBlank() }
            } else {
                catVal.toString().trim()
            }
        }
        val normalizedCategory = normalizeRecipeCategory(rawCategory)

        return WebRecipeResult(
            title = name.ifBlank { null },
            category = normalizedCategory,
            ingredients = ingredientsList,
            instructions = instructionsList,
            prepTimeMinutes = prepTime,
            cookTimeMinutes = cookTime,
            servings = yieldStr,
            imageUrl = imgUrl,
            sourceUrl = sourceUrl
        )
    }

    fun normalizeRecipeCategory(rawCategory: String?): String? {
        if (rawCategory.isNullOrBlank()) return null
        val lower = rawCategory.lowercase()
        return when {
            lower.contains("entrée") || lower.contains("entree") || lower.contains("starter") || lower.contains("appetizer") || lower.contains("amuse-bouche") -> "Entrée"
            lower.contains("plat") || lower.contains("main") || lower.contains("dinner") || lower.contains("viande") || lower.contains("poisson") -> "Plat principal"
            lower.contains("dessert") || lower.contains("gâteau") || lower.contains("gateau") || lower.contains("patisserie") || lower.contains("pâtisserie") || lower.contains("douceur") -> "Dessert"
            else -> rawCategory.trim()
        }
    }

    /**
     * Convertit une durée ISO 8601 (ex: "PT15M", "PT1H30M") en nombre de minutes sous forme de texte.
     */
    fun parseIsoDurationToMinutes(isoStr: String): String? {
        if (isoStr.isBlank()) return null
        val match = Regex("(?i)PT(?:(\\d+)H)?(?:(\\d+)M)?").find(isoStr) ?: return null
        val hours = match.groupValues[1].toIntOrNull() ?: 0
        val mins = match.groupValues[2].toIntOrNull() ?: 0
        val total = hours * 60 + mins
        return if (total > 0) total.toString() else null
    }
}
