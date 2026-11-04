package com.chefinventory.app.utils

/**
 * Sous-composant de l'Étape 4 (Fusion & Découpage) : Scinde les lignes d'ingrédients combinées en éléments distincts.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr4MergerIngredientSplitter {

    /**
     * Découpe une ligne contenant plusieurs ingrédients fusionnés (ex: *"4 toasts salade de blé"* ➔ *["4 toasts", "salade de blé"]*).
     *
     * @param line La ligne combinée à découper.
     * @param commonItems La liste des ingrédients connus sans quantité (`ingredients.xml`).
     * @param mandatoryComplements La liste des compléments d'ingrédients obligatoires (`complement_ingredients.xml`).
     * @return La liste des lignes d'ingrédients scindées.
     */
    fun splitCombinedIngredients(
        line: String, 
        commonItems: List<String>, 
        mandatoryComplements: List<String> = emptyList(),
        preparationModifiers: List<String> = emptyList(),
        splitterConnectors: List<String> = emptyList(),
        culinaryUnits: List<String> = emptyList()
    ): List<String> {
        var cleanedLine = line.trim()
        if (cleanedLine.isBlank()) return emptyList()

        val lower = cleanedLine.lowercase()
        if (commonItems.any { it.trim().lowercase() == lower }) {
            return listOf(cleanedLine)
        }

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

        val defaultConnectors = listOf("et", "ou", "à", "-", "de", "du", "des", "d'", "d", "au", "aux", "sur", "le", "la", "les", "un", "une", "soupe", "café", "gousse", "gousses", "pincée", "filet", "botte", "branche", "salade", "beau", "beaux", "belle", "belles", "petit", "petite", "petits", "petites", "grand", "grande", "grands", "grandes", "moyen", "moyenne", "moyens", "moyennes", "fine", "fines", "gros", "grosse", "grosses")
        val baseConnectors = if (splitterConnectors.isNotEmpty()) splitterConnectors else defaultConnectors
        val defaultUnits = listOf("paquet", "paquets", "sachet", "sachets", "ravier", "raviers", "boîte", "boite", "boîtes", "boites", "bocal", "bocaux")
        val activeUnits = if (culinaryUnits.isNotEmpty()) culinaryUnits else defaultUnits
        val allConnectors = (baseConnectors + mandatoryComplements.map { it.lowercase() } + activeUnits.map { it.lowercase() }).distinct()
        val connectors = allConnectors.joinToString("|") { Regex.escape(it) }
        
        val modifierWords = if (preparationModifiers.isNotEmpty()) {
            preparationModifiers.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it.lowercase()) }
        } else {
            "haché|hachée|hachés|hachées|émincé|émincée|émincés|émincées|ciselé|ciselée|ciselés|ciselées|râpé|râpée|râpés|râpées|fondu|fondue|fondus|fondues|forte|douce|moulu|moulée|concassé|concassée|décortiquées|fumé|fumée"
        }
        val qtyPattern = "(?:\\d+(?:/\\d+)?|(?<=\\s|^)[1Il!|](?!\\p{L})(?:/\\d+)?)"

        val seasoningRegex = "sel\\s+et\\s+poivre|sel,\\s*poivre"
        val potentialSplits = Regex(
            "(?<=[\\p{L}).,])(?<!\\b(?:$connectors))\\s+(?!(?:$connectors|$modifierWords)\\b)(?<!\\()(?=$qtyPattern\\s+[\\p{L}])|" +
            "(?<=[\\p{L},])(?<!\\b(?:$connectors))\\s+(?!(?:$modifierWords)\\b)(?=$otherKeywordsRegex)|" +
            "(?<=[\\p{L},])(?<!\\b(?:$connectors))\\s+(?=(?:$hacheRegex)\\b\\s+(?:de|du|d'|d\\s+))|" +
            "(?<=[\\p{L}])\\s+(?=$seasoningRegex\\b)",
            RegexOption.IGNORE_CASE
        )

        val sb = StringBuilder(cleanedLine)
        var offset = 0
        potentialSplits.findAll(cleanedLine).forEach { match ->
            System.err.println("DEBUG SPLIT MATCH = '" + match.value + "' at " + match.range + " in '" + cleanedLine + "'")
            val matchEndText = cleanedLine.substring(match.range.last + 1).trimStart()
            val isParenthesisQuantity = matchEndText.startsWith("(") || OcrTextUtils.isInsideParentheses(cleanedLine, match.range.first)
            if (!isParenthesisQuantity) {
                val splitPos = match.range.first + offset
                sb.insert(splitPos + 1, "##SPLIT##")
                offset += "##SPLIT##".length
            }
        }

        return sb.toString().split("##SPLIT##")
            .map { it.trim().replace(Regex("[,:;\\s\\-]+$"), "") }
            .filter { it.isNotBlank() }
    }
}
