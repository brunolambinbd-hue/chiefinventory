package com.example.chiefinventory.utils

import android.util.Log

/**
 * Extracteur spécialisé d'ingrédients implicites pour les recettes narratives courtes (Type 2).
 *
 * Responsabilités :
 * - Reconstituer le texte narratif continu à partir des phrases de consignes d'instructions.
 * - Recoudre les paires d'ingrédients/quantités tronquées sur plusieurs lignes.
 * - Parser et formater proprement les ingrédients quantifiés pour alimenter [RawSections.rawIngredientsList] afin d'indexer la recette dans la base de données du garde-manger.
 * - Filtrer rigoureusement les durées de temps ("5 mn", "3 h") et températures ("200°C") via [isDurationOrTime].
 */
object Ocr3NarrativeIngredientExtractor {
    private const val TAG = "Ocr3NarrativeExt"

    /**
     * Détermine si un segment quantifié correspond à une durée de temps ou une température de cuisson (ex: "5 mn", "3 h", "15 minutes", "200°C")
     * afin d'empêcher son ajout dans la liste des ingrédients du garde-manger.
     *
     * @param text Le segment de texte brut.
     * @param parsedUnit L'unité identifiée par le parser.
     * @param parsedName Le nom d'aliment extrait par le parser.
     * @return `true` s'il s'agit d'une durée de temps ou température, `false` s'il s'agit d'un ingrédient légitime.
     */
    fun isDurationOrTime(text: String, parsedUnit: String?, parsedName: String): Boolean {
        val lowerText = text.lowercase().trim()
        val lowerUnit = parsedUnit?.lowercase()?.trim() ?: ""
        val lowerName = parsedName.lowercase().trim()

        val durationKeywords = setOf("mn", "min", "mins", "minute", "minutes", "sec", "secondes", "h", "hr", "hrs", "heure", "heures")

        if (lowerUnit in durationKeywords) return true
        if (lowerName in durationKeywords) return true

        // Détection de motifs de durée isolée (ex: "5 mn", "3h", "15 min à feu moyen")
        val isDurationMatch = Regex("(?i)^\\d+(?:[/.,]\\d+)?\\s*(?:mn|min|mins|minute|minutes|sec|secondes|h|hr|hrs|heure|heures)\\b").containsMatchIn(lowerText)
        if (isDurationMatch) return true

        // Détection de température (ex: "200°C", "180 °")
        if (lowerText.contains("°c") || lowerText.contains("°")) return true

        return false
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
            if (isDurationOrTime(rawSegment, parsed.unit, parsed.name) || Ocr2Cleaner.isTechnicalDimension(rawSegment) || Ocr2Cleaner.isTechnicalDimension(parsed.name)) continue

            if (parsed.quantity != null && parsed.name.isNotBlank()) {
                val cleanNameWords = parsed.name.split(Regex("\\s+"))
                    .map { it.trim().replace(Regex("[^\\p{L}]"), "") }
                    .filter { word ->
                        val lowerWord = word.lowercase()
                        lowerWord.length >= 2 && lowerWord !in actionVerbs && lowerWord !in listOf("vous", "nous", "dans", "feu", "moyen", "temps", "que", "pour", "pendant", "bon", "soupe", "glacée", "savoir")
                    }
                var cleanName = cleanNameWords.joinToString(" ").trim()
                cleanName = cleanName.replace(Regex("(?i)\\s+(?:et|ou|de|d')$"), "").trim()

                if (cleanName.length >= 2) {
                    val qtyStr = if (parsed.quantity % 1.0 == 0.0) parsed.quantity.toInt().toString() else parsed.quantity.toString()
                    val unitStr = if (!parsed.unit.isNullOrEmpty()) "${parsed.unit} " else ""
                    val formattedIng = "$qtyStr $unitStr$cleanName".replace(Regex("\\s+"), " ").trim()

                    val primaryKeyword = cleanNameWords.firstOrNull { it.length >= 3 }?.lowercase() ?: cleanName.lowercase()
                    if (primaryKeyword !in seenKeywords) {
                        seenKeywords.add(primaryKeyword)
                        results.rawIngredientsList.add(formattedIng)
                        Log.d(TAG, "  [INGRÉDIENT IMPLICITE EXTRAIT] -> '$formattedIng'")
                    }
                }
            }
        }

        // 3. Recherche des assaisonnements et ingrédients courants non quantifiés (ex: "sel et poivre", "sel", "poivre")
        val lowerFullText = fullText.lowercase()
        val seasoningList = listOf("sel et poivre", "sel, poivre", "sel", "poivre")
        for (seasoning in seasoningList) {
            if (OcrRegexes.getWholeWordPattern(seasoning).containsMatchIn(lowerFullText)) {
                val formattedSeasoning = seasoning.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                if ("sel" !in seenKeywords && "poivre" !in seenKeywords) {
                    seenKeywords.add("sel")
                    seenKeywords.add("poivre")
                    results.rawIngredientsList.add(formattedSeasoning)
                    Log.d(TAG, "  [ASSAISONNEMENT IMPLICITE EXTRAIT] -> '$formattedSeasoning'")
                    break
                }
            }
        }
    }
}
