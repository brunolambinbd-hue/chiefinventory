package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 5 (Embellissement) dédié à la beautification et à l'harmonisation de la majusculation des titres.
 */
object Ocr5BeautifierTitle {

    /**
     * Harmonise la majusculation d'un titre de recette en Sentence case français (première lettre en majuscule, le reste en minuscules)
     * et applique la correction tolérante Levenshtein sur les fautes de frappe d'impression de l'OCR (ex: "fprincesses" -> "princesses").
     *
     * @param title Le titre brut à beautifier.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return Le titre harmonisé et corrigé (ex: "Haricots princesses au parmesan").
     */
    fun beautifyTitle(title: String?, vocabulary: OcrVocabulary? = null): String? {
        if (title.isNullOrBlank()) return title
        val trimmedTitle = title.trim().trimEnd('.', ':', '!', '?').trim()
        if (trimmedTitle.isBlank()) return title
        val vocab = vocabulary ?: OcrVocabularyLoader.loadFromProjectResources()
        val titleDictionary = (vocab.titleKeywords + vocab.commonIngredientsNoQty).distinct()

        val words = trimmedTitle.split(Regex("\\s+"))
        val correctedWords = words.map { word ->
            val cleanW = word.trim().removeSurrounding("•").removeSurrounding("-").removeSurrounding(".").removeSurrounding(",")
            val cleanWWithoutPrefix = cleanW.replace(Regex("(?i)^(?:du|de|d'|des|la|le|l'|un|une)\\s*"), "").trim()
            if (cleanWWithoutPrefix.length <= 3) {
                word
            } else {
                val bestMatch = OcrLevenshteinUtils.findBestMatch(cleanWWithoutPrefix, titleDictionary)
                if (bestMatch != null) {
                    val cleanBest = bestMatch.replace(Regex("(?i)^(?:du|de|d'|des|la|le|l'|un|une)\\s+"), "").trim()
                    val dist = OcrLevenshteinUtils.distance(OcrLevenshteinUtils.stripAccents(cleanWWithoutPrefix), OcrLevenshteinUtils.stripAccents(cleanBest))
                    val maxAllowed = OcrLevenshteinUtils.getMaxAllowedErrors(cleanBest.length)
                    val isPluralDiffOnly = cleanWWithoutPrefix.lowercase().trim().removeSuffix("s").removeSuffix("x") == cleanBest.lowercase().trim().removeSuffix("s").removeSuffix("x")
                    if (dist in 1..maxAllowed && !isPluralDiffOnly) {
                        val pattern = Regex("(?iu)\\b${Regex.escape(cleanWWithoutPrefix)}\\b")
                        if (pattern.containsMatchIn(word)) {
                            pattern.replace(word, java.util.regex.Matcher.quoteReplacement(cleanBest))
                        } else {
                            word
                        }
                    } else word
                } else word
            }
        }
        val correctedTitle = correctedWords.joinToString(" ")
        return correctedTitle.lowercase().replaceFirstChar { it.titlecase() }
    }
}
