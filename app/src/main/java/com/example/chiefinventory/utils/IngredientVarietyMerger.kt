package com.example.chiefinventory.utils

import android.util.Log

/**
 * Assistant dédié au traitement et à la fusion data-driven des ingrédients composés d'un nom de base
 * et d'un qualificatif de variété (ex: "200 g de haricots" \n "princesses" -> "200 g de haricots princesses").
 *
 * Ce sous-composant isole la logique de correspondance des qualificatifs pour réduire la charge cognitive
 * et maintenir une lisibilité optimale dans [Ocr4Merger] et [Ocr5Beautifier].
 */
object IngredientVarietyMerger {
    private const val TAG = "RecipeOCR"

    /**
     * Vérifie si le nom d'ingrédient se termine par un ingrédient de base appelant une variété.
     * Exemples : "haricots", "crevettes", "poivrons", "chou", "yaourt", "saumon".
     */
    fun isBaseExpectingVariety(text: String, baseIngredients: List<String>): Boolean {
        val lower = text.lowercase().trim()
        val words = lower.split(Regex("\\s+"))
        val lastWord = words.lastOrNull() ?: ""
        return baseIngredients.any { base ->
            base.equalsIgnoreCase(lastWord) || lower.endsWith(" $base")
        }
    }

    /**
     * Vérifie si le texte correspond à un qualificatif de variété d'ingrédient ne pouvant exister isolé.
     * Exemples : "princesses", "verts", "grises", "roses", "grec", "nature", "frais", "décortiquées".
     */
    fun isVarietyQualifier(text: String, qualifiers: List<String>): Boolean {
        val lower = text.lowercase().trim()
        val clean = lower.replace(Regex("^[^\\p{L}]+"), "").trim()
        return qualifiers.any { qual ->
            clean.equals(qual, ignoreCase = true) || clean.startsWith("${qual.lowercase()} ")
        }
    }

    /**
     * Effectue la fusion de post-processing d'une liste d'ingrédients beautified pour regrouper
     * tout qualificatif de variété orphelin isolé avec l'ingrédient de la ligne précédente.
     */
    fun beautifyIngredientVarieties(
        ingredients: List<String>,
        baseIngredients: List<String>,
        qualifiers: List<String>
    ): List<String> {
        if (ingredients.isEmpty()) return emptyList()

        val mergedList = mutableListOf<String>()
        var i = 0
        while (i < ingredients.size) {
            var current = ingredients[i].trim()
            if (current.isBlank()) {
                i++
                continue
            }

            while (i + 1 < ingredients.size) {
                val nextLine = ingredients[i + 1].trim()
                if (nextLine.isBlank()) {
                    i++
                    break
                }

                val isNextVariety = isVarietyQualifier(nextLine, qualifiers)
                val isCurrentBase = isBaseExpectingVariety(current, baseIngredients)

                if (isNextVariety && isCurrentBase) {
                    val uncapitalizedNext = nextLine.replaceFirstChar { if (it.isUpperCase()) it.lowercase() else it.toString() }
                    current = "$current $uncapitalizedNext"
                    Log.d(TAG, "VARIETY MERGER: Merged '$current' with '$nextLine'")
                    i++
                } else {
                    break
                }
            }
            mergedList.add(current)
            i++
        }
        return mergedList
    }

    private fun String.equalsIgnoreCase(other: String): Boolean {
        return this.equals(other, ignoreCase = true)
    }
}
