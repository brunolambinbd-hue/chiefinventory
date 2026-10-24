package com.example.chiefinventory.utils

import android.util.Log

/**
 * Catégoriseur spécialisé pour les recettes narratives courtes sans section d'ingrédients dédiée (Type 2).
 *
 * Responsabilités :
 * - Détecter de manière infaillible si un scan OCR correspond à une recette narrative courte (sans en-tête d'ingrédients).
 * - Conserver 100% du texte de préparation fluide et intact dans [RawSections.rawInstructionsList] sans découper les phrases en deux.
 * - Extraire les ingrédients implicites (quantités + noms d'aliments) contenus dans les consignes pour alimenter [RawSections.rawIngredientsList]
 *   afin d'indexer la recette pour le moteur de recherche par garde-manger.
 */
object Ocr3NarrativeCategorizer {
    private const val TAG = "Ocr3NarrativeCat"

    /**
     * Détermine si le scan de recette correspond à une recette narrative courte sans section d'ingrédients dédiée (Type 2).
     *
     * @param lines La liste des lignes de texte scannées nettoyées (sans le titre).
     * @param vocabulary Le vocabulaire métier OCR.
     * @return `true` s'il s'agit d'une recette narrative sans section d'ingrédients, `false` sinon (Redirection Type 1 classique).
     */
    fun isNarrativeInstructionOnlyRecipe(lines: List<String>, vocabulary: OcrVocabulary): Boolean {
        if (lines.isEmpty()) return false

        // 1. Si la fiche contient un en-tête d'ingrédients explicite ("INGRÉDIENTS", "COMPOSITION"), ce n'est PAS du Type 2 !
        val hasExplicitIngrHeader = lines.any { line ->
            val lower = line.lowercase().trim()
            lower.contains("ingrédients") || lower.contains("ingredients") || lower.contains("composition") || lower.contains("ngrédients")
        }
        if (hasExplicitIngrHeader) return false

        // 1.5 Si la fiche contient un en-tête d'instructions ET un ingrédient quantifié -> Type 1 !
        val hasInstructionHeader = lines.any { line ->
            val lower = line.lowercase().trim()
            (lower == "instructions" || lower == "réalisation" || lower == "méthode" || lower == "progression") && !lower.contains("mn") && !lower.contains("min")
        }
        val hasIngredientWithQuantity = lines.any { line ->
            val parsed = IngredientParser.parse(line, vocabulary.culinaryMeasurementUnits)
            val unitLower = parsed.unit?.lowercase()?.trim() ?: ""
            val isDurationUnit = unitLower in listOf("min", "mn", "sec", "h", "min.", "mn.", "sec.", "h.") || (parsed.quantity != null && (line.lowercase().endsWith(" min") || line.lowercase().endsWith(" mn") || line.lowercase().endsWith(" h")))
            parsed.quantity != null && parsed.name.isNotBlank() && !isDurationUnit
        }
        if (hasInstructionHeader && hasIngredientWithQuantity) return false

        // 1.6 Si la fiche contient un bloc d'ingrédients du bas ("POUR X PERSONNES") -> Type 3 Inversé !
        val hasBottomIngredientList = lines.indices.drop(2).any { i ->
            val lower = lines[i].lowercase().trim()
            val nextLower = if (i + 1 < lines.size) lines[i + 1].lowercase().trim() else ""
            (lower.contains("pour") && lower.contains("personnes")) ||
            (lower == "pour" && nextLower.contains("personnes"))
        }
        if (hasBottomIngredientList) return false

        // 2. Si la fiche contient au moins une ligne d'ingrédient autonome AVANT le début des instructions
        var hasInstructionStarted = false
        val hasStandaloneIngredientLine = lines.any { line ->
            val lower = line.lowercase().trim()
            val hasAction = OcrInstructionUtils.hasActionVerb(line, vocabulary.stepActionKeywords)
            val isHeader = lower == "pour" || lower.startsWith("préparation") || lower.startsWith("cuisson") || lower.startsWith("repos") || lower.startsWith("réalisation") || lower.startsWith("instructions")
            
            val isMetaHeader = lower.startsWith("préparation") || lower.startsWith("cuisson") || lower.startsWith("repos") || lower == "pour" || lower.startsWith("pour ")
            
            if (hasAction || (OcrInstructionUtils.hasInstructionTrigger(line, vocabulary.instructionSwitchKeywords) && !isMetaHeader)) {
                hasInstructionStarted = true
            }

            if (isHeader || hasInstructionStarted) return@any false

            val parsed = IngredientParser.parse(line, vocabulary.culinaryMeasurementUnits)
            parsed.quantity != null && (parsed.name.length >= 3 || !parsed.unit.isNullOrBlank())
        }
        if (hasStandaloneIngredientLine) return false

        // 3. Recherche de la première véritable ligne d'instruction (verbe d'action ou numéro d'étape)
        val firstInstructionLine = lines.firstOrNull { line ->
            val lower = line.lowercase().trim()
            if (lower == "pour" || lower.startsWith("préparation") || lower.startsWith("cuisson") || lower.startsWith("repos")) return@firstOrNull false
            if (OcrHelperUtils.isLikelyProperNameOrSource(line, vocabulary)) return@firstOrNull false
            if (vocabulary.excludedOcrKeywords.any { it.equals(line.trim(), ignoreCase = true) }) return@firstOrNull false

            OcrInstructionUtils.hasActionVerb(line, vocabulary.stepActionKeywords) ||
                    OcrInstructionUtils.hasInstructionTrigger(line, vocabulary.instructionSwitchKeywords) ||
                    Regex("^\\d{1,2}\\s+[\\p{Lu}]").containsMatchIn(line) ||
                    Regex("^\\d{1,2}[.)\\-]").containsMatchIn(line) ||
                    line.startsWith("•") || line.startsWith("-")
        }

        val result = firstInstructionLine != null
        Log.d(TAG, "isNarrativeInstructionOnlyRecipe = $result (firstInstructionLine='${firstInstructionLine ?: "aucun"}')")
        return result
    }

