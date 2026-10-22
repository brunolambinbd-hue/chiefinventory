package com.example.chiefinventory.utils

/**
 * Utility functions for OCR parsing logic.
 */
object OcrHelperUtils {

    /**
     * Compte le nombre de séquences d'ingrédients sur une ligne.
     */
    internal fun countIngredientSequences(line: String): Int {
        if (line.isBlank()) return 0
        // Ne pas autoriser de lettre immédiatement après le symbole de quantité 1 (l, I, 1, |, !) pour éviter d'attraper 'i' dans 'ingredients'
        val qtyPattern = "(?:\\d+(?:/\\d+)?|(?<=\\s|^)[1Il!|](?!\\p{L})(?:/\\d+)?)"
        val pattern = Regex("(?:^|(?<=\\s))$qtyPattern", RegexOption.IGNORE_CASE)
        
        val excludedNextWords = setOf("mm", "cm", "min", "sec", "ou", "et", "à", "sur")
        
        return pattern.findAll(line).count { match ->
            if (isInsideParentheses(line, match.range.first)) return@count false
            
            // On regarde le mot juste après la quantité
            val textAfter = line.substring(match.range.last + 1).trimStart() // <- Correction de l'index
            val nextWord = textAfter.split(Regex("[\\s,.;]")).firstOrNull()?.lowercase()
            
            nextWord != null && nextWord !in excludedNextWords && nextWord.any { it.isLetter() }
        }
    }

    /**
     * Découpe une ligne contenant plusieurs ingrédients (ex : "4 toasts salade de blé").
     * Utilise les ingrédients communs sans quantité comme points de rupture.
     */
    internal fun splitCombinedIngredients(
        line: String, 
        commonItems: List<String>, 
        mandatoryComplements: List<String> = emptyList()
    ): List<String> {
        var cleanedLine = line.trim()
        if (cleanedLine.isBlank()) return emptyList()

        val lower = cleanedLine.lowercase()
        if (commonItems.any { it.trim().lowercase() == lower }) {
            return listOf(cleanedLine)
        }

        // Nettoyage des numérotations OCR parasites (ex: "12 34 1 Oignon")
        // On ne nettoie que si on a au moins 3 blocs numériques/quantités au début
        val prefixMatch = Regex("^(\\d+\\s+){2,}\\d+").find(cleanedLine)
        if (prefixMatch != null) {
            cleanedLine = cleanedLine.substring(prefixMatch.range.last + 1).trim()
        }
        
        if (cleanedLine.isBlank()) return emptyList()
        
        val hacheVariants = listOf("haché", "hachée", "hachés", "hachées")
        val otherKeywords = commonItems.filter { it.lowercase() !in hacheVariants }
        
        val otherKeywordsRegex = if (otherKeywords.isNotEmpty()) {
            otherKeywords.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }
        } else {
            "MATCH_NOTHING_RANDOM_UNLIKELY_STRING"
        }
        val hacheRegex = hacheVariants.joinToString("|") { Regex.escape(it) }

        val baseConnectors = listOf("et", "ou", "à", "-", "de", "du", "des", "d", "au", "aux", "sur", "le", "la", "les", "un", "une", "soupe", "café", "gousse", "gousses", "pincée", "filet", "botte", "branche", "salade", "beau", "beaux", "belle", "belles")
        val allConnectors = (baseConnectors + mandatoryComplements.map { it.lowercase() }).distinct()
        val connectors = allConnectors.joinToString("|") { Regex.escape(it) }
        val modifierWords = "haché|hachée|hachés|hachées|émincé|émincée|émincés|émincées|ciselé|ciselée|ciselés|ciselées|râpé|râpée|râpés|râpées|fondu|fondue|fondus|fondues|forte|douce|moulu|moulée|concassé|concassée|décortiquées|fumé|fumée"
        val qtyPattern = "(?:\\d+(?:/\\d+)?|(?<=\\s|^)[1Il!|](?!\\p{L})(?:/\\d+)?)"

        val potentialSplits = Regex(
            "(?<=[\\p{L}).,])(?<!\\b(?:$connectors))\\s+(?!(?:$connectors|$modifierWords)\\b)(?=$qtyPattern\\s+[\\p{L}])|" +
            "(?<=[\\p{L},])(?<!\\b(?:$connectors))\\s+(?!(?:$modifierWords)\\b)(?=$otherKeywordsRegex)|" +
            "(?<=[\\p{L},])(?<!\\b(?:$connectors))\\s+(?=(?:$hacheRegex)\\b\\s+(?:de|du|d'|d\\s+))",
            RegexOption.IGNORE_CASE
        )

        val sb = StringBuilder(cleanedLine)
        var offset = 0
        potentialSplits.findAll(cleanedLine).forEach { match ->
            if (!isInsideParentheses(cleanedLine, match.range.first)) {
                val splitPos = match.range.first + offset
                sb.insert(splitPos + 1, "##SPLIT##")
                offset += "##SPLIT##".length
            }
        }

        return sb.toString().split("##SPLIT##")
            .map { it.trim().replace(Regex("[,:;\\s\\-]+$"), "") }
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

    internal fun isExcluded(text: String, excludedKeywords: List<String>): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        return excludedKeywords.any { it.trim().equals(trimmed, ignoreCase = true) }
    }

    internal fun cleanIngredientSemantics(
        text: String, 
        excludedKeywords: List<String>, 
        semanticExclusions: List<String> = emptyList(),
        standaloneArticles: List<String> = emptyList()
    ): String {
        val cleaned = text.trim().replace(Regex("^[•\\-*]\\s*"), "")
        if (!cleaned.any { it.isLetter() } || cleaned.length <= 1) return ""
        if (isLikelyProperNameOrSource(cleaned)) return ""
        
        if (isExcluded(cleaned, excludedKeywords)) return ""
        if (isExcluded(cleaned, semanticExclusions)) return ""

        val words = cleaned.split(Regex("\\s+"))
        if (words.size == 1) {
            val cleanWord = words[0].lowercase().replace(Regex("[^\\p{L}]"), "")
            val baseArticles = setOf("du", "de", "des", "le", "la", "les", "un", "une", "au", "aux", "en", "par", "pour", "sur", "ce", "cette", "ces", "sa", "son", "ses")
            val allArticles = baseArticles + standaloneArticles.map { it.lowercase() }
            if (cleanWord in allArticles) {
                return ""
            }
        }

        return cleaned
    }
}
