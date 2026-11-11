package com.chefinventory.app.utils

/**
 * Fonctions utilitaires d'analyse et de manipulation de texte et chaînes de caractères pour le pipeline OCR.
 *
 * Responsabilités :
 * - Déterminer si un caractère se trouve entre parenthèses (`isInsideParentheses`).
 * - Vérifier l'exclusion stricte de mots-clés (`isExcluded`).
 * - Déléguer la détection des noms propres à [Ocr3CategorizerProperNameDetector].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OcrTextUtils {

    /**
     * Vérifie si un index de caractère donné se situe à l'intérieur de parenthèses.
     *
     * @param text Le texte hôte à analyser.
     * @param position L'index de position du caractère.
     * @return `true` si la position se trouve entre une parenthèse ouvrante et fermante, `false` sinon.
     */
    fun isInsideParentheses(text: String, position: Int): Boolean {
        var openCount = 0
        val maxPos = minOf(position, text.length)
        for (i in 0 until maxPos) {
            if (text[i] == '(') openCount++
            if (text[i] == ')') openCount--
        }
        return openCount > 0
    }

    /**
     * Vérifie si un texte correspond exactement à un mot-clé d'exclusion (insensible à la casse).
     *
     * @param text Le texte à vérifier.
     * @param excludedKeywords La liste des mots-clés d'exclusion (`excluded_keywords.xml`).
     * @return `true` si le texte correspond à un mot d'exclusion, `false` sinon.
     */
    fun isExcluded(text: String, excludedKeywords: List<String>): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        return excludedKeywords.any { it.trim().equals(trimmed, ignoreCase = true) }
    }

    /**
     * S'assure qu'une chaîne de texte se termine par une ponctuation de fin de phrase (. ! ?).
     *
     * @param text Le texte à vérifier et ponctuer.
     * @param terminators La liste des caractères de ponctuation de fin de phrase issue de `ocr_shared_punctuation.xml`.
     * @return Le texte ponctué.
     */
    fun ensureSentenceEndingPunctuation(text: String, terminators: List<String> = emptyList()): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        val activeTerminators = if (terminators.isNotEmpty()) {
            terminators
        } else {
            OcrVocabularyLoader.loadFromProjectResources().sentenceEndingPunctuation
        }
        val lastCharStr = trimmed.last().toString()
        val hasTerminator = activeTerminators.any { it == lastCharStr }
        return if (hasTerminator) trimmed else "$trimmed."
    }

    /**
     * Détermine si un caractère appartient au répertoire des lettres de l'alphabet français (avec accents) ou des chiffres.
     *
     * @param c Le caractère à évaluer.
     * @return `true` si le caractère est une lettre française ou un chiffre, `false` sinon.
     */
    fun isFrenchLetterOrDigit(c: Char): Boolean {
        if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') return true
        val lower = c.lowercaseChar()
        return lower in "àâäéèêëîïôöùûüÿçæœ"
    }

    /**
     * Détermine si une ligne ne contient aucune lettre française ni aucun chiffre (bruit de symboles ou glyphes étrangers purs).
     *
     * @param line La ligne de texte à évaluer.
     * @return `true` si la ligne ne contient ni lettre française ni chiffre, `false` sinon.
     */
    fun isNonAlphanumericNoise(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return false
        return trimmed.none { isFrenchLetterOrDigit(it) }
    }

    /**
     * Détermine si une ligne de texte représente un bruit court orphelin (longueur condensée <= 3 sans chiffres).
     *
     * @param line La ligne de texte à évaluer.
     * @param standaloneTrashWords La liste des articles/prépositions isolés à rejeter.
     * @param hasActionVerb `true` si la ligne contient un verbe d'action culinaire.
     * @return `true` si la ligne est un bruit court orphelin, `false` sinon.
     */
    fun isShortNoise(
        line: String,
        standaloneTrashWords: Set<String> = emptySet(),
        hasActionVerb: Boolean = false
    ): Boolean {
        if (hasActionVerb || line.isBlank()) return false
        if (line.any { it.isDigit() }) return false
        val lowerCondensed = line.lowercase().replace(" ", "").removeSuffix(".")
        return lowerCondensed.length <= 3 || lowerCondensed in standaloneTrashWords
    }

    /**
     * Détermine si une ligne courte sans chiffres (2 à 4 mots majuscules) ressemble à un nom propre d'auteur ou d'établissement.
     */
    fun isLikelyProperNameOrSource(line: String, vocabulary: OcrVocabulary? = null): Boolean {
        return Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource(line, vocabulary)
    }
}
