package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Sous-composant du Bloc E de l'Étape 3 : Effectue l'aiguillage final de la ligne vers la section INGREDIENTS ou INSTRUCTIONS avec rattrapage.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3Categorizer5_BlockE_Dispatch {
    private const val TAG = "Ocr3Categorizer5_BlockE"

    /**
     * Détermine si la ligne représente un marqueur de fin de section d'ingrédients (ex: assaisonnement, sel, poivre).
     *
     * @param lowerLine La ligne nettoyée en minuscules.
     * @param endMarkers La liste facultative des marqueurs de fin d'ingrédients (ex: de [OcrVocabulary.ingredientEndMarkers]).
     * @return `true` si la ligne correspond à un marqueur de fin d'ingrédients, `false` sinon.
     */
    internal fun isEndMarkerLine(lowerLine: String, endMarkers: List<String> = emptyList()): Boolean {
        val markers = if (endMarkers.isNotEmpty()) endMarkers else OcrVocabularyLoader.loadFromProjectResources().ingredientEndMarkers
        return markers.any { marker ->
            val lowerMarker = marker.lowercase()
            lowerLine == lowerMarker || (lowerMarker.contains(" ") && lowerLine.contains(lowerMarker))
        }
    }

    /**
     * Évalue si une ligne initialement classée en instruction doit être réorientée vers les ingrédients (rattrapage).
     *
     * @param isReappearance Indique s'il s'agit d'une réapparition d'un ingrédient déjà inventorisé.
     * @param isHyphenatedInstruction Indique si la ligne se termine par une césure ou un connecteur de consigne.
     * @param isEndMarkerLine Indique s'il s'agit d'un marqueur de fin de liste d'ingrédients.
     * @param containsAction Indique la présence d'un verbe d'action culinaire.
     * @param closedState L'état de verrouillage de la liste d'ingrédients.
     * @param isStrictWithQty Indique si la ligne est un ingrédient strict accompagné d'une quantité.
     * @param isCommonWithNoAction Indique s'il s'agit d'un ingrédient courant sans verbe d'action.
     * @return `true` si la ligne doit être réorientée vers les ingrédients, `false` sinon.
     */
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
        seenIngredientKeywords: MutableSet<String>,
        nextLine: String = ""
    ): Boolean {
        val parsed = IngredientParser.parse(workingLine, vocabulary.culinaryMeasurementUnits)
        var closedState = isIngredientListClosed

        when (currentSection) {
            Ocr3Categorizer.SECTION_INGREDIENTS -> {
                if (Ocr2Cleaner.isTechnicalDimension(workingLine, vocabulary)) {
                    return closedState
                }

                val isSaltPepperMarker = vocabulary.ingredientEndMarkers.any { marker -> lowerLine.contains(marker.lowercase()) }

                if (!containsAction) {
                    if (isSaltPepperMarker) {
                        val isSeasoningFollowedByMore = nextLine.isNotBlank() && (
                                lowerLine.contains("sel") || lowerLine.contains("poivre")
                        ) && (
                                nextLine.trim().lowercase().startsWith("poivre") ||
                                nextLine.trim().lowercase().startsWith("sel") ||
                                Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin(nextLine, vocabulary.saltVarietyOrigins) ||
                                Ocr4MergerSeasoningHelper.isPepperWithPrepositionOrigin(nextLine, vocabulary.pepperDeOriginItems)
                        )
                        if (!isSeasoningFollowedByMore) {
                            closedState = true
                            if (vocabulary.logOcr3BlockEDispatchLockActivated.isNotBlank()) {
                                LogManager.d(TAG, vocabulary.logOcr3BlockEDispatchLockActivated)
                            }
                        }
                    }
                }
                val isIngrHeader = vocabulary.ocr1ProtectedIngredientsHeaders.any { header ->
                    val cleanHeader = header.trim().lowercase()
                    val cleanLine = workingLine.trim().lowercase().removeSuffix(":").removeSuffix(".").trim()
                    cleanLine == cleanHeader || cleanLine.startsWith("$cleanHeader ") || cleanLine.startsWith("$cleanHeader(") || cleanLine.startsWith("$cleanHeader:")
                } && workingLine.length < 35 && !containsAction

                if (isIngrHeader) {
                    return closedState
                }

                results.rawIngredientsList.add(workingLine)
                val isSubHeader = vocabulary.ingredientSubHeaders.any { sub ->
                    val cleanSub = sub.lowercase().trim()
                    val cleanLine = workingLine.lowercase().removeSuffix(":").removeSuffix(".").trim()
                    cleanLine == cleanSub
                }
                if (!isSubHeader) {
                    val keywords = Ocr3Categorizer.extractKeywordsFromText(parsed.name, parsed.supplementalInfo)
                        .filter { kw -> vocabulary.ingredientPreparationModifiers.none { mod -> mod.equals(kw, ignoreCase = true) } }
                    seenIngredientKeywords.addAll(keywords)
                    LogManager.d(TAG, String.format(vocabulary.logOcr3BlockEDispatchIngrSplit, parsed.quantity, parsed.unit, parsed.name, parsed.supplementalInfo, keywords))
                }
            }
            Ocr3Categorizer.SECTION_INSTRUCTIONS -> {
                val isTechDim = Ocr2Cleaner.isTechnicalDimension(workingLine, vocabulary)
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

                val endMarkerLine = isEndMarkerLine(lowerLine, vocabulary.ingredientEndMarkers)

                val isCommonIngredient = vocabulary.commonIngredientsNoQty.any { Regex("(?i)\\b${Regex.escape(it)}\\b").containsMatchIn(workingLine) }
                val endsWithDash = workingLine.trimEnd().endsWith("-")
                val conjunctions = vocabulary.ocrSentenceConjunctions.map { " ${it.lowercase().trim()}" }
                val endsWithConjunction = conjunctions.any { lowerLine.trimEnd().endsWith(it) }
                val isHyphenatedInstruction = endsWithDash || endsWithConjunction

                val hasHardOrWeight = if (hasHardQuantity) true else hasWeight
                val isStrictWithQty = isStrictIngredient && hasHardOrWeight
                val isNarrativeCompound = Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(workingLine)
                val isKeywordAlreadySeen = closedState && vocabulary.commonIngredientsNoQty.any { ingredient ->
                    ingredient.length >= 4 && lowerLine.contains(ingredient.lowercase()) &&
                            seenIngredientKeywords.any { seen -> seen.equals(ingredient, ignoreCase = true) }
                }

                val isCommonWithNoAction = if (isCommonIngredient && !containsAction && !isNarrativeCompound && !isKeywordAlreadySeen) {
                    !startsWithBullet
                } else {
                    false
                }

                val isPriorityIngredient = vocabulary.priorityIngredientPhrases.any { phrase ->
                    lowerLine.contains(phrase.lowercase())
                }
                var shouldPullBack = isPriorityIngredient || evaluateShouldPullBack(
                    isReappearance = isReappearance,
                    isHyphenatedInstruction = isHyphenatedInstruction,
                    isEndMarkerLine = endMarkerLine,
                    containsAction = containsAction,
                    closedState = closedState,
                    isStrictWithQty = isStrictWithQty,
                    isCommonWithNoAction = isCommonWithNoAction
                )

                if (isKeywordAlreadySeen) {
                    shouldPullBack = false
                }

                if (shouldPullBack) {
                    if (endMarkerLine) {
                        closedState = true
                        if (vocabulary.logOcr3BlockEDispatchLockActivated.isNotBlank()) {
                            LogManager.d(TAG, vocabulary.logOcr3BlockEDispatchLockActivated)
                        }
                    }
                    results.rawIngredientsList.add(workingLine)
                    seenIngredientKeywords.addAll(Ocr3Categorizer.extractKeywordsFromText(parsed.name, parsed.supplementalInfo))
                    LogManager.d(TAG, String.format(vocabulary.logOcr3BlockEDispatchCatchup, workingLine))
                } else {
                    results.rawInstructionsList.add(workingLine.replace(OcrRegexes.Beautifier.BULLET_PREFIX, "").trim())
                    LogManager.d(TAG, String.format(vocabulary.logOcr3BlockEDispatchInstrAdded, workingLine))
                }
            }
            else -> {
                val isIngrCandidate = looksLikeIngredient || startsWithConnector
                if (isIngrCandidate) {
                    results.rawIngredientsList.add(workingLine)
                    seenIngredientKeywords.addAll(Ocr3Categorizer.extractKeywordsFromText(parsed.name, parsed.supplementalInfo))
                    LogManager.d(TAG, String.format(vocabulary.logOcr3BlockEDispatchIngrSplit, parsed.quantity, parsed.unit, parsed.name, parsed.supplementalInfo, ""))
                } else {
                    results.rawInstructionsList.add(workingLine.replace(OcrRegexes.Beautifier.BULLET_PREFIX, "").trim())
                    LogManager.d(TAG, String.format(vocabulary.logOcr3BlockEDispatchInstrAdded, workingLine))
                }
            }
        }

        return closedState
    }
}
