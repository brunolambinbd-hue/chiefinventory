package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R

/**
 * Structure de données pour regrouper les sections extraites lors de la catégorisation.
 */
data class RawSections(
    val rawIngredientsList: MutableList<String> = mutableListOf(),
    val rawInstructionsList: MutableList<String> = mutableListOf(),
    val detectedWineList: MutableList<String> = mutableListOf(),
    val detectedSourceList: MutableList<String> = mutableListOf(),
    var detectedServings: String? = null
)

/**
 * Gère la logique de distribution des lignes OCR dans les différentes sections.
 */
object OcrCategorizer {
    private const val TAG = "RecipeOCR"

    /**
     * Parcourt les lignes pour les classer dans les listes brutes correspondantes.
     */
    internal fun categorizeLines(
        lines: List<String>,
        titleIndex: Int, // Conservé pour la signature mais plus utilisé pour l'exclusion
        res: Resources
    ): RawSections {
        val results = RawSections()
        var currentSection = 0

        // Variables de ressources nécessaires
        val wineRes = WineParser.loadResources(res)
        val sourceRes = SourceParser.loadResources(res)
        val stepActionKeywords = res.getStringArray(R.array.step_action_keywords).toList()
        val commonIngredientsNoQty = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        val excludedKeywords = res.getStringArray(R.array.excluded_ocr_keywords).toList()
        val stepConnectors = listOf("puis", "ensuite", "enfin", "après", "apres", "alors", "pendant", "dans")
        val extraVerbs = listOf("plongez", "retirez", "hachez", "ajoutez", "servez", "assaisonnez", "faites", "coupez", "mélangez", "préparez", "décorez", "répartissez", "passez", "prélevez", "lavez")

        val instructionHeaderKeywords = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression")
        val ingredientHeaderKeywords = listOf("ingrédients", "ingredients", "composition")

        val stepStartRegex = Regex("^\\s*(?:[•\\-*]|(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b)", RegexOption.IGNORE_CASE)
        val containsActionRegex = Regex("\\b(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b", RegexOption.IGNORE_CASE)
        
        // Ajout de un/une dans la détection des quantités
        val qtyRegex = Regex("^(?:[|Il!\\d\\-*•¼½¾]|un\\b|une\\b)", RegexOption.IGNORE_CASE)
        
        val servingsRegex = Regex("(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val alternateServingsRegex = Regex("(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)", RegexOption.IGNORE_CASE)

        for ((index, line) in lines.withIndex()) {
            val trimmedLine = line.trim()
            val lowerLine = trimmedLine.lowercase()

            // A. PORTIONS (Priorité haute)
            val sMatch = servingsRegex.find(trimmedLine) ?: alternateServingsRegex.find(trimmedLine)
            if (sMatch != null) {
                if (results.detectedServings == null) results.detectedServings = sMatch.groupValues[1]
                Log.d(TAG, "PERS détecté: ${results.detectedServings}")
                continue
            }

            // B. VIN
            if (WineParser.isWineLine(trimmedLine, wineRes)) {
                val cleanedWine = WineParser.cleanWineLine(trimmedLine, wineRes)
                results.detectedWineList.add(cleanedWine)
                Log.d(TAG, "WINE détecté: $cleanedWine")
                continue
            }

            // C. SOURCE
            if (OcrHelperUtils.isLikelyProperNameOrSource(trimmedLine) || SourceParser.isSourceLine(trimmedLine, sourceRes)) {
                val cleanedSource = SourceParser.cleanSourceLine(trimmedLine, sourceRes)
                results.detectedSourceList.add(cleanedSource)
                Log.d(TAG, "SOURCE détectée: $cleanedSource")
                continue
            }

            // D. BASCULES ET REMPLISSAGE
            val containsAction = containsActionRegex.containsMatchIn(trimmedLine)
            
            // Un Header doit être court et NE PAS contenir d'action narrative
            val isInstructionHeader = instructionHeaderKeywords.any { lowerLine.contains(it) } && 
                                     trimmedLine.length < 25 && !containsAction
            val isIngredientHeader = ingredientHeaderKeywords.any { lowerLine.contains(it) } && 
                                    trimmedLine.length < 25 && !containsAction

            if (isInstructionHeader) { currentSection = 2; Log.d(TAG, "Section INSTRUCTIONS détectée"); continue }
            if (isIngredientHeader) { currentSection = 1; Log.d(TAG, "Section INGRÉDIENTS détectée"); continue }

            val matchesStepStart = stepStartRegex.containsMatchIn(trimmedLine)
            val lineWithoutBullet = trimmedLine.replace(Regex("^[•\\-*]\\s*"), "")
            val lowerWithoutBullet = lineWithoutBullet.lowercase()
            
            // Vérification si la ligne commence par une unité connue (ex: "brins")
            val startsWithUnit = IngredientParser.units.any { unit ->
                lowerWithoutBullet.startsWith(unit.lowercase()) && 
                (lowerWithoutBullet.length == unit.length || !lowerWithoutBullet[unit.length].isLetter())
            }
            
            // Détection si la ligne ressemble à un ingrédient
            val looksLikeIngredient = qtyRegex.containsMatchIn(lineWithoutBullet.take(8)) || 
                                     commonIngredientsNoQty.any { lowerWithoutBullet.startsWith(it.lowercase()) } ||
                                     startsWithUnit

            if (matchesStepStart && !looksLikeIngredient) { currentSection = 2 }

            val ingredientSequences = OcrHelperUtils.countIngredientSequences(trimmedLine)
            if (ingredientSequences >= 2) {
                Log.d(TAG, "INGRÉDIENT (bloc compact): $trimmedLine")
                results.rawIngredientsList.addAll(OcrHelperUtils.splitCombinedIngredients(trimmedLine, commonIngredientsNoQty))
                continue
            }

            if (currentSection == 0 && looksLikeIngredient) currentSection = 1

            var lineHandled = false
            when (currentSection) {
                1 -> {
                    if (matchesStepStart && !looksLikeIngredient && trimmedLine.length > 40) {
                        currentSection = 2
                        results.rawInstructionsList.add(trimmedLine)
                        Log.d(TAG, "INSTRUCTION (bascule): $trimmedLine")
                    } else {
                        results.rawIngredientsList.addAll(OcrHelperUtils.splitCombinedIngredients(trimmedLine, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT: $trimmedLine")
                    }
                    lineHandled = true
                }
                2 -> {
                    val lastInstruction = results.rawInstructionsList.lastOrNull()
                    val isNarrativeContinuation = lastInstruction != null && 
                                                !lastInstruction.trim().endsWith(".") && 
                                                !lastInstruction.trim().endsWith("!") &&
                                                !lastInstruction.trim().endsWith("?")

                    val startsWithQty = qtyRegex.containsMatchIn(lineWithoutBullet.take(8))
                    val startsWithKnownIngredient = commonIngredientsNoQty.any { 
                        lowerWithoutBullet.startsWith(it.lowercase()) && (lineWithoutBullet.length == it.length || !lineWithoutBullet[it.length].isLetter())
                    }
                    val isStrictIngredient = startsWithQty || startsWithKnownIngredient || startsWithUnit

                    if (isStrictIngredient && !containsAction && !isNarrativeContinuation && trimmedLine.length < 45 && !matchesStepStart) {
                        results.rawIngredientsList.addAll(OcrHelperUtils.splitCombinedIngredients(trimmedLine, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT (récupération): $trimmedLine")
                    } else {
                        results.rawInstructionsList.add(trimmedLine)
                        Log.d(TAG, "INSTRUCTION: $trimmedLine")
                    }
                    lineHandled = true
                }
            }

            // E. EXCLUSION (En dernier recours seulement)
            if (!lineHandled) {
                if (OcrHelperUtils.isExcluded(trimmedLine, excludedKeywords)) {
                    Log.d(TAG, "LIGNE EXCLUE (bruit): $trimmedLine")
                    continue
                }
                
                // Par défaut, si non géré et non exclu
                if (trimmedLine.length < 45 || looksLikeIngredient) {
                    results.rawIngredientsList.addAll(OcrHelperUtils.splitCombinedIngredients(trimmedLine, commonIngredientsNoQty))
                    Log.d(TAG, "INGRÉDIENT (par défaut): $trimmedLine")
                } else {
                    results.rawInstructionsList.add(trimmedLine)
                    Log.d(TAG, "INSTRUCTION (par défaut): $trimmedLine")
                }
            }
        }
        return results
    }
}
