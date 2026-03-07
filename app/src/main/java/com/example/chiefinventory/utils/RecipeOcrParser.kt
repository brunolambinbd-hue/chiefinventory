package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R
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
        val excludedKeywords = res.getStringArray(R.array.excluded_ocr_keywords).toList()
        val commonIngredientsNoQty = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        
        val stepActionKeywords = res.getStringArray(R.array.step_action_keywords).toList()
        val stepConnectors = listOf("puis", "ensuite", "enfin", "après", "apres", "alors", "pendant", "dans")
        val extraVerbs = listOf("plongez", "retirez", "hachez", "ajoutez", "servez", "assaisonnez", "faites", "coupez", "mélangez", "préparez", "décorez", "répartissez", "passez")
        val stepStartRegex = Regex("^\\s*(?:[•\\-*]|(?:${(stepActionKeywords + stepConnectors + extraVerbs).joinToString("|")})\\b)", RegexOption.IGNORE_CASE)
        
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

        // 3. FUSION DES INGRÉDIENTS (Délégué au Merger)
        val finalIngredients = OcrMerger.mergeIngredients(
            sections.rawIngredientsList, 
            sections.rawInstructionsList, 
            res
        )

        // 4. FUSION DES INSTRUCTIONS (Délégué au Merger)
        val finalInstructions = OcrMerger.mergeInstructions(
            sections.rawInstructionsList, 
            res
        )

        return RecipeOcrResult(
            title = null,
            ingredients = finalIngredients.joinToString("\n"),
            instructions = finalInstructions.joinToString("\n"),
            wine = if (sections.detectedWineList.isNotEmpty()) sections.detectedWineList.joinToString(" ") else null,
            source = if (sections.detectedSourceList.isNotEmpty()) sections.detectedSourceList.joinToString(", ") else null,
            servings = sections.detectedServings
        )
    }
}
