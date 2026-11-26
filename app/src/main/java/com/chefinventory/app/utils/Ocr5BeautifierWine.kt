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

        // Purge et substitution intelligente des doublons corrompus issus du scan d'étiquette de bouteille (ex: 'CHATLAU' vs 'Château', 'TOLRAINE' vs 'Touraine')
        val deduplicatedWines = mutableListOf<String>()
        val knownDictWines = (vocab.wineAppellations + vocab.wineProducers).map { OcrLevenshteinUtils.stripAccents(it.lowercase().trim()) }.toSet()
        val genericWineDescriptors = setOf("VIN", "ROUGE", "BLANC", "ROSE", "SEC", "BRUT", "DOUX", "DEMI", "MOELLEUX", "MOUSSEUX")

        for (wine in beautifiedList) {
            val lowerWine = OcrLevenshteinUtils.stripAccents(wine.lowercase().trim().removeSuffix(","))

            var matchedExistingIdx = -1
            for ((idx, existing) in deduplicatedWines.withIndex()) {
                val lowerExisting = OcrLevenshteinUtils.stripAccents(existing.lowercase().trim().removeSuffix(","))
                val wordsExisting = lowerExisting.replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").split(Regex("\\s+")).filter { it.length >= 4 && it !in genericWineDescriptors }
                val wordsWine = lowerWine.replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").split(Regex("\\s+")).filter { it.length >= 4 && it !in genericWineDescriptors }
                val isWordByWordMatch = wordsWine.isNotEmpty() && wordsWine.all { wineWord ->
                    wordsExisting.any { existingWord ->
                        val dist = OcrLevenshteinUtils.distance(wineWord, existingWord)
                        dist <= OcrLevenshteinUtils.getMaxAllowedErrors(existingWord.length) || existingWord.contains(wineWord) || wineWord.contains(existingWord)
                    }
                }
                val isSubstringMatch = (lowerWine.contains(lowerExisting) || lowerExisting.contains(lowerWine)) &&
                        lowerWine.length >= 4 && lowerExisting.length >= 4

                val isFragmentMatch = wordsWine.any { w ->
                    wordsExisting.any { e ->
                        val dist = OcrLevenshteinUtils.distance(w, e)
                        dist <= OcrLevenshteinUtils.getMaxAllowedErrors(maxOf(w.length, e.length)) || w.contains(e) || e.contains(w)
                    }
                }

                if (isWordByWordMatch || isSubstringMatch || isFragmentMatch) {
                    matchedExistingIdx = idx
                    break
                }
            }

            if (matchedExistingIdx == -1) {
                deduplicatedWines.add(wine)
            } else {
                val existing = deduplicatedWines[matchedExistingIdx]
                val lowerExisting = OcrLevenshteinUtils.stripAccents(existing.lowercase().trim().removeSuffix(","))
                val hasDictAppellationWine = knownDictWines.any { app -> lowerWine.contains(app) }
                val hasDictAppellationExisting = knownDictWines.any { app -> lowerExisting.contains(app) }

                if (hasDictAppellationWine && !hasDictAppellationExisting) {
                    LogManager.d(TAG, "[5] REPLACEMENT DU VIN CORROMPU PAR MOT DU DICTIONNAIRE: '$existing' -> '$wine'")
                    deduplicatedWines[matchedExistingIdx] = wine
                } else if (!hasDictAppellationWine && hasDictAppellationExisting) {
                    LogManager.d(TAG, "[5] CONSERVATION DU VIN DU DICTIONNAIRE EXISTANT: '$existing' (IGNORÉ: '$wine')")
                } else if (wine.length > existing.length && !hasDictAppellationExisting) {
                    LogManager.d(TAG, "[5] REMPLACEMENT PAR LA PHRASE DE VIN PLUS COMPLÈTE: '$existing' -> '$wine'")
                    deduplicatedWines[matchedExistingIdx] = wine
                } else {
                    LogManager.d(TAG, "[5] PURGE DOUBLON ÉTIQUETTE BOUTEILLE VIN: '$wine'")
                }
            }
        }

        val finalDeduplicated = mutableListOf<String>()
        for (wine in deduplicatedWines) {
            val lowerWine = OcrLevenshteinUtils.stripAccents(wine.lowercase().trim().removeSuffix(","))
            val isSingleWordFragment = lowerWine.replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").split(Regex("\\s+")).filter { it.isNotBlank() }.size == 1
            if (isSingleWordFragment) {
                val matchesLargerDictEntry = deduplicatedWines.any { other ->
                    if (other == wine) false else {
                        val lowerOther = OcrLevenshteinUtils.stripAccents(other.lowercase().trim().removeSuffix(","))
                        val hasDictAppellationOther = knownDictWines.any { app -> lowerOther.contains(app) }
                        if (hasDictAppellationOther) {
                            val wordsOther = lowerOther.replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").split(Regex("\\s+")).filter { it.length >= 4 }
                            wordsOther.any { w ->
                                val dist = OcrLevenshteinUtils.distance(w, lowerWine)
                                dist <= OcrLevenshteinUtils.getMaxAllowedErrors(maxOf(w.length, lowerWine.length)) || w.contains(lowerWine) || lowerWine.contains(w)
                            }
                        } else false
                    }
                }
                if (matchesLargerDictEntry) {
                    LogManager.d(TAG, "[5] PURGE DU FRAGMENT MONOMOT D'ÉTIQUETTE: '$wine'")
                    continue
                }
            }
            finalDeduplicated.add(wine)
        }

        val standaloneYear = finalDeduplicated.firstOrNull { it.trim().matches(Regex("^(19\\d\\d|20\\d\\d)$")) }
        if (standaloneYear != null) {
            val otherWines = finalDeduplicated.filter { it != standaloneYear }
            val yearAlreadyInText = otherWines.any { it.contains(standaloneYear) }
            if (!yearAlreadyInText && otherWines.isNotEmpty()) {
                return listOf(standaloneYear) + otherWines
            }
        }

        return finalDeduplicated
    }
}
