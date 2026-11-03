package com.chefinventory.app.utils

import android.util.Log

/**
 * Sous-composant du Bloc E de l'Étape 3 : Effectue l'aiguillage final de la ligne vers la section INGREDIENTS ou INSTRUCTIONS avec rattrapage.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3Categorizer5_BlockE_Dispatch {
    private const val TAG = "Ocr3Categorizer5_BlockE"

    internal fun isEndMarkerLine(lowerLine: String): Boolean {
        val hasBothSaltPoivre = lowerLine.contains("sel") && lowerLine.contains("poivre")
        if (hasBothSaltPoivre) return true
        if (lowerLine == "sel") return true
        return lowerLine == "poivre"
    }

    internal fun evaluateShouldPullBack(
        isReappearance: Boolean,
        isHyphenatedInstruction: Boolean,
        isEndMarkerLine: Boolean,
        containsAction: Boolean,
        closedState: Boolean,
        isStrictWithQty: Boolean,
        isCommonWithNoAction: Boolean
    ): Boolean {
        if (isReappearance) return false
        if (isHyphenatedInstruction) return false
        if (isEndMarkerLine && !containsAction) return true
        if (closedState) {
            return isStrictWithQty || isCommonWithNoAction
        }
        return isStrictWithQty
    }

    /**
     * Bloc E : Effectue l'aiguillage final de la ligne vers la section INGREDIENTS ou INSTRUCTIONS avec rattrapage.
     *
     * @param workingLine La ligne nettoyée à aiguiller.
     * @param lowerLine La ligne nettoyée en minuscules.
     * @param currentSection La section courante.
     * @param isIngredientListClosed L'état de verrouillage de la liste d'ingrédients.
     * @param containsAction Indique la présence d'un verbe d'action.
     * @param startsWithBullet Indique la présence d'une puce.
     * @param startsWithConnector Indique la présence d'un connecteur.
     * @param hasWeight Indique la présence d'un poids.
     * @param hasHardQuantity Indique la présence d'une quantité forte.
     * @param isReappearance Indique la réapparition d'un ingrédient.
     * @param hasInstructionTrigger Indique la présence d'un déclencheur d'instructions.
     * @param looksLikeIngredient Indique si la ligne ressemble à un ingrédient.
     * @param vocabulary Le vocabulaire métier.
     * @param results Le conteneur [RawSections].
     * @param seenIngredientKeywords L'ensemble des mots-clés d'ingrédients recensés.
     * @return L'état mis à jour du verrou de la liste d'ingrédients (`isIngredientListClosed`).
     */
    internal fun processBlockE_SectionDispatch(
        workingLine: String,
        lowerLine: String,
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
        vocabulary: OcrVocabulary,
        results: RawSections,
        seenIngredientKeywords: MutableSet<String>
    ): Boolean {
        val parsed = IngredientParser.parse(workingLine)
        var closedState = isIngredientListClosed

        when (currentSection) {
            Ocr3Categorizer.SECTION_INGREDIENTS -> {
                if (Ocr2Cleaner.isTechnicalDimension(workingLine)) {
                    return closedState
                }

                val isSaltPepperMarker = lowerLine.contains("sel") && lowerLine.contains("poivre")

                if (!containsAction) {
                    if (isSaltPepperMarker) {
                        closedState = true
                        Log.d(TAG, "  [!] VERROU ACTIVÉ (Marqueur de fin sel/poivre détecté)")
                    }
                }
                results.rawIngredientsList.add(workingLine)
                val keywords = Ocr3Categorizer.extractKeywordsFromText(parsed.name, parsed.supplementalInfo)
                    .filter { kw -> vocabulary.ingredientPreparationModifiers.none { mod -> mod.equals(kw, ignoreCase = true) } }
                seenIngredientKeywords.addAll(keywords)
                Log.d(TAG, "  [INGR] Split: Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}', Info='${parsed.supplementalInfo}' | Keywords added: $keywords")
            }
            Ocr3Categorizer.SECTION_INSTRUCTIONS -> {
                val isTechDim = Ocr2Cleaner.isTechnicalDimension(workingLine)
                val isIngrOrConnector = looksLikeIngredient || startsWithConnector
                val isStrictIngredient = if (isIngrOrConnector && !containsAction) {
                    if (!isTechDim) {
                        !hasInstructionTrigger
                    } else {
                        false
                    }
                } else {
                    false
                }

                val endMarkerLine = isEndMarkerLine(lowerLine)

                val isCommonIngredient = vocabulary.commonIngredientsNoQty.any { lowerLine.contains(it.lowercase()) }
                val endsWithDash = workingLine.trimEnd().endsWith("-")
                val endsWithEt = lowerLine.trimEnd().endsWith(" et")
                val endsWithOu = lowerLine.trimEnd().endsWith(" ou")
                val isHyphenatedInstruction = endsWithDash || endsWithEt || endsWithOu

                val hasHardOrWeight = if (hasHardQuantity) true else hasWeight
                val isStrictWithQty = isStrictIngredient && hasHardOrWeight
                val isCommonWithNoAction = if (isCommonIngredient && !containsAction) {
                    !startsWithBullet
                } else {
                    false
                }

                val shouldPullBack = evaluateShouldPullBack(
                    isReappearance = isReappearance,
                    isHyphenatedInstruction = isHyphenatedInstruction,
                    isEndMarkerLine = endMarkerLine,
                    containsAction = containsAction,
                    closedState = closedState,
                    isStrictWithQty = isStrictWithQty,
                    isCommonWithNoAction = isCommonWithNoAction
                )

                if (shouldPullBack) {
                    if (endMarkerLine) {
                        closedState = true
                        Log.d(TAG, "  [!] VERROU ACTIVÉ DEPUIS RATTRAPAGE (Marqueur de fin détecté)")
                    }
                    results.rawIngredientsList.add(workingLine)
                    seenIngredientKeywords.addAll(Ocr3Categorizer.extractKeywordsFromText(parsed.name, parsed.supplementalInfo))
                    Log.d(TAG, "  [RATTRAPAGE] Split: Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}', Info='${parsed.supplementalInfo}'")
                } else {
                    results.rawInstructionsList.add(workingLine.replace(OcrRegexes.Beautifier.BULLET_PREFIX, "").trim())
                }
            }
            else -> {
                val isIngrCandidate = looksLikeIngredient || startsWithConnector
                if (isIngrCandidate) {
                    results.rawIngredientsList.add(workingLine)
                    seenIngredientKeywords.addAll(Ocr3Categorizer.extractKeywordsFromText(parsed.name, parsed.supplementalInfo))
                    Log.d(TAG, "  [DEFAUT] Split: Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}', Info='${parsed.supplementalInfo}'")
                } else {
                    results.rawInstructionsList.add(workingLine.replace(OcrRegexes.Beautifier.BULLET_PREFIX, "").trim())
                }
            }
        }

        return closedState
    }
}
