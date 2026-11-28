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
     * Évalue si la ligne est une ligne de condiments (sel/poivre) non suivie de condiments supplémentaires, et verrouille l'état.
     */
    internal fun evalSeasoningClosedState(
        workingLine: String,
        nextLine: String,
        containsAction: Boolean,
        closedState: Boolean,
        vocabulary: OcrVocabulary
    ): Boolean {
        if (containsAction) return closedState
        val lowerLine = workingLine.lowercase()
        val isSaltPepperMarker = vocabulary.ingredientEndMarkers.any { lowerLine.contains(it.lowercase()) }
        if (!isSaltPepperMarker) return closedState

        val isSaltOrPepperLine = lowerLine.contains("sel") || lowerLine.contains("poivre")
        val cleanNext = nextLine.trim().lowercase()
        val isNextSeasoning = if (cleanNext.isEmpty()) {
            false
        } else {
            cleanNext.startsWith("poivre") ||
            cleanNext.startsWith("sel") ||
            Ocr4MergerSeasoningHelper.isSaltWithPrepositionOrigin(cleanNext, vocabulary.saltVarietyOrigins) ||
            Ocr4MergerSeasoningHelper.isPepperWithPrepositionOrigin(cleanNext, vocabulary.pepperDeOriginItems)
        }

        val isSeasoningFollowedByMore = isSaltOrPepperLine && isNextSeasoning
        if (!isSeasoningFollowedByMore) {
            val logLock = vocabulary.logOcr3BlockEDispatchLockActivated
            LogManager.d(TAG, logLock)
            return true
        }
        return closedState
    }

    /**
     * Évalue si la consigne précédente se termine par un connecteur suspendu.
     */
    internal fun checkPrevInstEndsWithHangingConnector(
        lastInstLine: String,
        hangingConnectors: List<String>
    ): Boolean {
        if (lastInstLine.isBlank()) return false
        val lastInstLower = lastInstLine.trim().lowercase()
        return hangingConnectors.any { rawConn ->
            val conn = rawConn.trim().lowercase()
            lastInstLower.endsWith(" $conn") || lastInstLower == conn || (conn.endsWith("'") && lastInstLower.endsWith(conn))
        }
    }

    /**
     * Évalue si un mot-clé d'ingrédient courant (longueur >= 4) a déjà été vu.
     */
    internal fun checkKeywordAlreadySeen(
        closedState: Boolean,
        lowerLine: String,
        commonIngredientsNoQty: List<String>,
        seenIngredientKeywords: Set<String>
    ): Boolean {
        if (!closedState || commonIngredientsNoQty.isEmpty() || seenIngredientKeywords.isEmpty()) return false
        val lowerIngrs = commonIngredientsNoQty.filter { it.length >= 4 }.map { it.lowercase() }
        val matchingIngr = lowerIngrs.firstOrNull { lowerLine.contains(it) } ?: return false
        return seenIngredientKeywords.any { it.equals(matchingIngr, ignoreCase = true) }
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
        if (isReappearance || isHyphenatedInstruction) return false
        if (isEndMarkerLine && !containsAction) return true
        if (closedState) return isStrictWithQty || isCommonWithNoAction
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

                closedState = evalSeasoningClosedState(workingLine, nextLine, containsAction, closedState, vocabulary)

                val isIngrHeader = vocabulary.ocr1ProtectedIngredientsHeaders.any { header ->
                    val cleanHeader = header.trim().lowercase()
                    val cleanLine = workingLine.trim().lowercase().removeSuffix(":").removeSuffix(".").trim()
                    cleanLine.startsWith(cleanHeader)
                } && workingLine.length < 35 && !containsAction

                if (isIngrHeader) {
                    return closedState
                }

                results.rawIngredientsList.add(workingLine)
                val cleanLineSub = workingLine.lowercase().removeSuffix(":").removeSuffix(".").trim()
                val isSubHeader = vocabulary.ingredientSubHeaders.any { sub ->
                    cleanLineSub == sub.lowercase().trim()
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
                val isStrictIngredient = isIngrOrConnector && !containsAction && !isTechDim && !hasInstructionTrigger

                val endMarkerLine = isEndMarkerLine(lowerLine, vocabulary.ingredientEndMarkers)

                val isCommonIngredient = vocabulary.commonIngredientsNoQty.any { Regex("(?i)\\b${Regex.escape(it)}\\b").containsMatchIn(workingLine) }
                val lastInstLine = results.rawInstructionsList.lastOrNull()?.trimEnd() ?: ""
                val prevInstEndsWithDash = lastInstLine.endsWith("-")
                val prevInstEndsWithHangingConnector = checkPrevInstEndsWithHangingConnector(lastInstLine, vocabulary.hangingConnectors)

                val endsWithDash = workingLine.trimEnd().endsWith("-")
                val conjunctions = vocabulary.ocrSentenceConjunctions.map { " ${it.lowercase().trim()}" }
                val endsWithConjunction = conjunctions.any { lowerLine.trimEnd().endsWith(it) }
                val isHyphenatedInstruction = endsWithDash || endsWithConjunction || prevInstEndsWithDash || prevInstEndsWithHangingConnector

                val hasHardOrWeight = hasHardQuantity || hasWeight
                val isStrictWithQty = isStrictIngredient && hasHardOrWeight
                val isNarrativeCompound = Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(workingLine)
                val isKeywordAlreadySeen = checkKeywordAlreadySeen(closedState, lowerLine, vocabulary.commonIngredientsNoQty, seenIngredientKeywords)

                val isCommonWithNoAction = isCommonIngredient && !containsAction && !isNarrativeCompound && !isKeywordAlreadySeen && !startsWithBullet

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

                val lastRawInst = results.rawInstructionsList.lastOrNull() ?: ""
                val hasUnclosedParenInPrevInstruction = lastRawInst.count { it == '(' } > lastRawInst.count { it == ')' }

                if (isKeywordAlreadySeen || hasUnclosedParenInPrevInstruction) {
                    shouldPullBack = false
                }

                if (shouldPullBack) {
                    if (endMarkerLine) {
                        closedState = true
                        val logLock = vocabulary.logOcr3BlockEDispatchLockActivated
                        LogManager.d(TAG, logLock)
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
