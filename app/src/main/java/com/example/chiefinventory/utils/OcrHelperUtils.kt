package com.example.chiefinventory.utils

/**
 * Utility functions for OCR parsing logic.
 */
object OcrHelperUtils {

    /**
     * Compte le nombre de séquences d'ingrédients sur une ligne.
     */
    internal fun countIngredientSequences(line: String): Int {
        val qtyPattern = "(?:\\d+(?:/\\d+)?|[1Il!|](?:/\\d+)?)"
        val pattern = Regex("(?<!(?:ou|et|sur)\\s)(?:^|(?<=\\s))$qtyPattern\\s+(?!(?:mm|cm|min|sec)\\b)[\\p{L}]", RegexOption.IGNORE_CASE)
        
        return pattern.findAll(line).count { match ->
            !isInsideParentheses(line, match.range.first)
        }
    }

    /**
     * Découpe une ligne contenant plusieurs ingrédients (ex: "4 toasts salade de blé").
     * Utilise les ingrédients communs sans quantité comme points de rupture.
     */
    internal fun splitCombinedIngredients(line: String, commonItems: List<String>): List<String> {
        if (line.isBlank()) return emptyList()
        
        val hacheVariants = listOf("haché", "hachée", "hachés", "hachées")
        val otherKeywords = commonItems.filter { it.lowercase() !in hacheVariants }
        
        val otherKeywordsRegex = if (otherKeywords.isNotEmpty()) {
            otherKeywords.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }
        } else {
            "MATCH_NOTHING_RANDOM_UNLIKELY_STRING"
        }
        val hacheRegex = hacheVariants.joinToString("|") { Regex.escape(it) }

        val connectors = "et|ou|à|\\-|de|du|des|d'|sur"
        val qtyPattern = "(?:\\d+(?:/\\d+)?|[1Il!|](?:/\\d+)?)"

        // Regex de split améliorée : accepte la virgule ou la fin de mot avant l'ingrédient connu
        val potentialSplits = Regex(
            "(?<=[\\p{L}).,])(?<!\\b(?:$connectors))\\s+(?!(?:$connectors)\\s+)(?=$qtyPattern\\s+[\\p{L}])|" +
            "(?<=[\\p{L},])(?<!\\b(?:$connectors))\\s+(?=$otherKeywordsRegex)|" +
            "(?<=[\\p{L},])(?<!\\b(?:$connectors))\\s+(?=(?:$hacheRegex)\\b\\s+(?:de|du|d'|d\\s+))",
            RegexOption.IGNORE_CASE
        )

        val sb = StringBuilder(line)
        var offset = 0
        potentialSplits.findAll(line).forEach { match ->
            if (!isInsideParentheses(line, match.range.first)) {
                val splitPos = match.range.first + offset
                sb.insert(splitPos + 1, "##SPLIT##")
                offset += "##SPLIT##".length
            }
        }

        return sb.toString().split("##SPLIT##")
            .map { it.trim().replace(Regex("[,:;\\s\\-]+$"), "") } // Nettoyage des résidus de ponctuation
            .filter { it.isNotBlank() }
    }

    private fun isInsideParentheses(text: String, position: Int): Boolean {
        var openCount = 0
        for (i in 0 until position) {
            if (i < text.length) {
                if (text[i] == '(') openCount++
                if (text[i] == ')') openCount--
            }
        }
        return openCount > 0
    }

    internal fun isLikelyProperNameOrSource(line: String): Boolean {
        val trimmed = line.trim()
        val words = trimmed.split(Regex("\\s+"))
        if (words.size !in 2..4) return false
        if (trimmed.any { it.isDigit() }) return false
        return words.all { word -> word.isNotEmpty() && (word[0].isUpperCase() || word.all { it.isUpperCase() }) }
    }

    internal fun cleanIngredientSemantics(
        text: String, 
        excludedKeywords: List<String>, 
        semanticExclusions: List<String> = emptyList()
    ): String {
        var cleaned = text.trim().replace(Regex("^[•\\-*]\\s*"), "")
        if (!cleaned.any { it.isLetter() } || cleaned.length <= 1) return ""
        if (isLikelyProperNameOrSource(cleaned)) return ""
        
        val upperCleaned = cleaned.uppercase().trim()
        if (excludedKeywords.any { upperCleaned == it.uppercase().trim() }) return ""
        if (semanticExclusions.any { upperCleaned == it.uppercase().trim() }) return ""

        return cleaned
    }
}
