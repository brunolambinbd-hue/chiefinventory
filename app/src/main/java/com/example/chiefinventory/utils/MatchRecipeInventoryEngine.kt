package com.example.chiefinventory.utils

import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient

/**
 * Represents the match result of a recipe against available pantry inventory.
 *
 * @property recipe The candidate recipe being evaluated.
 * @property totalRequired The total number of ingredients required by the recipe.
 * @property totalInStock The number of required ingredients currently available in stock.
 * @property missingIngredients The list of missing ingredient lines formatted for display.
 * @property matchPercentage The feasibility percentage (0.0 to 100.0).
 */
@Suppress("RedundantVisibilityModifier")
public data class RecipeMatchResult(
    public val recipe: Recipe,
    public val totalRequired: Int,
    public val totalInStock: Int,
    public val missingIngredients: List<String>,
    public val matchPercentage: Double
)

/**
 * Engine evaluating recipe feasibility based on current pantry inventory stock.
 *
 * @see REQ-4.6.1.1 Garde-manger recipe feasibility matching engine.
 * @see REQ-4.6.1.2 Formatting missing ingredients with quantities and units.
 */
@Suppress("IfThenToElvis", "FoldInitializerAndIfNull", "kotlin:S6529", "RedundantVisibilityModifier")
public object MatchRecipeInventoryEngine {

    private val unitPrefixPattern: Regex by lazy {
        val vocab = try {
            OcrVocabularyLoader.loadFromProjectResources()
        } catch (_: Exception) {
            null
        }
        val loadedUnits = vocab?.culinaryMeasurementUnits ?: emptyList()
        val loadedConnectors = vocab?.ingredientConnectors ?: emptyList()

        val extraWords = listOf(
            "c. à soupe", "c. à café", "c à soupe", "c à café", "cuillère", "cuillere",
            "g", "kg", "cl", "ml", "l", "sachet", "sachets", "pot", "pots", "bocal", "bocaux",
            "boîte", "boite", "boîtes", "ravier", "raviers", "tranche", "tranches",
            "paquet", "paquets", "flacon", "flacons", "botte", "bottes", "bouteille", "bouteilles",
            "de", "du", "des", "d'", "au", "aux", "à", "a", "en", "pour"
        )
        val allWords = (loadedUnits + loadedConnectors + extraWords)
            .distinct()
            .filter { it.isNotBlank() }
            .sortedByDescending { it.length }
            .joinToString("|") { Regex.escape(it) }

        Regex("(?i)\\b($allWords)\\b")
    }

    /**
     * Cleans an ingredient name by purging measurement unit prefixes and non-alphabetic characters.
     *
     * @param text The raw ingredient name text.
     * @return The sanitized lowercase string for matching.
     */
    private fun cleanNameForMatching(text: String): String {
        return text.lowercase()
            .replace(unitPrefixPattern, " ")
            .replace(Regex("[^\\p{L}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Formats a numeric quantity into a string representation without trailing zeroes.
     *
     * @param qty The quantity value, or null if unspecified.
     * @return The formatted quantity string.
     */
    private fun formatQuantity(qty: Double?): String {
        if (qty == null) return ""
        return if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
    }

    /**
     * Formats a culinary unit string with trailing space if present.
     *
     * @param unit The unit string, or null if unspecified.
     * @return The formatted unit string.
     */
    private fun formatUnit(unit: String?): String {
        if (unit.isNullOrBlank()) return ""
        return "$unit "
    }

    /**
     * Formats supplemental info in parentheses if present.
     *
     * @param info The supplemental info string, or null if unspecified.
     * @return The formatted supplemental info string.
     */
    private fun formatSupplementalInfo(info: String?): String {
        if (info.isNullOrBlank()) return ""
        return " ($info)"
    }

    /**
     * Evaluates the feasibility of each recipe by comparing required ingredients against pantry stock.
     *
     * @param recipes The list of candidate recipes.
     * @param recipeIngredientsMap Map associating recipe ID to required ingredients.
     * @param pantryIngredients The list of available pantry ingredients.
     * @return The list of match results sorted by match percentage descending.
     */
    public fun evaluateRecipesFeasibility(
        recipes: List<Recipe>,
        recipeIngredientsMap: Map<Long, List<RecipeIngredient>>,
        pantryIngredients: List<Ingredient>
    ): List<RecipeMatchResult> {
        val inStockMap = pantryIngredients.associate {
            val qty = it.quantity
            val isPositive = if (qty != null) qty > 0.0 else false
            cleanNameForMatching(it.name) to isPositive
        }

        val results = recipes.map { recipe ->
            val reqListNull = recipeIngredientsMap[recipe.id]
            val requiredList = if (reqListNull != null) reqListNull else emptyList()
            var countInStock = 0
            val missing = mutableListOf<String>()

            for (req in requiredList) {
                val cleanReq = cleanNameForMatching(req.ingredientName)
                val isSel = cleanReq.contains("sel")
                val isPoivre = cleanReq.contains("poivre")
                val isSeasoningReq = isSel || isPoivre

                val isPresent = inStockMap.entries.any { (cleanPantry, inStock) ->
                    if (!inStock) {
                        false
                    } else if (cleanReq.isBlank()) {
                        false
                    } else if (cleanPantry.contains(cleanReq) || cleanReq.contains(cleanPantry)) {
                        true
                    } else if (isSeasoningReq && (cleanPantry.contains("sel") || cleanPantry.contains("poivre"))) {
                        true
                    } else {
                        false
                    }
                }

                if (isPresent) {
                    countInStock++
                } else {
                    val formattedQty = formatQuantity(req.quantityRequired)
                    val unitStr = formatUnit(req.unit)
                    val infoStr = formatSupplementalInfo(req.supplementalInfo)
                    val fullFormatted = "$formattedQty $unitStr${req.ingredientName}$infoStr".replace(Regex("\\s+"), " ").trim()

                    missing.add(if (!fullFormatted.isBlank()) fullFormatted else req.ingredientName)
                }
            }

            val total = requiredList.size
            val percentage = if (total > 0) (countInStock.toDouble() / total) * 100.0 else 0.0

            RecipeMatchResult(
                recipe = recipe,
                totalRequired = total,
                totalInStock = countInStock,
                missingIngredients = missing,
                matchPercentage = percentage
            )
        }

        return results.sortedByDescending { it.matchPercentage }
    }
}
