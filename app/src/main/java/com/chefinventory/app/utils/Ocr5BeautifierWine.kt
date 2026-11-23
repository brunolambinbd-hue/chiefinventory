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
        val standaloneTrashWords = (vocab.standaloneArticles + vocab.hangingConnectors).map { it.lowercase().trim() }.toSet()
        val beautifiedList = mutableListOf<String>()

        for (wine in wines) {
            var cleaned = Ocr5Beautifier.sanitizeLine(
                line = wine,
                prefixPatterns = listOf(OcrRegexes.Wines.LEADING_PUNCTUATION),
                vocab = vocab,
                standaloneTrashWords = standaloneTrashWords
            ) ?: continue

            // Nettoyage des ponctuations orphelines en fin de ligne
            cleaned = cleaned.replace(OcrRegexes.Normalizer.TRAILING_PUNCTUATION, "").trim()
            if (cleaned.isBlank()) continue

            // Majusculation initiale
            val first = cleaned[0]
            if (first.isLowerCase()) {
                cleaned = first.titlecase() + cleaned.substring(1)
            }

            if (cleaned !in beautifiedList) {
                beautifiedList.add(cleaned)
                LogManager.d(TAG, String.format(vocab.logOcr5BeautifierWineFormatted, wine, cleaned))
            }
        }

        // Purge des doublons corrompus issus du scan d'étiquette de bouteille (ex: 'CHATLAU' vs 'Château', 'dla FOsSARDIERE' vs 'Fossardière')
        val deduplicatedWines = mutableListOf<String>()
        for (wine in beautifiedList) {
            val lowerWine = OcrLevenshteinUtils.stripAccents(wine.lowercase().trim())
            val isCorruptedDuplicate = deduplicatedWines.any { existing ->
                val lowerExisting = OcrLevenshteinUtils.stripAccents(existing.lowercase().trim())
                val wordsExisting = lowerExisting.split(Regex("\\s+")).filter { it.length >= 4 }
                val wordsWine = lowerWine.split(Regex("\\s+")).filter { it.length >= 4 }
                wordsWine.isNotEmpty() && wordsWine.all { wineWord ->
                    wordsExisting.any { existingWord ->
                        val dist = OcrLevenshteinUtils.distance(wineWord, existingWord)
                        dist <= OcrLevenshteinUtils.getMaxAllowedErrors(existingWord.length) || existingWord.contains(wineWord) || wineWord.contains(existingWord)
                    }
                }
            }
            if (!isCorruptedDuplicate) {
                deduplicatedWines.add(wine)
            } else {
                LogManager.d(TAG, "[5] PURGE DOUBLON ÉTIQUETTE BOUTEILLE VIN: '$wine'")
            }
        }

        return deduplicatedWines
    }
}
