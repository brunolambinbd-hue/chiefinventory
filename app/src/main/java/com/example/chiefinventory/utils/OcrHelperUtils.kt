package com.example.chiefinventory.utils

/**
 * Utility functions for OCR parsing logic.
 */
object OcrHelperUtils {

    /**
     * Compte le nombre de séquences d'ingrédients sur une ligne.
     * Reconnait les chiffres et les variantes OCR courantes pour le chiffre 1 (l, I, |, !).
     */
    internal fun countIngredientSequences(line: String): Int {
        // On cherche des nombres (ou variantes OCR) qui NE sont PAS précédés par des connecteurs
        // ET qui ne sont PAS suivis par des unités techniques.
        // On remplace \b par (?:^|(?<=\s)) pour supporter les caractères non-word comme | ou !
        val qtyPattern = "(?:\\d+|[1Il!|])"
        val pattern = Regex("(?<!(?:ou|et|sur)\\s)(?:^|(?<=\\s))$qtyPattern\\s+(?!(?:mm|cm|min|sec)\\b)[a-zA-Z]", RegexOption.IGNORE_CASE)
        
        // On filtre les séquences qui sont à l'intérieur de parenthèses
        return pattern.findAll(line).count { match ->
            !isInsideParentheses(line, match.range.first)
        }
    }

    /**
     * Découpe une ligne contenant plusieurs ingrédients.
     * Ignore les séparateurs potentiels situés à l'intérieur de parenthèses.
     */
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

        val connectors = "et|ou|à|\\-|de|du|des|d'|sur"
        val qtyPattern = "(?:\\d+|[1Il!|])"

        // On identifie tous les splits potentiels par Regex
        val potentialSplits = Regex(
            "(?<=[a-zA-Z).])(?<!\\b(?:$connectors))\\s+(?!(?:$connectors)\\s+)(?=$qtyPattern\\s+[a-zA-Z])|" +
            "(?<=[a-zA-Z])(?<!\\b(?:$connectors))\\s+(?=$otherKeywordsRegex)|" +
            "(?<=[a-zA-Z])(?<!\\b(?:$connectors))\\s+(?=(?:$hacheRegex)\\b\\s+(?:de|du|d'|d\\s+))",
            RegexOption.IGNORE_CASE
        )

        // On ne garde que les splits qui ne sont PAS à l'intérieur de parenthèses
        val sb = StringBuilder(cleaned)
        var offset = 0
        potentialSplits.findAll(cleaned).forEach { match ->
            if (!isInsideParentheses(cleaned, match.range.first)) {
                val splitPos = match.range.first + offset
                sb.insert(splitPos + 1, "##SPLIT##")
                offset += "##SPLIT##".length
            }
        }

        return sb.toString().split("##SPLIT##").map { it.trim() }.filter { it.isNotBlank() }
    }

    /**
     * Vérifie si une position donnée dans une chaîne est à l'intérieur de parenthèses.
     */
    private fun isInsideParentheses(text: String, position: Int): Boolean {
        var openCount = 0
        for (i in 0 until position) {
            if (text[i] == '(') openCount++
            if (text[i] == ')') openCount--
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

    internal fun isExcluded(line: String, excludedKeywords: List<String>): Boolean {
        val upperLine = line.uppercase().trim()
        if (upperLine.isEmpty()) return false
        return excludedKeywords.any { upperLine == it.uppercase().trim() }
    }

    /**
     * Nettoyage sémantique final des ingrédients.
     * Désormais, on CONSERVE le contenu des parenthèses car il contient le poids (ex: (t l kg)).
     */
    internal fun cleanIngredientSemantics(
        text: String, 
        excludedKeywords: List<String>, 
        semanticExclusions: List<String> = emptyList()
    ): String {
        // 1. Suppression des puces au début (•, -, *)
        var cleaned = text.trim().replace(Regex("^[•\\-*]\\s*"), "")
        
        // 2. Vérification de la validité
        if (!cleaned.any { it.isLetter() } || cleaned.length <= 1) return ""
        if (isLikelyProperNameOrSource(cleaned)) return ""
        
        val upperCleaned = cleaned.uppercase().trim()
        
        // 3. Vérification contre l'exclusion globale (stricte)
        if (excludedKeywords.any { upperCleaned == it.uppercase().trim() }) return ""
        
        // 4. Vérification contre l'exclusion sémantique spécifique aux ingrédients (ex: POUR)
        if (semanticExclusions.any { upperCleaned == it.uppercase().trim() }) return ""

        return cleaned
    }
}
