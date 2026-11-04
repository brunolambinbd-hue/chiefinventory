package com.chefinventory.app.utils

import android.util.Log

/**
 * Résultat de l'analyse du Bloc D (Signaux d'instructions & Classification de section).
 *
 * @property shouldContinue `true` si la ligne est vide ou constitue du bruit à ignorer.
 * @property workingLine La ligne nettoyée.
 * @property updatedSection La section classifiée finale (INGREDIENTS ou INSTRUCTIONS).
 */
internal data class BlockDResult(
    val shouldContinue: Boolean,
    val workingLine: String,
    val updatedSection: Int
)

/**
 * Sous-composant du Bloc D de l'Étape 3 : Filtre la sémantique d'ingrédients et le bruit post-vin, puis classifie la section finale.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3Categorizer4_BlockD_Classification {
    private const val TAG = "Ocr3Categorizer4_BlockD"

    internal fun isInstructionSignal(
        hasUnclosedParenthesis: Boolean,
        hasInstructionTrigger: Boolean,
        isReappearance: Boolean,
        isStepNumber: Boolean,
        containsAction: Boolean,
        workingLineLength: Int,
        startsWithBullet: Boolean
    ): Boolean {
        if (hasUnclosedParenthesis) return false
        if (hasInstructionTrigger) return true
        if (isReappearance) return true
        if (isStepNumber) return true
        if (containsAction) {
            return workingLineLength > 25 || startsWithBullet
        }
        return false
    }

    internal fun classifySection(
        currentSection: Int,
        isInstructionSignal: Boolean,
        isIngredientListClosed: Boolean,
        hasHardQuantity: Boolean,
        hasWeight: Boolean,
        looksLikeIngredient: Boolean,
        startsWithConnector: Boolean
    ): Int {
        val hasNoQtyNoWeight = !hasHardQuantity && !hasWeight
        val isClosedNoQtyNoWeight = isIngredientListClosed && hasNoQtyNoWeight
        if (isInstructionSignal || isClosedNoQtyNoWeight) {
            return Ocr3Categorizer.SECTION_INSTRUCTIONS
        }
        if (currentSection == Ocr3Categorizer.SECTION_NONE) {
            if (looksLikeIngredient || startsWithConnector) {
                return Ocr3Categorizer.SECTION_INGREDIENTS
            }
        }
        return currentSection
    }

    /**
     * Bloc D : Filtre la sémantique d'ingrédients et le bruit post-vin, puis classifie la section finale.
     *
     * @param rawWorkingLine La ligne de travail courante.
     * @param currentSection La section courante.
     * @param isIngredientListClosed Indique si la liste d'ingrédients est verrouillée.
     * @param containsAction Indique la présence d'un verbe d'action.
     * @param startsWithBullet Indique la présence d'une puce.
     * @param startsWithConnector Indique la présence d'un connecteur.
     * @param hasWeight Indique la présence d'un poids.
     * @param hasHardQuantity Indique la présence d'une quantité forte.
     * @param isReappearance Indique une réapparition d'ingrédient.
     * @param hasInstructionTrigger Indique un déclencheur d'instructions.
     * @param looksLikeIngredient Indique si la ligne ressemble à un ingrédient.
     * @param wineState L'état du vin.
     * @param vocabulary Le vocabulaire métier.
     * @param results Le conteneur [RawSections].
     * @return L'objet [BlockDResult].
     */
    internal fun processBlockD_SectionClassification(
        rawWorkingLine: String,
        currentSection: Int,
        isIngredientListClosed: Boolean,
        containsAction: Boolean,
        startsWithBullet: Boolean,
        startsWithConnector: Boolean,
        hasWeight: Boolean,
        hasHardQuantity: Boolean,
        isReappearance: Boolean,
        hasInstructionTrigger: Boolean,
        looksLikeIngredient: Boolean,
        wineState: Ocr3CategorizerWine.WineState,
        vocabulary: OcrVocabulary,
        results: RawSections
    ): BlockDResult {
        var workingLine = rawWorkingLine.replace(Regex("\\s+"), " ").trim()

        workingLine = Ocr3CategorizerIngredientDetector.cleanIngredientSemantics(workingLine, vocabulary.excludedOcrKeywords, vocabulary.ingredientSemanticExclusions)
        if (workingLine.isEmpty()) {
            return BlockDResult(true, workingLine, currentSection)
        }

        // Ignorer le bruit de pied de page / étiquette de bouteille après fermeture de la section vin
        if (wineState.isWineSectionClosed) {
            if (!containsAction) {
                if (!startsWithBullet) {
                    return BlockDResult(true, workingLine, currentSection)
                }
            }
        }

        val lastRawIngNull = results.rawIngredientsList.lastOrNull()
        val lastRawIng = if (lastRawIngNull != null) lastRawIngNull else ""
        val isIngrSection = currentSection == Ocr3Categorizer.SECTION_INGREDIENTS
        val hasMoreOpenParens = lastRawIng.count { it == '(' } > lastRawIng.count { it == ')' }
        val hasUnclosedParenthesis = isIngrSection && hasMoreOpenParens

        val isStepNumber = Regex("^\\d{1,2}[.)\\-]\\s*").containsMatchIn(workingLine)
        val isInstructionSignal = isInstructionSignal(
            hasUnclosedParenthesis, hasInstructionTrigger, isReappearance,
            isStepNumber, containsAction, workingLine.length, startsWithBullet
        )

        Log.d(TAG, "  => BILAN: reappear=$isReappearance | instrSignal=$isInstructionSignal | currentSec=$currentSection")

        val updatedSection = classifySection(
            currentSection, isInstructionSignal, isIngredientListClosed,
            hasHardQuantity, hasWeight, looksLikeIngredient, startsWithConnector
        )

        return BlockDResult(false, workingLine, updatedSection)
    }
}
