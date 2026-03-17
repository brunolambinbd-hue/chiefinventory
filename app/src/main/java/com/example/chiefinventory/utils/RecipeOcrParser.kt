package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.google.mlkit.vision.text.Text

/**
 * Orchestrateur principal du pipeline OCR.
 * Connecte le Normalizer, Cleaner, Categorizer et Merger.
 */
object RecipeOcrParser {
    private const val TAG = "RecipeOCR"

    /**
     * Point d'entrée recommandé utilisant l'objet structurel Text de ML Kit.
     * Permet de respecter l'ordre des blocs (colonnes) de la recette.
     */
    fun parse(visionText: Text, res: Resources): RecipeOcrResult {
        Log.d(TAG, "=== DÉBUT PIPELINE OCR (Extraction par blocs) ===")
        Log.d(TAG, "[0] TEXTE DE DÉPART (ML KIT):\n${visionText.text}")

        // Extraction hiérarchique : on traite chaque bloc entièrement avant de passer au suivant
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
        Log.d(TAG, "[0] TEXTE DE DÉPART (BRUT):\n$fullText")
        
        if (fullText.isBlank()) return RecipeOcrResult()

        val rawLines = fullText.replace("|", "\n|")
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return executePipeline(rawLines, res)
    }

    /**
     * Exécute les 4 étapes du pipeline sur une liste de lignes brutes.
     */
    private fun executePipeline(rawLines: List<String>, res: Resources): RecipeOcrResult {
        // ÉTAPE 1 : Normalisation (Réparation des caractères et symboles)
        val normalizedLines = rawLines.map { Ocr1Normalizer.normalize(it) }
        Log.d(TAG, "[1] APRÈS NORMALISATION:\n${normalizedLines.joinToString("\n")}")

        // ÉTAPE 2 : Nettoyage (Suppression des publicités et du bruit numérique)
        val cleanedLines = Ocr2Cleaner.clean(normalizedLines, res)
        Log.d(TAG, "[2] APRÈS NETTOYAGE (Sans pub/bruit):\n${cleanedLines.joinToString("\n")}")

        // ÉTAPE 3 : Catégorisation (Tri sélectif dans les compartiments)
        val sections = Ocr3Categorizer.categorize(cleanedLines, res)
        Log.d(TAG, "[3] CATÉGORISATION TERMINÉE")
        
        // LOG DES LIGNES BRUTES APRÈS TRI
        Log.d(TAG, "    --- INGRÉDIENTS BRUTS (Catégorisés) ---")
        sections.rawIngredientsList.forEachIndexed { i, line -> Log.d(TAG, "    [$i] $line") }
        Log.d(TAG, "    --- INSTRUCTIONS BRUTS (Catégorisées) ---")
        sections.rawInstructionsList.forEachIndexed { i, line -> Log.d(TAG, "    [$i] $line") }

        // ÉTAPE 4 : Fusion (Reconstruction sémantique des phrases)
        val finalIngredients = Ocr4Merger.mergeIngredients(sections.rawIngredientsList)
        val finalInstructions = Ocr4Merger.mergeInstructions(sections.rawInstructionsList)

        // LOG LIGNE PAR LIGNE DES RÉSULTATS FUSIONNÉS
        Log.d(TAG, "[4] RÉSULTATS APRÈS FUSION (MERGER):")
        Log.d(TAG, "    --- INGRÉDIENTS FINAUX ---")
        finalIngredients.forEachIndexed { i, ing -> Log.d(TAG, "    [$i] $ing") }
        
        Log.d(TAG, "    --- INSTRUCTIONS FINALES ---")
        finalInstructions.forEachIndexed { i, inst -> Log.d(TAG, "    [$i] $inst") }

        val result = RecipeOcrResult(
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

        // LOG FINAL DES MÉTA-DONNÉES
        Log.d(TAG, "[5] MÉTA-DONNÉES EXTRAITES:")
        Log.d(TAG, "    - Portions: ${result.servings ?: "N/A"}")
        Log.d(TAG, "    - Prép (min): ${result.prepTime ?: "N/A"}")
        Log.d(TAG, "    - Cuisson (min): ${result.cookTime ?: "N/A"}")
        Log.d(TAG, "    - Repos (min): ${result.restingTime ?: "N/A"}")
        Log.d(TAG, "    - Vin: ${result.wine ?: "N/A"}")
        Log.d(TAG, "    - Source: ${result.source ?: "N/A"}")
        Log.d(TAG, "=== FIN PIPELINE OCR ===")

        return result
    }
}
