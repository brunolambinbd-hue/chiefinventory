package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 3 (Catégorisation) dédié au nettoyage des préfixes téléphoniques et de la ponctuation des sources.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerSourceCleaner {

    /**
     * Nettoie une ligne de source en supprimant les préfixes téléphoniques parasites en utilisant [vocabulary].
     *
     * @param line La ligne de source brute (ex: "Tel: 01 23 45 67 89").
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La ligne nettoyée (ex: "01 23 45 67 89").
     */
    fun cleanSourceLine(line: String, vocabulary: OcrVocabulary): String {
        return cleanSourceLine(line, vocabulary.phonePrefixes)
    }

    /**
     * Nettoie une ligne de source en supprimant les préfixes téléphoniques parasites en utilisant [phonePrefixes].
     *
     * @param line La ligne de source brute (ex: "Tel: 01 23 45 67 89").
     * @param phonePrefixes La liste des préfixes de téléphone à supprimer.
     * @return La ligne nettoyée.
     */
    fun cleanSourceLine(line: String, phonePrefixes: List<String>): String {
        var cleaned = line.trim()
        phonePrefixes.forEach { prefix ->
            val prefixRegex = OcrRegexes.getPhonePrefixPattern(prefix)
            cleaned = cleaned.replace(prefixRegex, "").trim()
        }
        return cleaned.replace(OcrRegexes.Sources.LEADING_PUNCTUATION, "").trim()
    }
}
