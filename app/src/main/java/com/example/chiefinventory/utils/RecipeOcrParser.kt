package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R

/**
 * Orchestrateur pour le parsing OCR des recettes.
 */
object RecipeOcrParser {
    private const val TAG = "RecipeOCR"

    fun parse(fullText: String, res: Resources): RecipeOcrResult {
        Log.d(TAG, "--- DÉBUT ANALYSE OCR ---")
        
        val processedText = fullText.replace("|", "\n|")
        val lines = processedText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return RecipeOcrResult()

        val wineRes = WineParser.loadResources(res)
        val sourceRes = SourceParser.loadResources(res)
        val stepActionKeywords = res.getStringArray(R.array.step_action_keywords).toList()
        val commonIngredientsNoQty = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        val excludedKeywords = res.getStringArray(R.array.excluded_ocr_keywords).toList()
        val stepConnectors = listOf("puis", "ensuite", "enfin", "après", "apres", "alors", "pendant", "dans")

        val instructionHeaderKeywords = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression")
        val ingredientHeaderKeywords = listOf("ingrédients", "ingredients", "composition")
        
        val stepStartRegex = Regex("^\\s*(?:[•\\-*]|(?:${(stepActionKeywords + stepConnectors).joinToString("|")})\\b)", RegexOption.IGNORE_CASE)
        val containsActionRegex = Regex("\\b(?:${(stepActionKeywords + stepConnectors).joinToString("|")})\\b", RegexOption.IGNORE_CASE)
        
        val qtyRegex = Regex("^[|Il!\\d\\-*¼½¾]")
        val servingsRegex = Regex("(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val alternateServingsRegex = Regex("(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)", RegexOption.IGNORE_CASE)

        // 1. IDENTIFICATION PRUDENTE DU TITRE
        var titleIndex = -1
        for (i in lines.indices) {
            val line = lines[i]
            val lowerLine = line.lowercase()
            val isNoise = isExcluded(line, excludedKeywords) || !line.any { it.isLetter() }
            val isSource = SourceParser.isSourceLine(line, sourceRes) || isLikelyProperNameOrSource(line)
            val isIngredient = qtyRegex.containsMatchIn(line.take(5)) || commonIngredientsNoQty.any { lowerLine.contains(it) }
            val isWine = WineParser.isWineLine(line, wineRes)
            val isServings = servingsRegex.containsMatchIn(line) || alternateServingsRegex.containsMatchIn(line)

            if (!isNoise && !isSource && !isIngredient && !isWine && !isServings && line.length < 65) {
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

            // VIN (Priorité haute)
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
            val isInstructionHeader = instructionHeaderKeywords.any { line.lowercase().contains(it) }
            val isIngredientHeader = ingredientHeaderKeywords.any { line.lowercase().contains(it) }
            val startsWithAction = stepStartRegex.containsMatchIn(line)
            
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

            val looksLikeIngredient = qtyRegex.containsMatchIn(line.take(5)) || commonIngredientsNoQty.any { line.lowercase().contains(it) }
            val ingredientSequences = countIngredientSequences(line)

            if (ingredientSequences >= 2) {
                Log.d(TAG, "INGRÉDIENTS (bloc compact détecté): $line")
                rawIngredientsList.addAll(splitCombinedIngredients(line, commonIngredientsNoQty))
                continue
            }

            if (currentSection == 0 && looksLikeIngredient) currentSection = 1

            when (currentSection) {
                1 -> {
                    if (startsWithAction) {
                        currentSection = 2
                        rawInstructionsList.add(line)
                        Log.d(TAG, "INSTRUCTION (bascule action): $line")
                    } else {
                        val split = splitCombinedIngredients(line, commonIngredientsNoQty)
                        rawIngredientsList.addAll(split)
                        Log.d(TAG, "INGRÉDIENT: $line")
                    }
                }
                2 -> {
                    if (ingredientSequences >= 1 && line.length < 45 && !startsWithAction) {
                        rawIngredientsList.addAll(splitCombinedIngredients(line, commonIngredientsNoQty))
                        Log.d(TAG, "INGRÉDIENT (récupération dans instructions): $line")
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

                if (lineContainsAction && !nextIsNew && !lineIsLongList) {
                    val cleaned = cleanIngredientSemantics(currentIng, excludedKeywords)
                    if (cleaned.isNotBlank()) finalIngredients.add(cleaned)
                    for (j in i until rawIngredientsList.size) rawInstructionsList.add(rawIngredientsList[j])
                    currentIng = ""
                    break
                }

                if (!nextIsNew && nextLine.length > 2 && nextLine.length < 35 && !nextLine.endsWith(".")) {
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
                               (countIngredientSequences(nextLine) >= 1 && nextLine.length < 45) ||
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

    private fun countIngredientSequences(line: String): Int {
        val pattern = Regex("(?<!(?:ou|à|-)\\s)\\b\\d+\\s+[a-zA-Z]")
        return pattern.findAll(line).count()
    }

    private fun splitCombinedIngredients(line: String, commonItems: List<String>): List<String> {
        var cleaned = line.replace(Regex("^\\d+\\s+\\d+\\s+"), "").trim()
        val keywordsRegex = commonItems.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }
        val separatorPattern = Regex("(?<=[a-zA-Z)])\\s+(?!(?:ou|à|-)\\s+)(?=\\d+\\s+[a-zA-Z])|(?<=[a-zA-Z])\\s+(?=$keywordsRegex)")
        val marked = cleaned.replace(separatorPattern, "##SPLIT##")
        return marked.split("##SPLIT##").map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun isLikelyProperNameOrSource(line: String): Boolean {
        val trimmed = line.trim()
        val words = trimmed.split(Regex("\\s+"))
        if (words.size !in 2..4) return false
        if (trimmed.any { it.isDigit() }) return false
        return words.all { word -> word.isNotEmpty() && (word[0].isUpperCase() || word.all { it.isUpperCase() }) }
    }

    private fun isExcluded(line: String, excludedKeywords: List<String>): Boolean {
        val upperLine = line.uppercase()
        return excludedKeywords.any { kw -> 
            val ukw = kw.uppercase()
            if (ukw.length <= 4) upperLine == ukw || upperLine.startsWith("$ukw ") || upperLine.endsWith(" $ukw") 
            else upperLine.contains(ukw)
        }
    }

    private fun cleanIngredientSemantics(text: String, excludedKeywords: List<String>): String {
        val cleaned = text.replace(Regex("\\(.*?\\)"), "").trim()
        if (!cleaned.any { it.isLetter() } || cleaned.length <= 1) return ""
        if (isLikelyProperNameOrSource(cleaned)) return ""
        val upperCleaned = cleaned.uppercase()
        if (excludedKeywords.any { kw -> 
                val ukw = kw.uppercase()
                if (ukw.length <= 4) upperCleaned == ukw else upperCleaned.contains(ukw)
            }) return ""
        return cleaned
    }
}
