package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R

/**
 * Step 2 of the OCR pipeline: Text cleaning and noise removal.
 * Responsibility: Remove advertisements, page numbers, and protect technical dimensions.
 */
object Ocr2Cleaner {
    private const val TAG = "RecipeOCR"

    // Regex pour détecter les dimensions techniques (ex: 5 mm sur 5 mm)
    // On les identifie pour éviter qu'elles ne soient traitées comme des ingrédients (5 g)
    private val DIMENSION_REGEX = Regex("\\d+\\s*(?:mm|cm|cm2|mm2)\\b", RegexOption.IGNORE_CASE)

    /**
     * Cleans a list of lines by removing advertisements and unwanted patterns.
     */
    fun clean(lines: List<String>, res: Resources): List<String> {
        val adExclusions = try {
            res.getStringArray(R.array.advertisement_exclusions).toList()
        } catch (e: Exception) {
            emptyList()
        }

        val excludedKeywords = try {
            res.getStringArray(R.array.excluded_ocr_keywords).toList()
        } catch (e: Exception) {
            emptyList()
        }

        return lines.filter { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@filter false

            // 1. Filtrage publicitaire (Regex dynamiques)
            val isAd = adExclusions.any { adPattern ->
                try {
                    Regex(adPattern, RegexOption.IGNORE_CASE).containsMatchIn(trimmed)
                } catch (e: Exception) {
                    trimmed.contains(adPattern, ignoreCase = true)
                }
            }
            if (isAd) {
                Log.d(TAG, "CLEANER: Ligne publicitaire supprimée -> $trimmed")
                return@filter false
            }

            // 2. Filtrage des mots-clés d'exclusion stricte (ex: PAGE, SAVEUR)
            val isExcluded = excludedKeywords.any { it.equals(trimmed, ignoreCase = true) }
            if (isExcluded) {
                Log.d(TAG, "CLEANER: Mot-clé exclu supprimé -> $trimmed")
                return@filter false
            }

            // 3. Suppression des numéros de page isolés ou dates (bruit OCR courant)
            if (trimmed.length <= 3 && trimmed.all { it.isDigit() }) return@filter false

            true
        }
    }

    /**
     * Identifies if a line is a technical dimension rather than an ingredient.
     */
    fun isTechnicalDimension(line: String): Boolean {
        // Si la ligne contient "sur" ou "x" entre deux dimensions (ex: 5 mm sur 5 mm)
        return DIMENSION_REGEX.containsMatchIn(line) && 
               (line.contains("sur", ignoreCase = true) || line.contains("x", ignoreCase = true))
    }
}
