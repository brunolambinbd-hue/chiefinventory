package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup

/**
 * Moteur d'importation universelle de recettes Web basé sur le standard international Schema.org / JSON-LD Recipe.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object WebRecipeImporter {

    private const val TAG = "WebRecipeImporter"

    internal var fetchHtmlDocumentImpl: (String) -> org.jsoup.nodes.Document = { url ->
        Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeout(10000)
            .get()
    }

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

    internal fun isRecipeResultValid(result: WebRecipeResult?): Boolean {
        if (result == null) return false
        val title = result.title ?: return false
        return title.isNotBlank()
    }

    /**
     * Analyse un document HTML JSoup et extrait la recette depuis ses balises script JSON-LD ou ses balises meta.
     */
    fun parseHtmlDocument(doc: org.jsoup.nodes.Document, url: String): WebRecipeResult {
        LogManager.d(TAG, "Analyse du document HTML pour URL: $url")
        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            val html = script.html().trim()
            if (html.isBlank()) continue

            val result = parseJsonLdScript(html, url)
            if (isRecipeResultValid(result)) {
                LogManager.d(TAG, "Recette JSON-LD extraite avec succès pour '$url': ${result!!.title}")
                return result
            } else {
                LogManager.d(TAG, "Recette JSON-LD ignorée ou sans titre pour '$url'")
            }
        }

        // Fallback meta tags HTML si pas de JSON-LD
        val titleFallback = doc.title().replace(Regex("(?i)\\s*[-–•]\\s*(marmiton|750g|cuisine az).*"), "").trim()
        LogManager.d(TAG, "Aucun bloc JSON-LD valide trouvé. Utilisation du fallback titre: $titleFallback")
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
        return try {
            LogManager.d(TAG, "Début de l'importation Web depuis l'URL: $url")
            val doc = fetchHtmlDocumentImpl(url)
            val result = parseHtmlDocument(doc, url)
            LogManager.d(TAG, "Importation Web terminée avec succès pour '$url'")
            result
        } catch (e: Exception) {
            LogManager.e(TAG, "Échec de l'importation Web depuis '$url': ${e.localizedMessage}", e)
            WebRecipeResult(
                title = null,
                ingredients = emptyList(),
                instructions = emptyList(),
                prepTimeMinutes = null,
                cookTimeMinutes = null,
                servings = null,
                imageUrl = null,
                sourceUrl = url
            )
        }
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
        } catch (e: Exception) {
            LogManager.e(TAG, "Erreur d'analyse du JSON-LD: ${e.localizedMessage}", e)
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
            when (val instVal = obj.get("recipeInstructions")) {
                is JSONArray -> {
                    for (i in 0 until instVal.length()) {
                        when (val item = instVal.get(i)) {
                            is JSONObject -> {
                                val text = item.optString("text", "").trim()
                                if (text.isNotBlank()) instructionsList.add(text)
                            }
                            is String -> {
                                if (item.isNotBlank()) instructionsList.add(item.trim())
                            }
                        }
                    }
                }
                is String -> {
                    val rawLines = instVal.split("\n")
                    for (rawLine in rawLines) {
                        val cleanLine = rawLine.trim()
                        if (cleanLine.isNotBlank()) {
                            instructionsList.add(cleanLine)
                        } else {
                            // Ligne vide ignorée
                        }
                    }
                }
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
            imgUrl = when (val imgVal = obj.get("image")) {
                is JSONArray -> imgVal.optString(0, "")
                is JSONObject -> imgVal.optString("url", "")
                is String -> imgVal
                else -> null
            }
            val cleanImg = imgUrl
            imgUrl = if (cleanImg != null && cleanImg.isNotBlank()) {
                cleanImg
            } else {
                null
            }
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
        if (rawCategory == null || rawCategory.isBlank()) {
            return null
        } else {
            val trimmed = rawCategory.trim()
            val normalized = CategoryMapper.normalizeRecipeCategory(trimmed)
            return if (normalized != null) normalized else trimmed
        }
    }

    /**
     * Convertit une durée ISO 8601 (ex : "PT15M", "PT1H30M") en nombre de minutes sous forme de texte.
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
