package com.chefinventory.app.utils

import android.util.Log

/**
 * Assistant de l'Étape 5 (Embellissement) dédié au traitement et à la fusion data-driven des ingrédients composés
 * d'un nom de base et d'un qualificatif de variété (ex: "200 g de haricots" \n "princesses" -> "200 g de haricots princesses").
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr5BeautifierIngredientVarietyMerger {
    private const val TAG = "RecipeOCR"

    /**
     * Vérifie si le nom d'ingrédient se termine par un ingrédient de base appelant une variété.
     * Exemples : "haricots", "crevettes", "poivrons", "chou", "yaourt", "saumon".
     */
    fun isBaseExpectingVariety(text: String, baseIngredients: List<String>): Boolean {
        val lower = text.lowercase().trim()
        val words = lower.split(Regex("\\s+"))
        val lastWord = words.last()
        return baseIngredients.any { base ->
            val lowerBase = base.lowercase().trim()
            lowerBase.equals(lastWord, ignoreCase = true)
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
            val lowerQual = qual.lowercase().trim()
            clean.equals(lowerQual, ignoreCase = true) || clean.startsWith("$lowerQual ")
        }
    }

    /**
     * Balaye la liste nettoyée des lignes d'ingrédients et fusionne les qualificatifs de variété orphelins avec la ligne précédente.
     *
     * @param ingredientLines La liste des lignes d'ingrédients.
     * @param baseIngredients La liste des noms d'ingrédients de base attendant une variété.
     * @param qualifiers La liste des adjectifs et qualificatifs de variété.
     * @return La liste nettoyée des lignes d'ingrédients avec variétés fusionnées.
     */
    fun beautifyIngredientVarieties(
        ingredientLines: List<String>,
        baseIngredients: List<String>,
        qualifiers: List<String>
    ): List<String> {
        if (ingredientLines.isEmpty()) return emptyList()

        val result = mutableListOf<String>()
        var idx = 0

        while (idx < ingredientLines.size) {
            val currentLine = ingredientLines[idx].trim()
            val nextLine = if (idx + 1 < ingredientLines.size) ingredientLines[idx + 1].trim() else ""

            if (nextLine.isNotEmpty()) {
                val isNextVariety = isVarietyQualifier(nextLine, qualifiers)
                val isCurrentBase = isBaseExpectingVariety(currentLine, baseIngredients)

                if (isNextVariety) {
                    if (isCurrentBase) {
                        val firstChar = nextLine[0]
                        val uncapitalizedNext = if (firstChar.isUpperCase()) {
                            firstChar.lowercase() + nextLine.substring(1)
                        } else {
                            nextLine
                        }
                        val merged = "$currentLine $uncapitalizedNext"
                        result.add(merged)
                        Log.d(TAG, "VARIETY MERGER: Fusion de la variété '$nextLine' avec '$currentLine' -> '$merged'")
                        idx += 2
                        continue
                    }
                }
            }

            result.add(currentLine)
            idx++
        }

        return result
    }
}
