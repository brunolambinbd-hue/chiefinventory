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
        Log.d(TAG, "OCR TEXT ANALYZED:\n$fullText")
        
        val processedText = fullText.replace("|", "\n|")
        var lines = processedText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return RecipeOcrResult()

        // --- FILTRAGE PUBLICITAIRE EN AMONT ---
        val adExclusions = try {
            res.getStringArray(R.array.advertisement_exclusions).toList()
        } catch (e: Exception) {
            emptyList()
        }

        if (adExclusions.isNotEmpty()) {
            lines = lines.filter { line ->
                val isAd = adExclusions.any { adPattern ->
                    try {
                        Regex(adPattern, RegexOption.IGNORE_CASE).containsMatchIn(line)
                    } catch (e: Exception) {
                        line.contains(adPattern, ignoreCase = true)
                    }
                }
                if (isAd) Log.d(TAG, "LIGNE PUBLICITAIRE SUPPRIMÉE: $line")
                !isAd
            }
        }

        // 1. IDENTIFICATION DU TITRE - Supprimée pour éviter la capture de mots orphelins
        val titleIndex = -1

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
            servings = sections.detectedServings,
            prepTime = sections.detectedPrepTime,
            cookTime = sections.detectedCookTime,
            restingTime = sections.detectedRestingTime
        )
    }
}
