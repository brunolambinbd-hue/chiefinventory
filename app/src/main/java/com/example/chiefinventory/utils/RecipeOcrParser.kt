package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R
import com.example.chiefinventory.utils.OcrHelperUtils.cleanIngredientSemantics
import com.example.chiefinventory.utils.OcrHelperUtils.countIngredientSequences
import com.example.chiefinventory.utils.OcrHelperUtils.isExcluded
import com.example.chiefinventory.utils.OcrHelperUtils.isLikelyProperNameOrSource


/**
 * Orchestrateur pour le parsing OCR des recettes.
 */
object RecipeOcrParser {
    private const val TAG = "RecipeOCR"

    fun parse(fullText: String, res: Resources): RecipeOcrResult {
        Log.d(TAG, "--- DÉBUT ANALYSE OCR ---")
        Log.d(TAG, "OCR TEXT ANALYZED:\n$fullText")
        
        val processedText = fullText.replace("|", "\n|")
        val lines = processedText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return RecipeOcrResult()

        val wineRes = WineParser.loadResources(res)
        val sourceRes = SourceParser.loadResources(res)
        val stepActionKeywords = res.getStringArray(R.array.step_action_keywords).toList()
        val commonIngredientsNoQty = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        val excludedKeywords = res.getStringArray(R.array.excluded_ocr_keywords).toList()
        val stepConnectors = listOf("puis", "ensuite", "enfin", "après", "apres", "alors", "pendant", "dans")
        val extraVerbs = listOf("plongez", "retirez", "hachez", "ajoutez", "servez", "assaisonnez", "faites", "coupez", "mélangez", "préparez")

        val instructionHeaderKeywords = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression")
        val ingredientHeaderKeywords = listOf("ingrédients", "ingredients", "composition")
        
        val stepStartRegex = Regex("^\\s*(?:[•\\-*]|(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b)", RegexOption.IGNORE_CASE)
        val containsActionRegex = Regex("\\b(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b", RegexOption.IGNORE_CASE)
        
        val qtyRegex = Regex("^[|Il!\\d\\-*¼½¾]")
        val servingsRegex = Regex("(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val alternateServingsRegex = Regex("(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)", RegexOption.IGNORE_CASE)

        // 1. IDENTIFICATION DU TITRE
        var titleIndex = -1
        for (i in lines.indices) {
            val line = lines[i]
            val lowerLine = line.lowercase()
            val isNoise = isExcluded(line, excludedKeywords) || !line.any { it.isLetter() }
            val isSource = SourceParser.isSourceLine(line, sourceRes) || isLikelyProperNameOrSource(line)
            val isIngredient = qtyRegex.containsMatchIn(line.take(5)) || commonIngredientsNoQty.any { lowerLine.contains(it) }
            val isWine = WineParser.isWineLine(line, wineRes)
            val isServings = servingsRegex.containsMatchIn(line) || alternateServingsRegex.containsMatchIn(line)
            val isInstruction = stepStartRegex.containsMatchIn(line)

            if (!isNoise && !isSource && !isIngredient && !isWine && !isServings && !isInstruction && line.length in 4..64) {
                titleIndex = i
                Log.d(TAG, "TITRE potentiel détecté: $line")
                break
            }
        }

// 2. PARSING DES SECTIONS (Délégué au Categorizer)
        val sections = OcrCategorizer.categorizeLines(lines, titleIndex, res)

// Récupération des résultats pour les étapes de fusion suivantes
        val rawIngredientsList = sections.rawIngredientsList
        val rawInstructionsList = sections.rawInstructionsList
        val detectedWineList = sections.detectedWineList
        val detectedSourceList = sections.detectedSourceList
        val detectedServings = sections.detectedServings


        // 3. FUSION ET NETTOYAGE FINAL (Ingrédients)
        val finalIngredients = mutableListOf<String>()
        if (rawIngredientsList.isNotEmpty()) {
            var currentIng = IngredientParser.preClean(rawIngredientsList[0])
            for (i in 1 until rawIngredientsList.size) {
                val nextLine = IngredientParser.preClean(rawIngredientsList[i])
                val nextIsNew = qtyRegex.containsMatchIn(nextLine.take(5)) || 
                               commonIngredientsNoQty.any { kw -> nextLine.lowercase().startsWith(kw) }
                val lineContainsAction = containsActionRegex.containsMatchIn(nextLine)
                val lineIsLongList = countIngredientSequences(nextLine) >= 2

                if (lineContainsAction && !nextIsNew && !lineIsLongList && nextLine.length > 50) {
                    val cleaned = cleanIngredientSemantics(currentIng, excludedKeywords)
                    if (cleaned.isNotBlank()) finalIngredients.add(cleaned)
                    rawInstructionsList.add(rawIngredientsList[i])
                    currentIng = ""
                } else if (!nextIsNew && nextLine.length > 2 && nextLine.length < 35 && !nextLine.endsWith(".")) {
                    currentIng += " $nextLine"
                } else {
                    val cleaned = cleanIngredientSemantics(currentIng, excludedKeywords)
                    if (cleaned.isNotBlank()) finalIngredients.add(cleaned)
                    currentIng = nextLine
                }
            }
            if (currentIng.isNotBlank()) {
                val cleanedLast = cleanIngredientSemantics(currentIng, excludedKeywords)
                if (cleanedLast.isNotBlank()) finalIngredients.add(cleanedLast)
            }
        }

        // 4. FUSION INTELLIGENTE DES INSTRUCTIONS
        val finalInstructions = mutableListOf<String>()
        if (rawInstructionsList.isNotEmpty()) {
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
        }

        return RecipeOcrResult(
            title = null,
            ingredients = finalIngredients.joinToString("\n"),
            instructions = finalInstructions.joinToString("\n"),
            wine = if (detectedWineList.isNotEmpty()) detectedWineList.joinToString(" ") else null,
            source = if (detectedSourceList.isNotEmpty()) detectedSourceList.joinToString(", ") else null,
            servings = detectedServings
        )
    }

}
