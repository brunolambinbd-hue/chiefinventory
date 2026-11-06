package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Assistant dédié à la détection des en-têtes de sections (Ingrédients, Instructions, Sous-titres)
 * et au basculement d'état du catégoriseur principal lors de l'Étape 3.
 *
 * Responsabilités :
 * - Détecter les mots-clés d'en-têtes simples ou complexes (ex: "INGRÉDIENTS", "ingrédients pour 4 personnes", "PRÉPARATION").
 * - Gérer le basculement d'état [HeaderState] entre les sections [Ocr3Categorizer.SECTION_INGREDIENTS] et [Ocr3Categorizer.SECTION_INSTRUCTIONS].
 * - Réinitialiser le verrou d'ingrédients et les mots-clés vus lors de la détection d'un nouvel en-tête.
 */
object Ocr3CategorizerHeader {
    private const val TAG = "RecipeOCR"

    /**
     * Conteneur mutable d'état de section lors de la catégorisation.
     *
     * @property currentSection L'identifiant de la section courante ([Ocr3Categorizer.SECTION_NONE], [Ocr3Categorizer.SECTION_INGREDIENTS], [Ocr3Categorizer.SECTION_INSTRUCTIONS]).
     * @property isIngredientListClosed Indicateur de fermeture de la liste d'ingrédients (activé par un marqueur fin sel/poivre).
     */
    class HeaderState(
        var currentSection: Int = Ocr3Categorizer.SECTION_NONE,
        var isIngredientListClosed: Boolean = false
    )

    /**
     * Détermine si [workingLine] est un mot-clé d'en-tête de section (ex: "INGRÉDIENTS", "ingrédients pour 4 personnes", "PRÉPARATION").
     * Si oui, met à jour [state] et retourne `true` pour que le catégoriseur principal effectue un `continue`.
     *
     * @param workingLine La ligne de texte courante analysée.
     * @param lowerLine La ligne de texte en minuscules.
     * @param containsAction `true` si la ligne contient un verbe d'action culinaire à l'impératif.
     * @param vocabulary Le vocabulaire métier data-driven contenant les sous-en-têtes.
     * @param state L'état mutable [HeaderState] mis à jour si un en-tête est détecté.
     * @param seenIngredientKeywords L'ensemble mutable des mots-clés d'ingrédients réinitialisé lors d'un nouvel en-tête d'ingrédients.
     * @return `true` si la ligne est un en-tête et doit être sautée par le catégoriseur, `false` sinon.
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
        val isInstrHeader = (vocabulary.ocr1ProtectedInstructionsHeaders.any { 
            val lowerIt = it.lowercase()
            cleanLower == lowerIt || 
            cleanLower.startsWith("$lowerIt :") || 
            cleanLower.startsWith("$lowerIt:") || 
            cleanLower.startsWith("$lowerIt de ") || 
            cleanLower.startsWith("$lowerIt d'") || 
            cleanLower.startsWith("temps de $lowerIt")
        }) && workingLine.length < 40 && !containsAction

        val isIngrHeader = vocabulary.ocr1ProtectedIngredientsHeaders.any { cleanLower.startsWith(it.lowercase()) } && workingLine.length < 35 && !containsAction
        val isSubHeader = vocabulary.ingredientSubHeaders.any { lowerLine.startsWith(it.lowercase()) }

        when {
            isInstrHeader -> {
                state.currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS
                LogManager.d(TAG, String.format(vocabulary.logOcr3HeaderSwitchInstructions, workingLine))
                return true
            }
            isIngrHeader -> {
                state.currentSection = Ocr3Categorizer.SECTION_INGREDIENTS
                state.isIngredientListClosed = false
                seenIngredientKeywords.clear()
                LogManager.d(TAG, String.format(vocabulary.logOcr3HeaderSwitchIngredients, workingLine))
                return true
            }
            isSubHeader -> {
                state.currentSection = Ocr3Categorizer.SECTION_INGREDIENTS
                state.isIngredientListClosed = false
                LogManager.d(TAG, String.format(vocabulary.logOcr3HeaderSubheaderDetected, workingLine))
                return false
            }
        }
        return false
    }
}
