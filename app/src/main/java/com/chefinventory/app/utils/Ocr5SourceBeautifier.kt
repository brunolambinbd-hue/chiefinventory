package com.chefinventory.app.utils

import android.util.Log

/**
 * Assistant dédié à la beautification et à la présentation finale des sources et établissements lors de l'Étape 5.
 *
 * Responsabilités :
 * - Normaliser la majusculation initiale des noms de restaurants, hôtels et enseignes (ex: "hôtel hilton" -> "Hôtel hilton").
 * - Nettoyer les ponctuations orphelines et préfixes téléphoniques parasites.
 * - Supprimer les doublons de coordonnées ou de sources identiques de manière déterministe.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr5SourceBeautifier {
    private const val TAG = "RecipeOCR"

    /**
     * Effectue la beautification et le dé-doublonnage des sources et coordonnées d'établissements.
     *
     * @param sources La liste des sources et coordonnées brutes.
     * @return La liste nettoyée, majusculée et sans doublons des sources d'origine.
     */
    fun beautifySources(sources: List<String>): List<String> {
        if (sources.isEmpty()) return emptyList()

        val beautifiedList = mutableListOf<String>()

        for (source in sources) {
            var cleaned = source.trim()
            if (cleaned.isBlank()) continue

            // Nettoyage des ponctuations orphelines en début/fin de ligne
            cleaned = cleaned.replace(OcrRegexes.Sources.LEADING_PUNCTUATION, "").trim()
            cleaned = cleaned.replace(OcrRegexes.Normalizer.TRAILING_PUNCTUATION, "").trim()
            if (cleaned.isBlank()) continue

            // Majusculation initiale
            cleaned = cleaned.replaceFirstChar { it.titlecase() }

            if (cleaned !in beautifiedList) {
                beautifiedList.add(cleaned)
                Log.d(TAG, "SOURCE BEAUTIFIER: '$source' -> '$cleaned'")
            }
        }

        return beautifiedList
    }
}
