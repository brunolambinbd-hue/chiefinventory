package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Assistant de l'Étape 5 (Embellissement) dédié à la beautification et à la présentation finale des recommandations de vins.
 *
 * Responsabilités :
 * - Normaliser la majusculation initiale des appellations et châteaux de vins (ex: "bordeaux 1996" -> "Bordeaux 1996").
 * - Nettoyer les ponctuations orphelines et les espaces superflus en début/fin de ligne.
 * - Supprimer les doublons d'appellations identiques de manière déterministe.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr5BeautifierWine {
    private const val TAG = "RecipeOCR"

    /**
     * Effectue la beautification et le dé-doublonnage des recommandations de vins.
     *
     * @param wines La liste des recommandations de vins brutes.
     * @return La liste nettoyée, majusculée et sans doublons des vins recommandés.
     */
    fun beautifyWines(wines: List<String>): List<String> {
        if (wines.isEmpty()) return emptyList()

        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val beautifiedList = mutableListOf<String>()

        for (wine in wines) {
            var cleaned = wine.trim()
            if (cleaned.isBlank()) continue

            // Nettoyage des ponctuations orphelines en début/fin de ligne
            cleaned = cleaned.replace(OcrRegexes.Wines.LEADING_PUNCTUATION, "").trim()
            cleaned = cleaned.replace(OcrRegexes.Normalizer.TRAILING_PUNCTUATION, "").trim()
            if (cleaned.isBlank()) continue

            // Majusculation initiale
            val first = cleaned[0]
            if (first.isLowerCase()) {
                cleaned = first.titlecase() + cleaned.substring(1)
            } else {
                // Le premier caractère est déjà une majuscule ou un chiffre
            }

            if (cleaned !in beautifiedList) {
                beautifiedList.add(cleaned)
                LogManager.d(TAG, String.format(vocab.logOcr5BeautifierWineFormatted, wine, cleaned))
            }
        }

        return beautifiedList
    }
}
