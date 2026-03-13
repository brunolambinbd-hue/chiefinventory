package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log

/**
 * Orchestrateur principal du pipeline OCR.
 * Connecte le Normalizer, Cleaner, Categorizer et Merger.
 */
object RecipeOcrParser {
    private const val TAG = "RecipeOCR"

    /**
     * Point d'entrée principal pour le parsing d'une recette.
     */
    fun parse(fullText: String, res: Resources): RecipeOcrResult {
        Log.d(TAG, "--- DÉBUT PIPELINE OCR ---")
        Log.d(TAG, "TEXTE ANALYSÉ:\n$fullText")

        if (fullText.isBlank()) return RecipeOcrResult()

        // 1. Découpage initial des lignes (support du séparateur '|')
        val rawLines = fullText.replace("|", "\n|")
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // ÉTAPE 1 : Normalisation (Réparation des caractères et symboles)
        // Transforme le "bruit machine" en texte français propre.
        val normalizedLines = rawLines.map { OcrNormalizer.normalize(it) }

        // ÉTAPE 2 : Nettoyage (Suppression des publicités et du bruit numérique)
        // Utilise les listes d'exclusions XML.
        val cleanedLines = OcrCleaner.clean(normalizedLines, res)

        // ÉTAPE 3 : Catégorisation (Tri sélectif dans les compartiments)
        // Décide si une ligne est un ingrédient, une instruction, du vin, etc.
        val sections = OcrCategorizer.categorize(cleanedLines, res)

        // ÉTAPE 4 : Fusion (Reconstruction sémantique des phrases)
        // Gère les césures (tirets) et les retours à la ligne des colonnes étroites.
        val finalIngredients = OcrMerger.mergeIngredients(sections.rawIngredientsList)
        val finalInstructions = OcrMerger.mergeInstructions(sections.rawInstructionsList)

        Log.d(TAG, "--- FIN PIPELINE OCR ---")

        return RecipeOcrResult(
            title = null, // Le titre sera extrait via une logique spatiale ultérieurement
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