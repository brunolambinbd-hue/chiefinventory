package com.example.chiefinventory.utils

/**
 * Utility to split combined ingredient lines into separate items.
 */
object IngredientSplitter {

    /**
     * Divides a line if it contains multiple ingredients.
     * Example: "2 gousses d'ail sel, poivre" -> ["2 gousses d'ail", "sel", "poivre"]
     * Example: "1 piment rouge persil haché" -> ["1 piment rouge", "persil haché"]
     */
    fun splitCombinedIngredients(line: String, commonItems: List<String>): List<String> {
        // 1. D'abord on split par les séparateurs classiques (virgule, et)
        val initialParts = line.split(Regex("[,;]|\\bet\\b|\\band\\b"), 0).map { it.trim() }.filter { it.isNotBlank() }
        
        // 2. Découpage agressif par mots-clés (même collés)
        val sortedKeywords = commonItems.sortedByDescending { it.length }

        val result = mutableListOf<String>()
        for (part in initialParts) {
            var currentPart = part
            var found: Boolean
            do {
                found = false
                for (keyword in sortedKeywords) {
                    // On cherche le mot-clé précédé d'au moins 3 caractères (pour ne pas couper "1 sel")
                    val pattern = Regex("(?<=.{3})${Regex.escape(keyword)}\\b", RegexOption.IGNORE_CASE)
                    val match = pattern.find(currentPart)
                    if (match != null) {
                        result.add(currentPart.substring(0, match.range.first).trim())
                        currentPart = currentPart.substring(match.range.first).trim()
                        found = true
                        break
                    }
                }
            } while (found)
            result.add(currentPart)
        }
        return result.filter { it.isNotBlank() }.distinct()
    }
}