    /**
     * Catégorise une recette narrative courte : conserve l'intégralité des consignes fluides et extrait les ingrédients implicites pour le garde-manger.
     *
     * @param lines La liste des lignes de texte scannées (sans le titre).
     * @param vocabulary Le vocabulaire métier OCR.
     * @return L'objet [RawSections] alimenté avec les consignes fluides et les ingrédients pour la BDD.
     */
    fun categorizeNarrativeRecipe(lines: List<String>, vocabulary: OcrVocabulary): RawSections {
        Log.d(TAG, "--- DÉBUT CATÉGORISATION NARRATIVE (TYPE 2) ---")
        val results = RawSections()

        val instructionLines = mutableListOf<String>()
        val wineState = Ocr3CategorizerWine.WineState()

        for (line in lines) {
            var workingLine = line.trim()
            if (workingLine.isBlank()) continue

            if (wineState.isWineSectionClosed) continue

            // 1. Extraction des méta-données (Portions, Temps, Kcal)
            val metaResult = RecipeMetadataParser.extractMetadata(workingLine, results, vocabulary)
            workingLine = metaResult.remainingLine.trim()
            if (metaResult.isPureMetadata || workingLine.isBlank()) continue

            // 2. Traitement des recommandations de vins
            if (Ocr3CategorizerWine.processWineLine(workingLine, 2, true, false, false, vocabulary, wineState)) {
                continue
            }

            // 3. Traitement des sources/établissements
            if (!OcrInstructionUtils.hasActionVerb(workingLine, vocabulary.stepActionKeywords) && SourceParser.isSourceLine(workingLine, vocabulary)) {
                val cleanedSource = SourceParser.cleanSourceLine(workingLine, vocabulary)
                if (cleanedSource.isNotBlank() && cleanedSource !in results.detectedSourceList) {
                    results.detectedSourceList.add(cleanedSource)
                }
                continue
            }

            val cleanInstruction = workingLine.replace(OcrRegexes.Beautifier.BULLET_PREFIX, "").trim()
            if (cleanInstruction.isNotBlank()) {
                instructionLines.add(cleanInstruction)
            }
        }

        results.rawInstructionsList.addAll(instructionLines)
        results.detectedWineList.addAll(wineState.detectedWineList)

        // 4. Extraction d'ingrédients implicites depuis les consignes pour alimenter le moteur de recherche par garde-manger
        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(instructionLines, vocabulary, results)

        Log.d(TAG, "--- FIN CATÉGORISATION NARRATIVE (TYPE 2) ---")
        return results
    }
}
