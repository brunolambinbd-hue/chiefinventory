package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R
import com.google.mlkit.vision.text.Text

/**
 * Orchestrateur principal du pipeline OCR.
 * Connecte le Normalizer, Cleaner, Categorizer et Merger.
 */
object RecipeOcrParser {
    private const val TAG = "RecipeOCR"

    /**
     * Point d'entrée recommandé utilisant l'objet structurel Text de ML Kit.
     */
    fun parse(visionText: Text, res: Resources): RecipeOcrResult {
        Log.d(TAG, "=== DÉBUT PIPELINE OCR (Extraction par blocs) ===")

        // Extraction hiérarchique par blocs
        val rawLines = visionText.textBlocks.flatMap { block ->
            block.lines.map { it.text }
        }

        return executePipeline(rawLines, res)
    }

    /**
     * Point d'entrée utilisant du texte brut (support legacy et tests).
     */
    fun parse(fullText: String, res: Resources): RecipeOcrResult {
        Log.d(TAG, "=== DÉBUT PIPELINE OCR (Texte brut) ===")

        if (fullText.isBlank()) return RecipeOcrResult()

        val rawLines = fullText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return executePipeline(rawLines, res)
    }

    /**
     * Exécute les 4 étapes du pipeline.
     */
    private fun executePipeline(rawLines: List<String>, res: Resources): RecipeOcrResult {
        // Chargement des dictionnaires XML
        val preparationModifiers = res.getStringArray(R.array.ingredient_preparation_modifiers).toList()
        val repairs = res.getStringArray(R.array.ocr_spelling_repairs).toList()

        // ÉTAPE 1 : Normalisation (avec dictionnaire de réparations)
        val normalizedLines = rawLines.map { Ocr1Normalizer.normalize(it, repairs) }
        Log.d(TAG, "[1] APRÈS NORMALISATION:\n${normalizedLines.joinToString("\n")}")

        // ÉTAPE 2 : Nettoyage
        val cleanedLines = Ocr2Cleaner.clean(normalizedLines, res)
        Log.d(TAG, "[2] APRÈS NETTOYAGE:\n${cleanedLines.joinToString("\n")}")

        // ÉTAPE 3 : Catégorisation
        val sections = Ocr3Categorizer.categorize(cleanedLines, res)
        Log.d(TAG, "[3] CATÉGORISATION TERMINÉE")

        // ÉTAPE 4 : Fusion (Merger)
        val finalIngredients = Ocr4Merger.mergeIngredients(sections.rawIngredientsList, preparationModifiers)
        val finalInstructions = Ocr4Merger.mergeInstructions(sections.rawInstructionsList)

        Log.d(TAG, "[4] RÉSULTATS FINAUX (MERGER):")
        finalIngredients.forEachIndexed { i, ing -> Log.d(TAG, "    ING[$i]: $ing") }
        finalInstructions.forEachIndexed { i, inst -> Log.d(TAG, "    INSTR[$i]: $inst") }

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