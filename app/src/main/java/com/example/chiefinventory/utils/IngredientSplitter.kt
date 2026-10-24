package com.example.chiefinventory.utils

/**
 * Utility to split combined ingredient lines into separate items.
 */
object IngredientSplitter {

    private val ARTICLES = setOf("le", "la", "les", "un", "une", "des", "du", "de", "d'", "d")
    private val SIZE_PREFIX_REGEX = Regex("^(?:\\d+|1/2|1/4|un|une)\\s+(?:petit|petite|petits|petites|grand|grande|grands|grandes|moyen|moyenne|moyens|moyennes|beau|beaux|belle|belles)$", RegexOption.IGNORE_CASE)

    /**
     * Divides a line if it contains multiple ingredients.
     * Example: "2 gousses d'ail sel, poivre" -> ["2 gousses d'ail", "sel", "poivre"]
     * Example: "1 piment rouge persil haché" -> ["1 piment rouge", "persil haché"]
     */
    fun splitCombinedIngredients(line: String, commonItems: List<String>): List<String> {
        val lower = line.lowercase().trim()
        if (commonItems.any { it.trim().lowercase() == lower }) {
            return listOf(line.trim())
        }

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
                    val pattern = Regex("(?<=.{3})${Regex.escape(keyword)}\\b", RegexOption.IGNORE_CASE)
                    val match = pattern.find(currentPart)
                    if (match != null) {
                        val prefix = currentPart.substring(0, match.range.first).trim()
                        val lowerPrefix = prefix.lowercase()
                        val endsWithConnector = lowerPrefix.endsWith(" de") || lowerPrefix.endsWith(" d'") || lowerPrefix.endsWith(" du") || lowerPrefix.endsWith(" des") || lowerPrefix.endsWith(" au") || lowerPrefix.endsWith(" aux")
                        val lastPrefixWord = lowerPrefix.split(Regex("\\s+")).lastOrNull() ?: ""
                        val endsWithModifier = lastPrefixWord in listOf("fine", "fines", "grand", "grande", "grands", "grandes", "petit", "petite", "petits", "petites", "beau", "beaux", "belle", "belles")
                        if (lowerPrefix !in ARTICLES && !SIZE_PREFIX_REGEX.matches(lowerPrefix) && !endsWithConnector && !endsWithModifier) {
                            result.add(prefix)
                            currentPart = currentPart.substring(match.range.first).trim()
                            found = true
                            break
                        }
                    }
                }
            } while (found)
            result.add(currentPart)
        }
        return result.filter { it.isNotBlank() }.distinct()
    }
}
