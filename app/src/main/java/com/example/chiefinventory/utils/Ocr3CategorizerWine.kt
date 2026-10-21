package com.example.chiefinventory.utils

import android.util.Log

/**
 * Assistant dédié au traitement et à l'extraction des recommandations de vins lors de l'Étape 3 (Catégorisation).
 *
 * Cet objet sous-composant isole la logique d'identification des vins, le nettoyage des appellations/châteaux/millésimes
 * et la gestion de la fermeture de la section vin du catégoriseur principal [Ocr3Categorizer].
 */
object Ocr3CategorizerWine {
    private const val TAG = "RecipeOCR"

    /**
     * Encapsule l'état mutable de la section Vin au cours du balayage de la recette.
     *
     * @property isWineSectionClosed Indique si la section vin est verrouillée (suite à la détection d'un volume de bouteille comme 75 cl).
     * @property detectedWineList Liste ordonnée des lignes de recommandation de vins extraites de la recette.
     */
    class WineState {
        var isWineSectionClosed: Boolean = false
        val detectedWineList: MutableList<String> = mutableListOf()
    }

    /**
     * Évalue si la ligne courante constitue une recommandation de vin et la traite dans l'objet [wineState].
     *
     * La détection du vin est autorisée si la ligne ne contient aucun verbe d'action de cuisine ni connecteur
     * d'ingrédient, et qu'elle se situe hors de la liste active des ingrédients (ou si un volume explicite de vin est détecté).
     *
     * @param workingLine La ligne de texte en cours d'analyse.
     * @param currentSection La section actuelle du catégoriseur principal (ex: SECTION_INGREDIENTS, SECTION_INSTRUCTIONS, SECTION_NONE).
     * @param isIngredientListClosed Indique si la liste principale des ingrédients a déjà été clôturée.
     * @param containsAction Indique si la ligne contient un verbe d'action de cuisine.
     * @param startsWithConnector Indique si la ligne commence par un connecteur d'ingrédient (ex: "de ", "d'").
     * @param vocabulary Le vocabulaire métier OCR contenant les appellations et producteurs de vin.
     * @param wineState L'objet de gestion d'état de la section vin.
     * @return `true` si la ligne a été identifiée et capturée comme du vin (et doit être sautée dans la boucle principale), `false` sinon.
     */
    fun processWineLine(
        workingLine: String,
        currentSection: Int,
        isIngredientListClosed: Boolean,
        containsAction: Boolean,
        startsWithConnector: Boolean,
        vocabulary: OcrVocabulary,
        wineState: WineState
    ): Boolean {
        if (containsAction || startsWithConnector) return false

        val allowWineDetection = currentSection != Ocr3Categorizer.SECTION_INGREDIENTS || isIngredientListClosed || WineParser.hasWineVolume(workingLine)

        if (allowWineDetection && WineParser.isWineLine(workingLine, vocabulary)) {
            if (!wineState.isWineSectionClosed) {
                val cleanedWine = WineParser.cleanWineLine(workingLine, vocabulary)
                if (cleanedWine.isNotBlank() && wineState.detectedWineList.none { it.equals(cleanedWine, ignoreCase = true) }) {
                    wineState.detectedWineList.add(cleanedWine)
                    if (WineParser.hasWineVolume(workingLine)) {
                        wineState.isWineSectionClosed = true
                        Log.d(TAG, "  [!] SECTION VIN FERMÉE (Volume/Millésime détecté)")
                    }
                }
            }
            return true
        }
        return false
    }
}
