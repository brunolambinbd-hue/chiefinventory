package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R

/**
 * Étape 2 du pipeline OCR : Nettoyage du texte et élimination du bruit publicitaire.
 *
 * Responsabilités :
 * - Éliminer les bruits publicitaires et mentions de présentoirs (`ads_exclusions.xml`).
 * - Filtrer les numéros de pages isolés et mots-clés d'exclusion stricte (`excluded_ocr_keywords`).
 * - Identifier et protéger les dimensions techniques (ex: "5 mm sur 5 mm") pour qu'elles ne soient pas confondues avec des ingrédients.
 * - Consommer le cache memoized [OcrRegexes.getAdPrefixPattern] pour éviter la re-compilation CPU de Regexes dans les boucles.
 */
object Ocr2Cleaner {
    private const val TAG = "RecipeOCR"

    /**
     * Nettoie une liste de lignes de texte OCR en utilisant les ressources [Resources] Android pour charger les exclusions.
     *
     * @param lines La liste initiale des lignes de texte brutes normalisées.
     * @param res Les ressources de l'application Android permettant de lire `ads_exclusions.xml`.
     * @return La liste filtrée et nettoyée des lignes de texte.
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
     * Nettoie une liste de lignes de texte OCR en utilisant les collections [List] transmises (utilisé par les tests unitaires TDD).
     *
     * @param lines La liste des lignes de texte brutes normalisées.
     * @param excludedKeywords Les mots-clés d'exclusion stricte à supprimer.
     * @param adExclusions Les motifs et préfixes publicitaires à filtrer.
     * @return La liste filtrée et nettoyée des lignes de texte.
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
                    val prefixRegex = OcrRegexes.getAdPrefixPattern(rawPattern)
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

            // 2. Filtrage des mots-clés d'exclusion stricte (ex: PAGE, SAVEUR), sauf les en-têtes de section
            val isProtectedHeader = OcrRegexes.Normalizer.PROTECTED_KEYWORDS.containsMatchIn(trimmed) || 
                    trimmed.equals("INGRÉDIENTS", ignoreCase = true) || 
                    trimmed.equals("INSTRUCTIONS", ignoreCase = true) ||
                    trimmed.equals("PRÉPARATION", ignoreCase = true)

            val isExcluded = !isProtectedHeader && excludedKeywords.any { it.equals(trimmed, ignoreCase = true) }
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
     * Détermine si une ligne de texte représente une dimension technique plutôt qu'un ingrédient (ex: "5 mm sur 5 mm").
     *
     * @param line La ligne de texte à analyser.
     * @return `true` si la ligne représente une dimension technique, `false` sinon.
     */
    fun isTechnicalDimension(line: String): Boolean {
        // Si la ligne contient "sur" ou "x" entre deux dimensions (ex: 5 mm sur 5 mm)
        return OcrRegexes.Cleaner.TECHNICAL_DIMENSION.containsMatchIn(line) && 
               (line.contains("sur", ignoreCase = true) || line.contains("x", ignoreCase = true))
    }
}
