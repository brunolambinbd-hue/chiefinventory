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
        titleIndex: Int,
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
        val extraVerbs = listOf("plongez", "retirez", "hachez", "ajoutez", "servez", "assaisonnez", "faites", "coupez", "mélangez", "préparez", "décorez", "répartissez", "passez")

        val instructionHeaderKeywords = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression")
        val ingredientHeaderKeywords = listOf("ingrédients", "ingredients", "composition")

        val stepStartRegex = Regex("^\\s*(?:[•\\-*]|(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b)", RegexOption.IGNORE_CASE)
        val containsActionRegex = Regex("\\b(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b", RegexOption.IGNORE_CASE)
        val qtyRegex = Regex("^[|Il!\\d\\-*¼½¾]")
        val servingsRegex = Regex("(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val alternateServingsRegex = Regex("(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)", RegexOption.IGNORE_CASE)

        for ((index, line) in lines.withIndex()) {
            if (index == titleIndex) continue

            val lowerLine = line.lowercase().trim()

            // A. PORTIONS
            val sMatch = servingsRegex.find(line) ?: alternateServingsRegex.find(line)
            if (sMatch != null) {
                if (results.detectedServings == null) results.detectedServings = sMatch.groupValues[1]
                Log.d(TAG, "PERS détecté: ${results.detectedServings}")
                continue
            }

            // B. VIN
            if (WineParser.isWineLine(line, wineRes)) {
                val cleanedWine = WineParser.cleanWineLine(line, wineRes)
                results.detectedWineList.add(cleanedWine)
                Log.d(TAG, "WINE détecté: $cleanedWine")
                continue
            }

            // C. SOURCE
            if (OcrHelperUtils.isLikelyProperNameOrSource(line) || SourceParser.isSourceLine(line, sourceRes)) {
                val cleanedSource = SourceParser.cleanSourceLine(line, sourceRes)
                results.detectedSourceList.add(cleanedSource)
                Log.d(TAG, "SOURCE détectée: $cleanedSource")
                continue
            }

            // D. EXCLUSIONS
            if (OcrHelperUtils.isExcluded(line, excludedKeywords)) {
                val upperLine = line.uppercase()
                val keywordsToSource = listOf("CONRAD", "HILTON", "SHERATON", "MARRIOTT", "CHEF", "HOTEL", "RESTAURANT")
                if (keywordsToSource.any { upperLine.contains(it) }) {
                    results.detectedSourceList.add(line)
                    Log.d(TAG, "SOURCE (Exclusion redirection): $line")
                }
                continue
            }

            // E. BASCULES ET REMPLISSAGE
            val startsWithAction = stepStartRegex.containsMatchIn(line)
            val containsAction = containsActionRegex.containsMatchIn(line)
            
            // Un Header doit être court et NE PAS contenir d'action narrative
            val isInstructionHeader = instructionHeaderKeywords.any { lowerLine.contains(it) } && 
                                     line.length < 25 && !containsAction
            val isIngredientHeader = ingredientHeaderKeywords.any { lowerLine.contains(it) } && 
                                    line.length < 25 && !containsAction

            if (isInstructionHeader) { 
                currentSection = 2 
                Log.d(TAG, "Section INSTRUCTIONS détectée")
                continue 
            }
            if (isIngredientHeader) { 
                currentSection = 1 
                Log.d(TAG, "Section INGRÉDIENTS détectée")
                continue 
            }

            if (startsWithAction) { currentSection = 2 }

            val looksLikeIngredient = qtyRegex.containsMatchIn(line.take(5)) || commonIngredientsNoQty.any { lowerLine.contains(it) }
            val ingredientSequences = OcrHelperUtils.countIngredientSequences(line)

            if (ingredientSequences >= 2) {
                Log.d(TAG, "INGRÉDIENT (bloc compact détecté): $line")
                results.rawIngredientsList.addAll(OcrHelperUtils.splitCombinedIngredients(line, commonIngredientsNoQty))
                continue
            }

            if (currentSection == 0 && looksLikeIngredient) currentSection = 1

            when (currentSection) {
                1 -> {
                    if (startsWithAction && line.length > 40) {
                        currentSection = 2
                        results.rawInstructionsList.add(line)
                        Log.d(TAG, "INSTRUCTION (bascule action): $line")
                    } else {
                        results.rawIngredientsList.addAll(OcrHelperUtils.splitCombinedIngredients(line, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT: $line")
                    }
                }
                2 -> {
                    val lastInstruction = results.rawInstructionsList.lastOrNull()
                    val isNarrativeContinuation = lastInstruction != null && 
                                                !lastInstruction.trim().endsWith(".") && 
                                                !lastInstruction.trim().endsWith("!") &&
                                                !lastInstruction.trim().endsWith("?")

                    val startsWithQty = qtyRegex.containsMatchIn(line.take(5))
                    val startsWithKnownIngredient = commonIngredientsNoQty.any { 
                        line.lowercase().startsWith(it.lowercase()) && 
                        (line.length == it.length || !line[it.length].isLetter())
                    }
                    val isStrictIngredient = startsWithQty || startsWithKnownIngredient

                    val shouldRecover = isStrictIngredient && !containsAction && !isNarrativeContinuation && 
                                       line.length < 45 && !startsWithAction

                    if (shouldRecover) {
                        results.rawIngredientsList.addAll(OcrHelperUtils.splitCombinedIngredients(line, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT (récupération): $line")
                    } else {
                        results.rawInstructionsList.add(line)
                        Log.d(TAG, "INSTRUCTION: $line")
                    }
                }
                else -> {
                    if (line.length < 45 || looksLikeIngredient) {
                        results.rawIngredientsList.addAll(OcrHelperUtils.splitCombinedIngredients(line, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT (par défaut): $line")
                    } else {
                        results.rawInstructionsList.add(line)
                        Log.d(TAG, "INSTRUCTION (par défaut): $line")
                    }
                }
            }
        }
        return results
    }
}
