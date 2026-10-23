package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R

/**
 * Conteneur du résultat de nettoyage d'Étape 2.
 *
 * @property cleanedLines Liste des lignes nettoyées du bruit OCR et des publicités.
 * @property earlyDetectedSources Liste des marques/sources connues capturées au vol dans les bannières publicitaires.
 */
data class CleaningResult(
    val cleanedLines: List<String>,
    val earlyDetectedSources: List<String> = emptyList()
)

/**
 * Étape 2 du pipeline OCR : Nettoyage du texte, élimination du bruit de scan, des publicités et capture précoce des sources.
 *
 * Ordre d'Exécution des Dépollutions XML :
 * 1. Capture précoce des sources/enseignes dans les bannières publicitaires (`ocr_step2_source_brands.xml`).
 * 2. Passe 1 : Élimination du bruit de scan OCR, codes-barres et scories visuelles (`ocr_step2_noise_exclusions.xml`).
 * 3. Passe 2 : Élimination des bruits publicitaires et mentions de présentoirs (`ocr_step2_ads_exclusions.xml`).
 * 4. Passe 3 : Filtrage des numéros de pages isolés et mots-clés d'exclusion stricte (`ocr_step2_excluded_keywords.xml`).
 * 5. Protection des dimensions techniques (ex: "5 mm sur 5 mm") et consommation du cache memoized [OcrRegexes.getAdPrefixPattern].
 */
object Ocr2Cleaner {
    private const val TAG = "RecipeOCR"

    /**
     * Nettoie une liste de lignes de texte OCR en utilisant les ressources [Resources] Android pour charger les exclusions.
     *
     * @param lines La liste initiale des lignes de texte brutes normalisées.
     * @param res Les ressources de l'application Android permettant de lire `ads_exclusions.xml` et `ocr_noise_exclusions.xml`.
     * @return La liste filtrée et nettoyée des lignes de texte.
     */
    fun clean(lines: List<String>, res: Resources): List<String> {
        return cleanWithVocabulary(lines, OcrVocabulary.fromResources(res)).cleanedLines
    }

    /**
     * Nettoie une liste de lignes de texte OCR en 3 passes de dépollution structurées (utilisé par les tests unitaires TDD).
     *
     * @param lines La liste des lignes de texte brutes normalisées.
     * @param excludedKeywords Les mots-clés d'exclusion stricte à supprimer.
     * @param adExclusions Les motifs et préfixes publicitaires à filtrer.
     * @param ocrNoiseExclusions Le bruit de scan OCR et artefacts visuels à supprimer.
     * @return La liste filtrée et nettoyée des lignes de texte.
     */
    fun clean(
        lines: List<String>, 
        excludedKeywords: List<String>, 
        adExclusions: List<String> = emptyList(),
        ocrNoiseExclusions: List<String> = emptyList()
    ): List<String> {
        val vocab = OcrVocabulary(
            excludedOcrKeywords = excludedKeywords,
            adExclusions = adExclusions,
            ocrNoiseExclusions = ocrNoiseExclusions
        )
        return cleanWithVocabulary(lines, vocab).cleanedLines
    }

    /**
     * Nettoie une liste de lignes de texte OCR avec capture précoce des marques/enseignes sources de [OcrVocabulary.knownSourceBrands].
     *
     * @param lines La liste des lignes de texte brutes normalisées.
     * @param vocabulary Le vocabulaire métier OCR complet.
     * @return L'objet [CleaningResult] contenant les lignes nettoyées et les sources précoces capturées.
     */
    fun cleanWithVocabulary(
        lines: List<String>,
        vocabulary: OcrVocabulary
    ): CleaningResult {
        val cleanedLines = mutableListOf<String>()
        val earlySources = mutableListOf<String>()

        for (line in lines) {
            var trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Inspection précoce de la source dans la ligne publicitaire/marketing avant suppression
            val extractedSource = SourceParser.extractSourceFromAdLine(trimmed, vocabulary)
            if (extractedSource != null && extractedSource !in earlySources) {
                earlySources.add(extractedSource)
                Log.d(TAG, "CLEANER: Source précoce capturée depuis bannière -> $extractedSource")
            }

            // 1. PASSE 1 : Filtrage du bruit de scan OCR et artefacts d'images (ocr_noise_exclusions.xml)
            val isOcrNoise = vocabulary.ocrNoiseExclusions.any { noise ->
                noise.isNotBlank() && trimmed.equals(noise.trim(), ignoreCase = true)
            }
            if (isOcrNoise) {
                Log.d(TAG, "CLEANER: Bruit de scan OCR supprimé -> $trimmed")
                continue
            }

            // 2. PASSE 2 : Nettoyage/Suppression des préfixes et motifs publicitaires (ads_exclusions.xml)
            var isFullAdLine = false
            for (adPattern in vocabulary.adExclusions) {
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

            // 3. PASSE 3 : Filtrage des mots-clés d'exclusion stricte (ex: PAGE, SAVEUR), sauf les en-têtes de section
            val isProtectedHeader = OcrRegexes.Normalizer.PROTECTED_KEYWORDS.containsMatchIn(trimmed) || 
                    trimmed.equals("INGRÉDIENTS", ignoreCase = true) || 
                    trimmed.equals("INSTRUCTIONS", ignoreCase = true) ||
                    trimmed.equals("PRÉPARATION", ignoreCase = true)

            val isExcluded = !isProtectedHeader && vocabulary.excludedOcrKeywords.any { it.equals(trimmed, ignoreCase = true) }
            if (isExcluded) {
                Log.d(TAG, "CLEANER: Mot-clé exclu supprimé -> $trimmed")
                continue
            }

            // 4. Suppression des numéros de page isolés (bruit OCR courant)
            if (trimmed.length <= 3 && trimmed.all { it.isDigit() }) continue

            cleanedLines.add(trimmed)
        }

        return CleaningResult(cleanedLines, earlySources)
    }

    /**
     * Détermine si une ligne de texte représente une dimension technique plutôt qu'un ingrédient (ex: "5 mm sur 5 mm").
     *
     * @param line La ligne de texte à analyser.
     * @return `true` si la ligne représente une dimension technique, `false` sinon.
     */
    fun isTechnicalDimension(line: String): Boolean {
        val lower = line.lowercase()
        val isDimensionMatch = OcrRegexes.Cleaner.TECHNICAL_DIMENSION.containsMatchIn(line)
        val hasTechnicalContext = lower.contains("sur") || lower.contains("x") || lower.contains("de haut") || lower.contains("de large") || lower.contains("d'épaisseur") || lower.contains("tronçon") || lower.contains("troncon")

        return isDimensionMatch && hasTechnicalContext
    }
}
