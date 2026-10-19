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
     * Cleans a list of lines by removing advertisements and unwanted patterns using [Resources].
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

        return clean(lines, excludedKeywords, adExclusions)
    }

    /**
     * Cleans a list of lines by removing advertisements and unwanted patterns using pure [List] collections.
     */
    fun clean(lines: List<String>, excludedKeywords: List<String>, adExclusions: List<String> = emptyList()): List<String> {
        val cleanedLines = mutableListOf<String>()

        for (line in lines) {
            var trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // 1. Nettoyage/Suppression des préfixes et motifs publicitaires
            var isFullAdLine = false
            for (adPattern in adExclusions) {
                if (adPattern.isBlank()) continue

                val rawPattern = adPattern.removePrefix(".*").removeSuffix(".*").trim()
                if (rawPattern.isBlank()) continue

                try {
                    val prefixRegex = Regex("(?i)^.*?" + rawPattern + "\\s*")
                    if (prefixRegex.containsMatchIn(trimmed)) {
                        val remaining = prefixRegex.replace(trimmed, "").trim()

                        if (remaining.isEmpty() || remaining.length <= 8) {
                            isFullAdLine = true
                            Log.d(TAG, "CLEANER: Ligne publicitaire supprimée -> $trimmed")
                            break
                        } else {
                            Log.d(TAG, "CLEANER: Préfixe publicitaire effacé de -> $trimmed")
                            trimmed = remaining
                        }
                    }
                } catch (_: Exception) {
                    if (trimmed.contains(rawPattern, ignoreCase = true)) {
                        isFullAdLine = true
                        break
                    }
                }
            }

            if (isFullAdLine || trimmed.isEmpty()) continue

            // 2. Filtrage des mots-clés d'exclusion stricte (ex: PAGE, SAVEUR)
            val isExcluded = excludedKeywords.any { it.equals(trimmed, ignoreCase = true) }
            if (isExcluded) {
                Log.d(TAG, "CLEANER: Mot-clé exclu supprimé -> $trimmed")
                continue
            }

            // 3. Suppression des numéros de page isolés ou dates (bruit OCR courant)
            if (trimmed.length <= 3 && trimmed.all { it.isDigit() }) continue

            cleanedLines.add(trimmed)
        }

        return cleanedLines
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
