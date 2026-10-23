package com.example.chiefinventory.utils

/**
 * Façade orchestratrice d'aide au parsing et au nettoyage sémantique du pipeline OCR.
 *
 * Responsabilités :
 * - Déléguer les opérations d'ingrédients à [OcrIngredientUtils].
 * - Déléguer les opérations d'instructions à [OcrInstructionUtils].
 * - Déléguer les opérations de chaînes et noms propres à [OcrTextUtils].
 */
object OcrHelperUtils {

    /**
     * Détermine si une ligne contient un poids ou un volume d'ingrédient explicite.
     */
    internal fun hasExplicitWeight(line: String): Boolean {
        return OcrIngredientUtils.hasExplicitWeight(line)
    }

    /**
     * Compte le nombre de séquences d'ingrédients distinctes présentes sur une seule ligne.
     */
    internal fun countIngredientSequences(line: String): Int {
        return OcrIngredientUtils.countIngredientSequences(line)
    }

    /**
     * Découpe une ligne contenant plusieurs ingrédients fusionnés.
     */
    internal fun splitCombinedIngredients(
        line: String, 
        commonItems: List<String>, 
        mandatoryComplements: List<String> = emptyList(),
        preparationModifiers: List<String> = emptyList(),
        splitterConnectors: List<String> = emptyList()
    ): List<String> {
        return OcrIngredientUtils.splitCombinedIngredients(line, commonItems, mandatoryComplements, preparationModifiers, splitterConnectors)
    }

    /**
     * Détermine si une ligne ressemble à un nom propre d'auteur ou d'établissement de provenance.
     */
    internal fun isLikelyProperNameOrSource(line: String, vocabulary: OcrVocabulary? = null): Boolean {
        return OcrTextUtils.isLikelyProperNameOrSource(line, vocabulary)
    }

    /**
     * Vérifie si un texte correspond exactement à un mot-clé d'exclusion.
     */
    internal fun isExcluded(text: String, excludedKeywords: List<String>): Boolean {
        return OcrTextUtils.isExcluded(text, excludedKeywords)
    }

    /**
     * Effectue le nettoyage sémantique d'une ligne d'ingrédient.
     */
    internal fun cleanIngredientSemantics(
        text: String, 
        excludedKeywords: List<String>, 
        semanticExclusions: List<String> = emptyList(),
        standaloneArticles: List<String> = emptyList()
    ): String {
        return OcrIngredientUtils.cleanIngredientSemantics(text, excludedKeywords, semanticExclusions, standaloneArticles)
    }

    /**
     * Vérifie si un index de caractère se situe entre parenthèses.
     */
    internal fun isInsideParentheses(text: String, position: Int): Boolean {
        return OcrTextUtils.isInsideParentheses(text, position)
    }
}
