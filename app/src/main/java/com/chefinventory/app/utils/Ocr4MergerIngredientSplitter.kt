package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Sous-composant de l'Étape 4 (Fusion & Découpage) : Scinde les lignes d'ingrédients combinées en éléments distincts.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr4MergerIngredientSplitter {
    private const val TAG = "Ocr4MergerSplitter"

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

        val lower = java.text.Normalizer.normalize(cleanedLine.lowercase(), java.text.Normalizer.Form.NFC)
        val isSeasoningPhrase = lower.startsWith("sel de ") || lower.startsWith("sel du ") || lower.startsWith("sel d'") ||
                lower.startsWith("poivre de ") || lower.startsWith("poivre du ") || lower.startsWith("poivre d'")
        if (isSeasoningPhrase || commonItems.any { java.text.Normalizer.normalize(it.trim().lowercase(), java.text.Normalizer.Form.NFC) == lower }) {
            return listOf(cleanedLine)
        }

        val prefixMatch = Regex("^(\\d+\\s+){2,}\\d+").find(cleanedLine)
        if (prefixMatch != null) {
            cleanedLine = cleanedLine.substring(prefixMatch.range.last + 1).trim()
        }
        
        if (cleanedLine.isBlank()) return emptyList()

        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val hacheVariants = vocab.ingredientPreparationSuffixModifiers.ifEmpty { listOf("haché", "hachée", "hachés", "hachées") }
        val otherKeywords = commonItems.filter { it.lowercase() !in hacheVariants }
        
        val otherKeywordsRegex = if (otherKeywords.isNotEmpty()) {
            otherKeywords.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }
        } else {
            LogManager.w(TAG, "Attention : liste d'ingrédients connus vide, désactivation du découpage par mots-clés")
            "(?!)"
        }
        val hacheRegex = hacheVariants.joinToString("|") { Regex.escape(it) }

        // Adjectifs de taille/forme d'origine intégrés dans ocr_step4_ingredient_connectors.xml :
        // val sizeAdjectives = listOf("beau", "beaux", "belle", "belles", "petit", "petite", "petits", "petites", "grand", "grande", "grands", "grandes", "moyen", "moyenne", "moyens", "moyennes", "fine", "fines", "gros", "grosse", "grosses")
        val defaultConnectors = (vocab.ingredientConnectors + vocab.standaloneArticles + vocab.culinaryMeasurementUnits).map { it.lowercase().trim() }.distinct()
        val baseConnectors = splitterConnectors.ifEmpty { defaultConnectors }
        val defaultUnits = vocab.culinaryMeasurementUnits.map { it.lowercase().trim() }.distinct()
        val activeUnits = culinaryUnits.ifEmpty { defaultUnits }
        val allConnectors = (baseConnectors + mandatoryComplements.map { it.lowercase() } + activeUnits.map { it.lowercase() }).distinct()
        val connectors = allConnectors.joinToString("|") { Regex.escape(it) }
        
        val modifierWordsList = preparationModifiers.ifEmpty { vocab.ingredientPreparationModifiers }
        val modifierWords = modifierWordsList.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it.lowercase()) }

        val seasoningPhrases = vocab.ingredientEndMarkers.filter { it.contains(" ") }
        val seasoningRegex = seasoningPhrases.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }

        val qtyPattern = "(?:\\d+(?:/\\d+)?|(?<=\\s|^)[1Il!|](?!\\p{L})(?:/\\d+)?)"

        val potentialSplits = Regex(
            "(?<=[\\p{L}).,])(?<!\\b(?:$connectors))\\s+(?!(?:$connectors|$modifierWords)\\b)(?<!\\()(?=$qtyPattern\\s+[\\p{L}])|" +
            "(?<=[\\p{L},])(?<!\\b(?:$connectors))\\s+(?!(?:$modifierWords)\\b)(?=$otherKeywordsRegex)|" +
            "(?<=[\\p{L},])(?<!\\b(?:$connectors))\\s+(?=(?:$hacheRegex)\\b\\s+(?:de|du|d'|d\\s+))|" +
            "(?<=[\\p{L}])\\s+(?=(?:$seasoningRegex)\\b)",
            RegexOption.IGNORE_CASE
        )

        val sb = StringBuilder(cleanedLine)
        var offset = 0
        potentialSplits.findAll(cleanedLine).forEach { match ->
            LogManager.d(TAG, "DEBUG SPLIT MATCH = '" + match.value + "' at " + match.range + " in '" + cleanedLine + "'")
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
