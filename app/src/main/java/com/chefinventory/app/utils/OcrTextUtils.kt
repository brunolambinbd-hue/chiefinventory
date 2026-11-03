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
     * Détermine si une ligne courte sans chiffres (2 à 4 mots majuscules) ressemble à un nom propre d'auteur ou d'établissement.
     */
    fun isLikelyProperNameOrSource(line: String, vocabulary: OcrVocabulary? = null): Boolean {
        return Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource(line, vocabulary)
    }
}
