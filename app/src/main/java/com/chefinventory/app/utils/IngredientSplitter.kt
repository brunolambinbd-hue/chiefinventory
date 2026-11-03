package com.chefinventory.app.utils

/**
 * Utility to split combined ingredient lines into separate items.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object IngredientSplitter {

    private val ARTICLES = setOf("le", "la", "les", "un", "une", "des", "du", "de", "d'", "d")
    private val SIZE_PREFIX_REGEX = Regex("^(?:\\d+|1/2|1/4|un|une)\\s+(?:petit|petite|petits|petites|grand|grande|grands|grandes|moyen|moyenne|moyens|moyennes|beau|beaux|belle|belles)$", RegexOption.IGNORE_CASE)
    private val MODIFIERS = setOf("fine", "fines", "grand", "grande", "grands", "grandes", "petit", "petite", "petits", "petites", "beau", "beaux", "belle", "belles")

    /**
     * Détermine si le préfixe précédant un mot-clé constitue une séparation d'ingrédient valide.
     */
    internal fun isValidSplitPrefix(lowerPrefix: String): Boolean {
        if (lowerPrefix in ARTICLES) return false
        if (SIZE_PREFIX_REGEX.matches(lowerPrefix)) return false

        val endsWithDe = lowerPrefix.endsWith(" de")
        val endsWithD = lowerPrefix.endsWith(" d'")
        val endsWithDu = lowerPrefix.endsWith(" du")
        val endsWithDes = lowerPrefix.endsWith(" des")
        val endsWithAu = lowerPrefix.endsWith(" au")
        val endsWithAux = lowerPrefix.endsWith(" aux")
        val endsWithConnector = endsWithDe || endsWithD || endsWithDu || endsWithDes || endsWithAu || endsWithAux
        if (endsWithConnector) return false

        val lastPrefixWord = lowerPrefix.split(Regex("\\s+")).last()
        val endsWithModifier = lastPrefixWord in MODIFIERS
        if (endsWithModifier) return false

        return true
    }

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
                        if (isValidSplitPrefix(lowerPrefix)) {
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
        return result.distinct()
    }
}
