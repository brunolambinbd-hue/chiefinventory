package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R
import com.google.mlkit.vision.text.Text

/**
 * Orchestrateur principal du pipeline OCR.
 * Connecte le Normalizer, Cleaner, Categorizer, Merger et Beautifier.
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
     * Exécute les 5 étapes du pipeline.
     */
    private fun executePipeline(rawLines: List<String>, res: Resources): RecipeOcrResult {

        Log.d(TAG, "[0] AVANT NORMALISATION: texte brut:\n${rawLines.joinToString("\n")}")

        // Chargement des dictionnaires XML
        val preparationModifiers = res.getStringArray(R.array.ingredient_preparation_modifiers).toList()
        val commonIngredients = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        val repairs = res.getStringArray(R.array.ocr_spelling_repairs).toList()

        // ÉTAPE 1 : Normalisation (avec dictionnaire de réparations)
        val normalizedLines = rawLines.map { Ocr1Normalizer.normalize(it, repairs) }
        Log.d(TAG, "[1] APRÈS NORMALISATION:\n${normalizedLines.joinToString("\n")}")

        // ÉTAPE 2 : Nettoyage (Suppression du bruit)
        val cleanedLines = Ocr2Cleaner.clean(normalizedLines, res)
        Log.d(TAG, "[2] APRÈS NETTOYAGE:\n${cleanedLines.joinToString("\n")}")

        // ÉTAPE 3 : Catégorisation (Tri intelligent)
        val sections = Ocr3Categorizer.categorize(cleanedLines, res)
        Log.d(TAG, "[3] CATÉGORISATION TERMINÉE")

        // ÉTAPE 4 : Fusion (Merger)
        val mergedIngredients = Ocr4Merger.mergeIngredients(sections.rawIngredientsList, preparationModifiers, commonIngredients)
        val mergedInstructions = Ocr4Merger.mergeInstructions(sections.rawInstructionsList)

        // ÉTAPE 5 : Beautification (Présentation finale à l'écran)
        val finalIngredients = Ocr5Beautifier.beautifyIngredients(mergedIngredients)
        val finalInstructions = Ocr5Beautifier.beautifyInstructions(mergedInstructions)

        Log.d(TAG, "[4-5] RÉSULTATS FINAUX (BEAUTIFIED):")
        finalIngredients.forEachIndexed { i, ing -> Log.d(TAG, "    ING[$i]: $ing") }
        finalInstructions.forEachIndexed { i, inst -> Log.d(TAG, "    INSTR[$i]: $inst") }

        val result = RecipeOcrResult(
            title = null,
            ingredients = finalIngredients.joinToString("\n"),
            instructions = finalInstructions.joinToString("\n"),
            wine = if (sections.detectedWineList.isNotEmpty()) sections.detectedWineList.joinToString(" ") else null,
            source = if (sections.detectedSourceList.isNotEmpty()) sections.detectedSourceList.joinToString(", ") else null,
            servings = sections.detectedServings,
            prepTime = sections.detectedPrepTime,
            cookTime = sections.detectedCookTime,
            restingTime = sections.detectedRestingTime,
            kcalPerServing = sections.detectedKcal,
            difficulty = sections.detectedDifficulty
        )

        // LOG FINAL DES MÉTA-DONNÉES EXTRAITES
        Log.d(TAG, "[6] MÉTA-DONNÉES EXTRAITES:")
        Log.d(TAG, "    - Portions: ${result.servings ?: "N/A"}")
        Log.d(TAG, "    - Kcal/por: ${result.kcalPerServing ?: "N/A"}")
        Log.d(TAG, "    - Difficulté: ${result.difficulty ?: "N/A"}")
        Log.d(TAG, "    - Prép (min): ${result.prepTime ?: "N/A"}")
        Log.d(TAG, "    - Cuisson (min): ${result.cookTime ?: "N/A"}")
        Log.d(TAG, "    - Repos (min): ${result.restingTime ?: "N/A"}")
        Log.d(TAG, "    - Vin: ${result.wine ?: "N/A"}")
        Log.d(TAG, "    - Source: ${result.source ?: "N/A"}")
        Log.d(TAG, "=== FIN PIPELINE OCR ===")

        return result
    }
}