package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 3 (Catégorisation) dédié à la détection sémantique des noms propres d'auteurs, chefs et établissements.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerProperNameDetector {

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

        val activeVocab = vocabulary ?: OcrVocabularyLoader.loadFromProjectResources()

        val containsConnector = activeVocab.ingredientConnectors.any { lower.contains(" ${it.trim().lowercase()} ") || lower.contains(" ${it.trim().lowercase()}") }

        val containsFoodWord = containsConnector ||
                activeVocab.commonIngredientsNoQty.any { Regex("(?i)\\b${Regex.escape(it)}\\b").containsMatchIn(lower) } ||
                activeVocab.titleKeywords.any { Regex("(?i)\\b${Regex.escape(it)}\\b").containsMatchIn(lower) } ||
                activeVocab.baseIngredientsExpectingVariety.any { Regex("(?i)\\b${Regex.escape(it)}\\b").containsMatchIn(lower) }

        if (containsFoodWord) return false

        return words.all { word -> word[0].isUpperCase() }
    }
}
