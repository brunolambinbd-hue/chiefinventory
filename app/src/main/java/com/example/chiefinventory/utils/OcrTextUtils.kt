package com.example.chiefinventory.utils

/**
 * Fonctions utilitaires d'analyse et de manipulation de texte et chaînes de caractères pour le pipeline OCR.
 *
 * Responsabilités :
 * - Déterminer si un caractère se trouve entre parenthèses (`isInsideParentheses`).
 * - Vérifier l'exclusion stricte de mots-clés (`isExcluded`).
 * - Identifier si une ligne ressemble à un nom propre de chef ou d'établissement de provenance (`isLikelyProperNameOrSource`).
 */
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
        for (i in 0 until position) {
            if (i < text.length) {
                if (text[i] == '(') openCount++
                if (text[i] == ')') openCount--
            }
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
     * Rejette automatiquement les lignes contenant des mots culinaires ou connecteurs d'ingrédients (ex: *"RICOTTA ET ÉPINARDS"*).
     *
     * @param line La ligne de texte à évaluer.
     * @param vocabulary Le vocabulaire métier OCR permettant de vérifier les mots d'ingrédients et de titres.
     * @return `true` si la ligne est probablement un nom propre de source/chef, `false` sinon.
     */
    fun isLikelyProperNameOrSource(line: String, vocabulary: OcrVocabulary? = null): Boolean {
        val trimmed = line.trim()
        val lower = trimmed.lowercase()
        val words = trimmed.split(Regex("\\s+"))
        if (words.size !in 2..4) return false
        if (trimmed.any { it.isDigit() }) return false

        if (vocabulary != null) {
            val foodConnectors = listOf(" et ", " ou ", " au ", " aux ", " avec ", " de ", " d'", " à ")
            val containsConnector = foodConnectors.any { lower.contains(it) }

            val containsFoodWord = containsConnector ||
                    vocabulary.commonIngredientsNoQty.any { Regex("(?i)\\b${Regex.escape(it)}\\b").containsMatchIn(lower) } ||
                    vocabulary.titleKeywords.any { Regex("(?i)\\b${Regex.escape(it)}\\b").containsMatchIn(lower) } ||
                    vocabulary.baseIngredientsExpectingVariety.any { Regex("(?i)\\b${Regex.escape(it)}\\b").containsMatchIn(lower) }

            if (containsFoodWord) return false
        }

        return words.all { word -> word.isNotEmpty() && (word[0].isUpperCase() || word.all { it.isUpperCase() }) }
    }
}
