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
        val qtyPattern = "(?:\\d+|[1Il!|])"
        val pattern = Regex("(?<!(?:ou|à|-|et|sur)\\s)\\b$qtyPattern\\s+(?!(?:mm|cm|min|sec)\\b)[a-zA-Z]", RegexOption.IGNORE_CASE)
        return pattern.findAll(line).count()
    }

    /**
     * Découpe une ligne contenant plusieurs ingrédients.
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

        // Règle de découpage améliorée :
        // 1. Après une lettre, une parenthèse ou un POINT (fin d'ingrédient précédent)
        // 2. Devant une quantité (chiffre ou variante OCR)
        val separatorPattern = Regex(
            "(?<=[a-zA-Z).])(?<!\\b(?:$connectors))\\s+(?!(?:$connectors)\\s+)(?=$qtyPattern\\s+[a-zA-Z])|" +
            "(?<=[a-zA-Z])(?<!\\b(?:$connectors))\\s+(?=$otherKeywordsRegex)|" +
            "(?<=[a-zA-Z])(?<!\\b(?:$connectors))\\s+(?=(?:$hacheRegex)\\b\\s+(?:de|du|d'|d\\s+))",
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

    /**
     * Vérifie si une ligne doit être exclue globalement (bruit OCR pur).
     */
    internal fun isExcluded(line: String, excludedKeywords: List<String>): Boolean {
        val upperLine = line.uppercase().trim()
        if (upperLine.isEmpty()) return false
        return excludedKeywords.any { upperLine == it.uppercase().trim() }
    }

    /**
     * Nettoyage sémantique final des ingrédients.
     * Supprime les puces (bullets) et filtre les bruits comme "POUR".
     */
    internal fun cleanIngredientSemantics(
        text: String, 
        excludedKeywords: List<String>, 
        semanticExclusions: List<String> = emptyList()
    ): String {
        // 1. Suppression des puces au début (•, -, *)
        var cleaned = text.trim().replace(Regex("^[•\\-*]\\s*"), "")
        
        // 2. Suppression des parenthèses et leur contenu
        cleaned = cleaned.replace(Regex("\\([^\\)]*+\\)"), "").trim()
        
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
