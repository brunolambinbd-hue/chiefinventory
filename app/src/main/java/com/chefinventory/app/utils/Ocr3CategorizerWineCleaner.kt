package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 3 (Catégorisation) dédié au nettoyage, à la correction orthographique et à la vérification de volume des recommandations de vins.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerWineCleaner {

    /**
     * Détermine si une ligne contient un volume explicite de bouteille de vin (ex: "75 cl", "1.5 l", "magnum").
     *
     * @param line La ligne à analyser.
     * @return `true` si un volume de bouteille est détecté, `false` sinon.
     */
    fun hasWineVolume(line: String): Boolean {
        val hasVolume = OcrRegexes.Wines.VOLUME.containsMatchIn(line)
        if (hasVolume) {
            return true
        } else {
            return OcrRegexes.Wines.STANDARD_BOTTLE.containsMatchIn(line)
        }
    }

    /**
     * Nettoie une ligne de recommandation de vin en utilisant [vocabulary].
     *
     * @param line La ligne de vin brute.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La ligne de vin nettoyée et corrigée.
     */
    fun cleanWineLine(line: String, vocabulary: OcrVocabulary): String {
        return cleanWineLine(
            line,
            Ocr3CategorizerWine.WineResources(
                appellations = vocabulary.wineAppellations,
                producers = vocabulary.wineProducers,
                keywords = vocabulary.wineKeywords,
                titleKeywords = vocabulary.wineTitleKeywords,
                removePattern = vocabulary.wineRemovePattern
            )
        )
    }

    /**
     * Nettoie une ligne de recommandation de vin en supprimant les en-têtes parasites et en appliquant les corrections orthographiques.
     *
     * @param line La ligne de vin brute (ex: "Notre vin conseillé: Un Bordeau 1996 75 cl").
     * @param resources Les ressources de vins.
     * @return La ligne nettoyée (ex: "Un Bordeaux 1996 75 cl").
     */
    fun cleanWineLine(line: String, resources: Ocr3CategorizerWine.WineResources): String {
        var cleaned = line
        cleaned = cleaned.replace(OcrRegexes.Wines.REMOVE_HEADER_PATTERN, "").trim()

        if (resources.removePattern.isNotBlank()) {
            try {
                cleaned = cleaned.replace(Regex(resources.removePattern, RegexOption.IGNORE_CASE), "").trim()
            } catch (_: Exception) {}
        }

        cleaned = cleaned.replace(OcrRegexes.Wines.HEADER_PREFIX, "").trim()
        cleaned = cleaned.replace(OcrRegexes.Wines.LEADING_PUNCTUATION, "").trim()

        return applySpellingCorrections(cleaned)
    }

    /**
     * Applique la liste des règles de correction orthographique sur les appellations et châteaux de vins.
     *
     * @param text Le texte brut à corriger.
     * @return Le texte après remplacement des fautes de frappe récurrentes.
     */
    fun applySpellingCorrections(text: String): String {
        var result = text
        for ((pattern, replacement) in OcrRegexes.Wines.SPELLING_CORRECTIONS) {
            result = pattern.replace(result, replacement)
        }
        return result
    }
}
