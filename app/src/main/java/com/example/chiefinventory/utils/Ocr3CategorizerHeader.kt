package com.example.chiefinventory.utils

import android.util.Log

/**
 * Assistant dédié à la détection des en-têtes de sections (Ingrédients, Instructions, Sous-titres)
 * et au basculement d'état du catégoriseur principal lors de l'Étape 3.
 *
 * Ce sous-composant permet de maintenir une charge cognitive minimale dans [Ocr3Categorizer].
 */
object Ocr3CategorizerHeader {
    private const val TAG = "RecipeOCR"

    /**
     * Conteneur mutable d'état de section lors de la catégorisation.
     */
    class HeaderState(
        var currentSection: Int = Ocr3Categorizer.SECTION_NONE,
        var isIngredientListClosed: Boolean = false
    )

    /**
     * Détermine si [workingLine] est un mot-clé d'en-tête de section (ex: "INGRÉDIENTS", "PRÉPARATION").
     * Si oui, met à jour [state] et retourne `true` pour que le catégoriseur effectue un `continue`.
     *
     * @return `true` si la ligne est un en-tête et doit être sautée, `false` sinon.
     */
    fun processSectionHeader(
        workingLine: String,
        lowerLine: String,
        containsAction: Boolean,
        vocabulary: OcrVocabulary,
        state: HeaderState,
        seenIngredientKeywords: MutableSet<String>
    ): Boolean {
        val cleanLower = lowerLine.trimEnd('.').trim()
        val instructionHeaders = listOf("préparation", "preparation", "instructions", "consignes", "etapes", "étapes", "méthode", "methode", "déroulement", "deroulement")
        val isInstrHeader = (instructionHeaders.any { cleanLower == it || cleanLower.startsWith("$it :") || cleanLower.startsWith("$it:") || cleanLower.startsWith("$it de la") || cleanLower.startsWith("$it de ") || cleanLower.startsWith("temps de $it") || cleanLower == "temps de $it" }) && workingLine.length < 40 && !containsAction

        val isIngrHeader = (cleanLower.startsWith("ingrédients") || cleanLower.startsWith("ingredients") || cleanLower.startsWith("composition") || cleanLower.startsWith("ngrédients")) && workingLine.length < 35 && !containsAction
        val isSubHeader = vocabulary.ingredientSubHeaders.any { lowerLine.startsWith(it.lowercase()) }

        when {
            isInstrHeader -> {
                state.currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS
                Log.d(TAG, "  [!] BASCULEMENT DE SECTION -> INSTRUCTIONS ('$workingLine')")
                return true
            }
            isIngrHeader -> {
                state.currentSection = Ocr3Categorizer.SECTION_INGREDIENTS
                state.isIngredientListClosed = false
                seenIngredientKeywords.clear()
                Log.d(TAG, "  [!] BASCULEMENT DE SECTION -> INGRÉDIENTS ('$workingLine')")
                return true
            }
            isSubHeader -> {
                state.currentSection = Ocr3Categorizer.SECTION_INGREDIENTS
                state.isIngredientListClosed = false
                Log.d(TAG, "  [!] SOUS-EN-TÊTE D'INGRÉDIENT DÉTECTÉ ('$workingLine')")
                return false
            }
        }
        return false
    }
}
