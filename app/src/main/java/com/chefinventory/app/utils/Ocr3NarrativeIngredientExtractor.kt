package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Extracteur spécialisé d'ingrédients implicites pour les recettes narratives courtes (Type 2).
 *
 * Responsabilités :
 * - Reconstituer le texte narratif continu à partir des phrases de consignes d'instructions.
 * - Recoudre les paires d'ingrédients/quantités tronquées sur plusieurs lignes.
 * - Parser et formater proprement les ingrédients quantifiés pour alimenter [RawSections.rawIngredientsList] afin d'indexer la recette dans la base de données du garde-manger.
 * - Filtrer rigoureusement les durées de temps ("5 mn", "3 h") et températures ("200°C") via [isDurationOrTime].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3NarrativeIngredientExtractor {
    private const val TAG = "Ocr3NarrativeExt"

    /**
     * Détermine si un segment quantifié correspond à une durée de temps ou une température de cuisson (ex: "5 mn", "3 h", "15 minutes", "200°C")
     * afin d'empêcher son ajout dans la liste des ingrédients du garde-manger.
     *
     * @param text Le segment de texte brut.
     * @param parsedUnit L'unité identifiée par le parser.
     * @param parsedName Le nom d'aliment extrait par le parser.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return `true` s'il s'agit d'une durée de temps ou température, `false` s'il s'agit d'un ingrédient légitime.
     */
    fun isDurationOrTime(
        text: String, 
        parsedUnit: String?, 
        parsedName: String,
        vocabulary: OcrVocabulary? = null
    ): Boolean {
        val lowerText = text.lowercase().trim()
        val lowerUnit = if (parsedUnit != null) {
            parsedUnit.lowercase().trim()
        } else {
            ""
        }
        val lowerName = parsedName.lowercase().trim()

        val activeVocab = vocabulary ?: OcrVocabularyLoader.loadFromProjectResources()
        val activeUnits = activeVocab.durationUnits
        val durationKeywords = activeUnits.map { it.lowercase().trim() }.toSet()

        if (lowerUnit in durationKeywords) return true
        if (lowerName in durationKeywords) return true

        // Détection de motifs de durée isolée (ex: "5 mn", "3h", "15 min à feu moyen")
        // Ligne originale conservée en commentaire :
        // val durationRegex = Regex("(?i)^\\d+(?:[/.,]\\d+)?\\s*(?:$escapedUnits)\\b")
        val durationRegex = OcrRegexes.getDurationPrefixPattern(activeUnits)

        if (durationRegex.containsMatchIn(lowerText)) return true

        // Détection de température (ex: "200°C", "180 °")
        if (lowerText.contains("°c") || lowerText.contains("°")) return true

        return false
    }

    /**
     * Formate un ingrédient quantifié extrait d'une recette narrative.
     *
     * @param parsed L'ingrédient décomposé par [IngredientParser].
     * @param actionVerbs Ensemble des verbes d'action culinaires.
     * @param narrativeStopWords Mots-morts exclus pour l'extraction narrative (`ocr_step3_narrative_stop_words.xml`).
     * @return La chaîne d'ingrédient formatée, ou null si l'ingrédient est invalide/trop court.
     */
    internal fun formatImpliedIngredient(
        parsed: IngredientParser.ParsedIngredient,
        actionVerbs: Set<String>,
        narrativeStopWords: List<String> = emptyList()
    ): String? {
        val qty = parsed.quantity ?: return null
        if (parsed.name.isBlank()) return null

        val stopWords = narrativeStopWords.map { it.lowercase().trim() }.toSet()

        val cleanNameWords = parsed.name.split(Regex("\\s+"))
            .map { it.trim().replace(Regex("[^\\p{L}]"), "") }
            .filter { word ->
                val lowerWord = word.lowercase()
                if (lowerWord.length < 2) return@filter false
                if (lowerWord in actionVerbs) return@filter false
                if (lowerWord in stopWords) return@filter false
                true
            }
        var cleanName = cleanNameWords.joinToString(" ").trim()
        cleanName = cleanName.replace(Regex("(?i)\\s+(?:et|ou|de|d')$"), "").trim()

        if (cleanName.length < 2) return null

        val qtyStr = if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
        val currentUnit = parsed.unit
        val unitStr = if (currentUnit != null) {
            "$currentUnit "
        } else {
            ""
        }
        return "$qtyStr $unitStr$cleanName".replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Extrait les ingrédients implicites depuis les lignes de consignes narratives d'une recette de Type 2.
     *
     * @param instructionLines La liste des lignes de consignes brutes d'instructions.
     * @param vocabulary Le vocabulaire métier OCR.
     * @param results L'objet [RawSections] dont [RawSections.rawIngredientsList] sera alimenté.
     */
    fun extractImpliedIngredients(
        instructionLines: List<String>,
        vocabulary: OcrVocabulary,
        results: RawSections
    ) {
        if (instructionLines.isEmpty()) return

        // 1. Reconstitution du texte narratif continu (fusion des lignes d'instructions tronquées par saut de ligne)
        val mergedSentences = Ocr4Merger.mergeInstructions(instructionLines)
        val fullText = mergedSentences.joinToString(" ")
            .replace(OcrRegexes.Beautifier.STEP_DIGIT_PREFIX, " ")
            .replace(Regex("(?:^|\\s)\\d{1,2}\\s+(?=[\\p{Lu}])"), " ")

        val seenKeywords = mutableSetOf<String>()
        val actionVerbs = vocabulary.stepActionKeywords.map { it.lowercase().trim() }.toSet()

        // 2. Recherche de toutes les séquences quantifiées (chiffre + nom/unité) dans le texte narratif
        val qtyMatchPattern = Regex("(?i)\\b(\\d+(?:[/.,]\\d+)?\\s*(?:c\\.?\\s*[àa]\\s*(?:soupe|café)|càs|cac|g|kg|mg|cl|ml|dl|l|gousses?|pincées?|brins?|botte|verre)?\\s+(?:de\\s+|d')?[\\p{L}\\s\\-]+)")

        for (match in qtyMatchPattern.findAll(fullText)) {
            var rawSegment = match.value.trim()

            // Nettoyage des verbes d'action parasites
            actionVerbs.forEach { verb ->
                if (verb.length >= 3) {
                    rawSegment = rawSegment.replace(Regex("(?i)\\b${Regex.escape(verb)}\\b"), "").trim()
                }
            }

            val parsed = IngredientParser.parse(rawSegment, vocabulary.culinaryMeasurementUnits)
            if (isDurationOrTime(rawSegment, parsed.unit, parsed.name, vocabulary)) continue
            if (Ocr2Cleaner.isTechnicalDimension(rawSegment, vocabulary)) continue

            val formattedIng = formatImpliedIngredient(parsed, actionVerbs, vocabulary.narrativeStopWords) ?: continue

            val cleanNameWords = parsed.name.split(Regex("\\s+"))
                .map { it.trim().replace(Regex("[^\\p{L}]"), "") }
                .filter { word ->
                    val lowerWord = word.lowercase()
                    if (lowerWord.length < 2) return@filter false
                    if (lowerWord in actionVerbs) return@filter false
                    true
                }

            val longWord = cleanNameWords.firstOrNull { it.length >= 3 }
            val primaryKeyword = if (longWord != null) {
                longWord.lowercase()
            } else {
                formattedIng.lowercase()
            }

            if (primaryKeyword !in seenKeywords) {
                seenKeywords.add(primaryKeyword)
                results.rawIngredientsList.add(formattedIng)
                LogManager.d(TAG, String.format(vocabulary.logOcr3NarrativeExtractedImpliedIngr, formattedIng))
            }
        }

        // 3. Recherche des assaisonnements et ingrédients courants non quantifiés (ex: "sel et poivre", "sel", "poivre")
        val lowerFullText = fullText.lowercase()
        val seasoningList = vocabulary.ingredientEndMarkers
        for (seasoning in seasoningList) {
            if (OcrRegexes.getWholeWordPattern(seasoning).containsMatchIn(lowerFullText)) {
                val formattedSeasoning = seasoning[0].titlecase() + seasoning.substring(1)
                if ("sel" !in seenKeywords && "poivre" !in seenKeywords) {
                    seenKeywords.add("sel")
                    seenKeywords.add("poivre")
                    results.rawIngredientsList.add(formattedSeasoning)
                    LogManager.d(TAG, String.format(vocabulary.logOcr3NarrativeExtractedSeasoning, formattedSeasoning))
                    break
                } else {
                    LogManager.d(TAG, vocabulary.logOcr3NarrativeSeasoningAlreadyPresent)
                }
            }
        }
    }
}
