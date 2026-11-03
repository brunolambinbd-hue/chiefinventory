package com.chefinventory.app.utils

import android.util.Log

/**
 * Sous-composant du Bloc A de l'Étape 2.5 : Extraction des Kcal, Difficulté et Portions pour [RecipeMetadataParser].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object RecipeMetadataParser1_BlockA_ServingsDiff {
    private const val TAG = "RecipeMetadataParser1_BlockA"

    /**
     * Extraie les métadonnées de Kcal, Difficulté et Portions de la ligne [rawWorkingLine].
     *
     * @return La ligne nettoyée de ces éléments.
     */
    internal fun processServingsAndDifficulty(rawWorkingLine: String, line: String, results: RawSections): String {
        var workingLine = rawWorkingLine

        val kcalMatch = OcrRegexes.Metadata.KCAL.find(workingLine)
        if (kcalMatch != null) {
            if (results.detectedKcal == null) {
                results.detectedKcal = kcalMatch.groupValues[1]
            } else {
                Log.d(TAG, "  [INFO] Kcal déjà détecté (${results.detectedKcal})")
            }
            workingLine = workingLine.replace(kcalMatch.value, "").trim()
        }

        val diffMatch = OcrRegexes.Metadata.DIFFICULTY.find(workingLine)
        if (diffMatch != null) {
            if (results.detectedDifficulty == null) {
                results.detectedDifficulty = diffMatch.groupValues[1].lowercase().replace("diffcile", "difficile")
            } else {
                Log.d(TAG, "  [INFO] Difficulté déjà détectée (${results.detectedDifficulty})")
            }
            workingLine = workingLine.replace(diffMatch.value, "").trim()
        }

        val sMatch = OcrRegexes.Metadata.SERVINGS.find(workingLine)
        if (sMatch != null) {
            if (results.detectedServings == null) {
                results.detectedServings = sMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
            } else {
                Log.d(TAG, "  [INFO] Portions déjà détectées (${results.detectedServings})")
            }
            workingLine = workingLine.replace(sMatch.value, "").trim()
            val lowerLine = line.lowercase()
            val hasIngrAcc = lowerLine.contains("ingrédients")
            val hasIngrNoAcc = lowerLine.contains("ingredients")
            val hasIngrHeader = if (hasIngrAcc) true else hasIngrNoAcc

            val hasPrepAcc = lowerLine.contains("préparation")
            val hasInstrWord = lowerLine.contains("instructions")
            val hasInstrHeader = if (hasPrepAcc) true else hasInstrWord

            if (workingLine.isBlank()) {
                if (hasIngrHeader) {
                    workingLine = "ingrédients"
                } else if (hasInstrHeader) {
                    workingLine = "préparation"
                } else {
                    Log.d(TAG, "  [INFO] Portions seules sans en-tête")
                }
            }
        }

        return workingLine
    }
}
