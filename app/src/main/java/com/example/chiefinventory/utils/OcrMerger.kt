package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Gère la fusion finale des lignes OCR pour reconstituer des blocs cohérents.
 */
object OcrMerger {

    /**
     * Fusionne les lignes d'ingrédients brutes en une liste d'ingrédients propres.
     */
    internal fun mergeIngredients(
        rawIngredientsList: List<String>,
        rawInstructionsList: MutableList<String>,
        res: Resources
    ): List<String> {
        if (rawIngredientsList.isEmpty()) return emptyList()

        val commonIngredientsNoQty = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        val excludedKeywords = res.getStringArray(R.array.excluded_ocr_keywords).toList()
        val stepActionKeywords = res.getStringArray(R.array.step_action_keywords).toList()
        val stepConnectors = listOf("puis", "ensuite", "enfin", "après", "apres", "alors", "pendant", "dans")
        val extraVerbs = listOf("plongez", "retirez", "hachez", "ajoutez", "servez", "assaisonnez", "faites", "coupez", "mélangez", "préparez", "décorez", "répartissez", "passez")
        
        val qtyRegex = Regex("^[|Il!\\d\\-*¼½¾]")
        val containsActionRegex = Regex("\\b(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b", RegexOption.IGNORE_CASE)

        val finalIngredients = mutableListOf<String>()
        var currentIng = IngredientParser.preClean(rawIngredientsList[0])

        for (i in 1 until rawIngredientsList.size) {
            val nextLine = IngredientParser.preClean(rawIngredientsList[i])
            val lowerNext = nextLine.lowercase()
            
            // On vérifie si la ligne précédente se terminait par un connecteur (de, d', et...)
            // Cela indique une suite obligatoire (ex: "soupe de" -> "mayonnaise")
            val connectors = listOf("de", "du", "des", "d'", "et", "ou", "à")
            val currentLower = currentIng.lowercase().trim()
            val isBrokenSyntax = connectors.any { currentLower.endsWith(" $it") || currentLower.endsWith("$it") }

            val nextIsNew = qtyRegex.containsMatchIn(nextLine.take(5)) || 
                           commonIngredientsNoQty.any { kw -> lowerNext.startsWith(kw) }
            
            val lineContainsAction = containsActionRegex.containsMatchIn(nextLine)
            val lineIsLongList = OcrHelperUtils.countIngredientSequences(nextLine) >= 2

            // On fusionne si :
            // 1. Ce n'est pas un nouvel ingrédient fort (quantité)
            // 2. OU si la syntaxe de la ligne précédente appelait une suite (isBrokenSyntax)
            val shouldMerge = !qtyRegex.containsMatchIn(nextLine.take(5)) && (isBrokenSyntax || !nextIsNew)

            if (lineContainsAction && !nextIsNew && !lineIsLongList && nextLine.length > 50) {
                val cleaned = OcrHelperUtils.cleanIngredientSemantics(currentIng, excludedKeywords)
                if (cleaned.isNotBlank()) finalIngredients.add(cleaned)
                rawInstructionsList.add(rawIngredientsList[i])
                currentIng = ""
            } else if (shouldMerge && nextLine.length > 2 && nextLine.length < 35 && !nextLine.endsWith(".")) {
                currentIng += " $nextLine"
            } else {
                val cleaned = OcrHelperUtils.cleanIngredientSemantics(currentIng, excludedKeywords)
                if (cleaned.isNotBlank()) finalIngredients.add(cleaned)
                currentIng = nextLine
            }
        }
        
        if (currentIng.isNotBlank()) {
            val cleanedLast = OcrHelperUtils.cleanIngredientSemantics(currentIng, excludedKeywords)
            if (cleanedLast.isNotBlank()) finalIngredients.add(cleanedLast)
        }
        
        return finalIngredients
    }

    /**
     * Fusionne les lignes d'instructions brutes en étapes cohérentes.
     */
    internal fun mergeInstructions(
        rawInstructionsList: List<String>,
        res: Resources
    ): List<String> {
        if (rawInstructionsList.isEmpty()) return emptyList()

        val wineRes = WineParser.loadResources(res)
        val stepActionKeywords = res.getStringArray(R.array.step_action_keywords).toList()
        val stepConnectors = listOf("puis", "ensuite", "enfin", "après", "apres", "alors", "pendant", "dans")
        val extraVerbs = listOf("plongez", "retirez", "hachez", "ajoutez", "servez", "assaisonnez", "faites", "coupez", "mélangez", "préparez", "décorez", "répartissez", "passez")
        val instructionHeaderKeywords = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression")
        
        val stepStartRegex = Regex("^\\s*(?:[•\\-*]|(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b)", RegexOption.IGNORE_CASE)

        val finalInstructions = mutableListOf<String>()
        var currentStep = IngredientParser.preClean(rawInstructionsList[0])

        for (i in 1 until rawInstructionsList.size) {
            val nextLine = IngredientParser.preClean(rawInstructionsList[i])
            val isNewStep = stepStartRegex.containsMatchIn(nextLine) || 
                           instructionHeaderKeywords.any { nextLine.lowercase().contains(it) } ||
                           WineParser.isWineLine(nextLine, wineRes)
            
            if (!isNewStep && nextLine.isNotBlank() && !currentStep.endsWith(".")) {
                currentStep += " $nextLine"
            } else {
                finalInstructions.add(currentStep)
                currentStep = nextLine
            }
        }
        
        if (currentStep.isNotBlank()) finalInstructions.add(currentStep)
        
        return finalInstructions
    }
}
