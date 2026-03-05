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
        
        val stepStartRegex = Regex("^\\s*(?:${(stepActionKeywords + stepConnectors).joinToString("|")})\\b", RegexOption.IGNORE_CASE)
        val qtyRegex = Regex("^[|Il!\\d\\-*¼½¾]")
        val servingsRegex = Regex("(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val alternateServingsRegex = Regex("(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)", RegexOption.IGNORE_CASE)

        // 1. DÉTECTION DU TITRE
        // On cherche la première ligne qui n'est ni du bruit, ni une info de source (adresse, chef, etc.)
        var titleIndex = 0
        while (titleIndex < lines.size && (
                isExcluded(lines[titleIndex], excludedKeywords) || 
                !lines[titleIndex].any { it.isLetter() } ||
                SourceParser.isSourceLine(lines[titleIndex], sourceRes) ||
                isLikelyProperNameOrSource(lines[titleIndex])
            )) {
            titleIndex++
        }

        val rawTitle = if (titleIndex < lines.size) lines[titleIndex] else lines[0]
        val detectedTitle = cleanTitle(rawTitle)

        // 2. PARSING DES SECTIONS
        var currentSection = 0 // 0: Header, 1: Ingredients, 2: Instructions
        val rawIngredientsList = mutableListOf<String>()
        val instructionsList = mutableListOf<String>()
        val detectedWineList = mutableListOf<String>()
        val detectedSourceList = mutableListOf<String>()
        var detectedServings: String? = null

        for ((index, line) in lines.withIndex()) {
            if (index == titleIndex) continue
            
            val lowerLine = line.lowercase()

            // Portions
            val sMatch = servingsRegex.find(line) ?: alternateServingsRegex.find(line)
            if (sMatch != null) {
                if (detectedServings == null) detectedServings = sMatch.groupValues[1]
                continue
            }

            // Vin
            if (WineParser.isWineLine(line, wineRes)) {
                detectedWineList.add(WineParser.cleanWineLine(line, wineRes))
                continue
            }

            // Proper names / Source
            if (isLikelyProperNameOrSource(line) || SourceParser.isSourceLine(line, sourceRes)) {
                detectedSourceList.add(SourceParser.cleanSourceLine(line, sourceRes))
                continue
            }

            // Exclusions
            if (isExcluded(line, excludedKeywords)) {
                val upperLine = line.uppercase()
                val keywordsToSource = listOf("CONRAD", "HILTON", "SHERATON", "MARRIOTT", "CHEF", "HOTEL", "RESTAURANT")
                if (keywordsToSource.any { upperLine.contains(it) }) detectedSourceList.add(line)
                continue
            }

            // Bascules de section
            val isInstructionHeader = instructionHeaderKeywords.any { lowerLine.contains(it) }
            val isIngredientHeader = ingredientHeaderKeywords.any { lowerLine.contains(it) }
            val startsWithAction = stepStartRegex.containsMatchIn(line)
            
            if (isInstructionHeader) { currentSection = 2; continue }
            if (isIngredientHeader) { currentSection = 1; continue }
            if (startsWithAction) { currentSection = 2 }

            val hasCommonIngredient = commonIngredientsNoQty.any { lowerLine.contains(it) }
            val looksLikeIngredient = qtyRegex.containsMatchIn(line.take(5)) || hasCommonIngredient

            if (currentSection == 0 && looksLikeIngredient) currentSection = 1

            when (currentSection) {
                1 -> {
                    if (line.length > 65 && !looksLikeIngredient) {
                        currentSection = 2
                        instructionsList.add(line)
                    } else {
                        rawIngredientsList.addAll(splitCombinedIngredients(line, commonIngredientsNoQty))
                    }
                }
                2 -> {
                    if (looksLikeIngredient && line.length < 45) rawIngredientsList.add(line)
                    else instructionsList.add(line)
                }
                else -> {
                    if (line.length < 45 || looksLikeIngredient) rawIngredientsList.add(line)
                    else instructionsList.add(line)
                }
            }
        }

        // 3. FUSION ET NETTOYAGE INGRÉDIENTS
        val finalIngredients = mutableListOf<String>()
        if (rawIngredientsList.isNotEmpty()) {
            var currentIng = IngredientParser.preClean(rawIngredientsList[0])
            for (i in 1 until rawIngredientsList.size) {
                val nextLine = IngredientParser.preClean(rawIngredientsList[i])
                val nextIsNew = qtyRegex.containsMatchIn(nextLine.take(5)) || 
                               commonIngredientsNoQty.any { kw -> nextLine.lowercase().startsWith(kw) }
                val containsAction = stepStartRegex.containsMatchIn(nextLine)

                if (!nextIsNew && !containsAction && nextLine.length > 2 && nextLine.length < 35 && !nextLine.endsWith(".")) {
                    currentIng += " $nextLine"
                } else {
                    val cleaned = cleanIngredientSemantics(currentIng, excludedKeywords)
                    if (cleaned.isNotBlank()) finalIngredients.add(cleaned)
                    currentIng = nextLine
                }
            }
            val cleanedLast = cleanIngredientSemantics(currentIng, excludedKeywords)
            if (cleanedLast.isNotBlank()) finalIngredients.add(cleanedLast)
        }

        return RecipeOcrResult(
            title = detectedTitle,
            ingredients = finalIngredients.joinToString("\n"),
            instructions = instructionsList.joinToString("\n"),
            wine = if (detectedWineList.isNotEmpty()) detectedWineList.joinToString(" ") else null,
            source = if (detectedSourceList.isNotEmpty()) detectedSourceList.joinToString(", ") else null,
            servings = detectedServings
        )
    }

    private fun cleanTitle(title: String): String {
        // Enlever les numéros et les bruits (LS, etc.)
        var cleaned = title.replace(Regex("(?i)^(?:LS|SAVEUR DE|RECETTE|PAGE)\\s+"), "")
                          .replace(Regex("^\\d+[\\s.\\-]*"), "")
                          .trim()
        
        // Lettrine S manquante
        if (cleaned.lowercase().startsWith("alaade")) {
            cleaned = "S" + cleaned.substring(0)
            cleaned = cleaned.replace(Regex("(?i)^Salaade"), "Salade")
        }

        if (cleaned.isNotEmpty() && cleaned[0].isLowerCase()) {
            cleaned = cleaned.replaceFirstChar { it.uppercase() }
        }
        return cleaned.removeSuffix(".")
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

    private fun splitCombinedIngredients(line: String, commonItems: List<String>): List<String> {
        val result = mutableListOf<String>()
        val initialParts = line.split(Regex("[,;]|\\bet\\b"), 0).map { it.trim() }.filter { it.isNotBlank() }
        for (part in initialParts) {
            var currentPart = part
            var splitFound: Boolean
            do {
                splitFound = false
                for (keyword in commonItems.sortedByDescending { it.length }) {
                    val pattern = Regex("(?<=.{3})${Regex.escape(keyword)}\\b", RegexOption.IGNORE_CASE)
                    val match = pattern.find(currentPart)
                    if (match != null) {
                        result.add(currentPart.substring(0, match.range.first).trim())
                        currentPart = currentPart.substring(match.range.first).trim()
                        splitFound = true; break
                    }
                }
            } while (splitFound)
            result.add(currentPart)
        }
        return result.filter { it.isNotBlank() }.distinct()
    }

    private fun cleanIngredientSemantics(text: String, excludedKeywords: List<String>): String {
        val cleaned = text.replace(Regex("\\(.*?\\)"), "").trim()
        if (!cleaned.any { it.isLetter() } || cleaned.length <= 1) return ""
        if (isLikelyProperNameOrSource(cleaned)) return ""
        if (excludedKeywords.any { kw -> 
                val ukw = kw.uppercase()
                if (ukw.length <= 4) cleaned.uppercase() == ukw else cleaned.uppercase().contains(ukw)
            }) return ""
        return cleaned
    }
}
