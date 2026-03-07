package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R
import com.example.chiefinventory.utils.OcrHelperUtils.cleanIngredientSemantics
import com.example.chiefinventory.utils.OcrHelperUtils.countIngredientSequences
import com.example.chiefinventory.utils.OcrHelperUtils.isExcluded
import com.example.chiefinventory.utils.OcrHelperUtils.isLikelyProperNameOrSource
import com.example.chiefinventory.utils.OcrHelperUtils.splitCombinedIngredients

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

        // 2. PARSING DES SECTIONS
        var currentSection = 0
        val rawIngredientsList = mutableListOf<String>()
        val rawInstructionsList = mutableListOf<String>()
        val detectedWineList = mutableListOf<String>()
        val detectedSourceList = mutableListOf<String>()
        var detectedServings: String? = null

        for ((index, line) in lines.withIndex()) {
            if (index == titleIndex) continue
            
            val lowerLine = line.lowercase()

            // PORTIONS
            val sMatch = servingsRegex.find(line) ?: alternateServingsRegex.find(line)
            if (sMatch != null) {
                if (detectedServings == null) detectedServings = sMatch.groupValues[1]
                Log.d(TAG, "PERS détecté: $detectedServings")
                continue
            }

            // VIN
            if (WineParser.isWineLine(line, wineRes)) {
                val cleanedWine = WineParser.cleanWineLine(line, wineRes)
                detectedWineList.add(cleanedWine)
                Log.d(TAG, "WINE détecté: $cleanedWine")
                continue
            }

            // SOURCE
            if (isLikelyProperNameOrSource(line) || SourceParser.isSourceLine(line, sourceRes)) {
                val cleanedSource = SourceParser.cleanSourceLine(line, sourceRes)
                detectedSourceList.add(cleanedSource)
                Log.d(TAG, "SOURCE détectée: $cleanedSource")
                continue
            }

            // EXCLUSIONS
            if (isExcluded(line, excludedKeywords)) {
                val upperLine = line.uppercase()
                val keywordsToSource = listOf("CONRAD", "HILTON", "SHERATON", "MARRIOTT", "CHEF", "HOTEL", "RESTAURANT")
                if (keywordsToSource.any { upperLine.contains(it) }) {
                    detectedSourceList.add(line)
                    Log.d(TAG, "SOURCE (Exclusion redirection): $line")
                }
                continue
            }

            // BASCULES ET REMPLISSAGE
            val isInstructionHeader = instructionHeaderKeywords.any { lowerLine.contains(it) }
            val isIngredientHeader = ingredientHeaderKeywords.any { lowerLine.contains(it) }
            val startsWithAction = stepStartRegex.containsMatchIn(line)

            if (isInstructionHeader) { currentSection = 2; Log.d(TAG, "Section INSTRUCTIONS détectée"); continue }
            if (isIngredientHeader) { currentSection = 1; Log.d(TAG, "Section INGRÉDIENTS détectée"); continue }

            if (startsWithAction) { currentSection = 2 }

            val looksLikeIngredient = qtyRegex.containsMatchIn(line.take(5)) || commonIngredientsNoQty.any { lowerLine.contains(it) }
            val ingredientSequences = countIngredientSequences(line)

            // Priorité ingrédients compacts
            if (ingredientSequences >= 2) {
                Log.d(TAG, "INGRÉDIENT (bloc compact détecté): $line")
                rawIngredientsList.addAll(splitCombinedIngredients(line, commonIngredientsNoQty))
                continue
            }

            if (currentSection == 0 && looksLikeIngredient) currentSection = 1

            when (currentSection) {
                1 -> {
                    if (startsWithAction && line.length > 40) {
                        currentSection = 2
                        rawInstructionsList.add(line)
                        Log.d(TAG, "INSTRUCTION (bascule action): $line")
                    } else {
                        rawIngredientsList.addAll(splitCombinedIngredients(line, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT: $line")
                    }
                }
                2 -> {
                    // Correction : On repasse en ingrédients si on voit une quantité et pas de verbe d'action
                    if (looksLikeIngredient && line.length < 45 && !startsWithAction) {
                        rawIngredientsList.addAll(splitCombinedIngredients(line, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT (récupération): $line")
                    } else {
                        rawInstructionsList.add(line)
                        Log.d(TAG, "INSTRUCTION: $line")
                    }
                }
                else -> {
                    if (line.length < 45 || looksLikeIngredient) {
                        rawIngredientsList.addAll(splitCombinedIngredients(line, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT (par défaut): $line")
                    } else {
                        rawInstructionsList.add(line)
                        Log.d(TAG, "INSTRUCTION (par défaut): $line")
                    }
                }
            }
        }

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
