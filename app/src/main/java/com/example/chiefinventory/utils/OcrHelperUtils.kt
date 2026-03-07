package com.example.chiefinventory.utils

/**
 * Utility functions for OCR parsing logic.
 */
object OcrHelperUtils {

    internal fun countIngredientSequences(line: String): Int {
        // On cherche des nombres qui NE sont PAS précédés par "ou", "à", "-"
        val pattern = Regex("(?<!(?:ou|à|-)\\s)\\b\\d+\\s+[a-zA-Z]")
        return pattern.findAll(line).count()
    }

    internal fun splitCombinedIngredients(line: String, commonItems: List<String>): List<String> {
        var cleaned = line.replace(Regex("^\\d+\\s+\\d+\\s+"), "").trim()
        
        val hacheVariants = listOf("haché", "hachée", "hachés", "hachées")
        val otherKeywords = commonItems.filter { it.lowercase() !in hacheVariants }
        
        val otherKeywordsRegex = if (otherKeywords.isNotEmpty()) {
            otherKeywords.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }
        } else {
            "MATCH_NOTHING_RANDOM_UNLIKELY_STRING"
        }
        val hacheRegex = hacheVariants.joinToString("|") { Regex.escape(it) }

        // Correction : On ajoute (?<!(?:ou|à|-)) pour ne pas splitter si l'espace est précédé d'un mot de liaison d'intervalle
        val separatorPattern = Regex(
            "(?<=[a-zA-Z)])(?<!\\b(?:ou|à|-))\\s+(?!(?:ou|à|-)\\s+)(?=\\d+\\s+[a-zA-Z])|" +
            "(?<=[a-zA-Z])\\s+(?=$otherKeywordsRegex)|" +
            "(?<=[a-zA-Z])\\s+(?=(?:$hacheRegex)\\b\\s+(?:de|du|d'|d\\s+))",
            RegexOption.IGNORE_CASE
        )

        val marked = cleaned.replace(separatorPattern, "##SPLIT##")
        return marked.split("##SPLIT##").map { it.trim() }.filter { it.isNotBlank() }
    }

    internal fun isLikelyProperNameOrSource(line: String): Boolean {
        val trimmed = line.trim()
        val words = trimmed.split(Regex("\\s+"))
        if (words.size !in 2..4) return false
        if (trimmed.any { it.isDigit() }) return false
        return words.all { word -> word.isNotEmpty() && (word[0].isUpperCase() || word.all { it.isUpperCase() }) }
    }

    internal fun isExcluded(line: String, excludedKeywords: List<String>): Boolean {
        val upperLine = line.uppercase()
        return excludedKeywords.any { kw ->
            val ukw = kw.uppercase()
            if (ukw.length <= 4) upperLine == ukw || upperLine.startsWith("$ukw ") || upperLine.endsWith(" $ukw")
            else upperLine.contains(ukw)
        }
    }

    internal fun cleanIngredientSemantics(text: String, excludedKeywords: List<String>): String {
        val cleaned = text.replace(Regex("\\(.*?\\)"), "").trim()
        if (!cleaned.any { it.isLetter() } || cleaned.length <= 1) return ""
        if (isLikelyProperNameOrSource(cleaned)) return ""
        val upperCleaned = cleaned.uppercase()
        if (excludedKeywords.any { kw ->
                val ukw = kw.uppercase()
                if (ukw.length <= 4) upperCleaned == ukw else upperCleaned.contains(ukw)
            }) return ""
        return cleaned
    }
}
