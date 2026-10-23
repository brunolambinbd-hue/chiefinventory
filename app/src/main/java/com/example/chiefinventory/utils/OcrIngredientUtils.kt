package com.example.chiefinventory.utils

/**
 * Fonctions utilitaires dédiées à l'analyse et au traitement des ingrédients du pipeline OCR.
 *
 * Responsabilités :
 * - Détecter la présence d'un poids ou volume explicite d'ingrédient dans une ligne (`hasExplicitWeight`).
 * - Compter le nombre de séquences d'ingrédients distinctes sur une même ligne (`countIngredientSequences`).
 * - Scinder une ligne contenant plusieurs ingrédients fusionnés (`splitCombinedIngredients`).
 * - Effectuer le nettoyage sémantique final des puces et articles isolés (`cleanIngredientSemantics`).
 */
object OcrIngredientUtils {

    /**
     * Détermine si une ligne contient un poids ou un volume d'ingrédient explicite (ex: 200 g, 1 kg, 5 cl, 250 ml).
     * Exclut automatiquement les volumes de bouteilles de vin (`WineParser.hasWineVolume`).
     *
     * @param line La ligne de texte à analyser.
     * @return `true` si un poids ou volume explicite est trouvé, `false` sinon.
     */
    fun hasExplicitWeight(line: String): Boolean {
        return Regex("(?i)\\b\\d+(?:[.,]\\d+)?\\s*(?:g|kg|mg|cl|ml|dl|l|g\\.|kg\\.|cl\\.|ml\\.|dl\\.|l\\.)\\b").containsMatchIn(line)
    }

    /**
     * Compte le nombre de séquences d'ingrédients distinctes présentes sur une seule ligne.
     * Ignore les quantités situées à l'intérieur de parenthèses ou suivies d'unités de temps/dimensions.
     *
     * @param line La ligne de texte à analyser.
     * @return Le nombre de séquences d'ingrédients dénombrées.
     */
    fun countIngredientSequences(line: String): Int {
        if (line.isBlank()) return 0
        val qtyPattern = "(?:\\d+(?:/\\d+)?|(?<=\\s|^)[1Il!|](?!\\p{L})(?:/\\d+)?)"
        val pattern = Regex("(?:^|(?<=\\s))$qtyPattern", RegexOption.IGNORE_CASE)
        
        val excludedNextWords = setOf("mm", "cm", "min", "sec", "ou", "et", "à", "sur")
        
        return pattern.findAll(line).count { match ->
            if (OcrTextUtils.isInsideParentheses(line, match.range.first)) return@count false
            
            val textAfter = line.substring(match.range.last + 1).trimStart()
            val nextWord = textAfter.split(Regex("[\\s,.;]")).firstOrNull()?.lowercase()
            
            nextWord != null && nextWord !in excludedNextWords && nextWord.any { it.isLetter() }
        }
    }

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
        mandatoryComplements: List<String> = emptyList()
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
            if (!OcrTextUtils.isInsideParentheses(cleanedLine, match.range.first)) {
                val splitPos = match.range.first + offset
                sb.insert(splitPos + 1, "##SPLIT##")
                offset += "##SPLIT##".length
            }
        }

        return sb.toString().split("##SPLIT##")
            .map { it.trim().replace(Regex("[,:;\\s\\-]+$"), "") }
            .filter { it.isNotBlank() }
    }

    /**
     * Effectue le nettoyage sémantique d'une ligne d'ingrédient (retrait des puces, validation de longueur, filtrage des articles isolés).
     *
     * @param text La ligne d'ingrédient brute.
     * @param excludedKeywords Liste des mots-clés d'exclusion OCR (`excluded_keywords.xml`).
     * @param semanticExclusions Liste des exclusions sémantiques d'ingrédients (`ingredient_exclusion_arrays.xml`).
     * @param standaloneArticles Liste des articles isolés à vider (`standalone_articles.xml`).
     * @return La ligne d'ingrédient nettoyée ou une chaîne vide si la ligne doit être supprimée.
     */
    fun cleanIngredientSemantics(
        text: String, 
        excludedKeywords: List<String>, 
        semanticExclusions: List<String> = emptyList(),
        standaloneArticles: List<String> = emptyList()
    ): String {
        val cleaned = text.trim().replace(Regex("^[•\\-*]\\s*"), "")
        if (!cleaned.any { it.isLetter() } || cleaned.length <= 1) return ""
        if (OcrTextUtils.isLikelyProperNameOrSource(cleaned)) return ""
        
        if (OcrTextUtils.isExcluded(cleaned, excludedKeywords)) return ""
        if (OcrTextUtils.isExcluded(cleaned, semanticExclusions)) return ""

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
