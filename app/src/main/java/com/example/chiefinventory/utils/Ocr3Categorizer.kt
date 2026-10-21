package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import java.util.regex.Pattern

/**
 * Step 3 of the OCR pipeline: Line categorization.
 * Responsibility: Classify cleaned and normalized lines into specific recipe sections.
 */
data class RawSections(
    val rawIngredientsList: MutableList<String> = mutableListOf(),
    val rawInstructionsList: MutableList<String> = mutableListOf(),
    val detectedWineList: MutableList<String> = mutableListOf(),
    val detectedSourceList: MutableList<String> = mutableListOf(),
    var detectedTitle: String? = null,
    var detectedServings: String? = null,
    var detectedPrepTime: String? = null,
    var detectedCookTime: String? = null,
    var detectedRestingTime: String? = null,
    var detectedKcal: String? = null,    // Kcal par portion
    var detectedDifficulty: String? = null, // Difficulté
    var ingredientKeywords: Set<String> = emptySet()
)

object Ocr3Categorizer {
    private const val TAG = "Ocr3Categorizer"

    internal const val SECTION_NONE = 0
    internal const val SECTION_INGREDIENTS = 1
    internal const val SECTION_INSTRUCTIONS = 2

    /**
     * Classifie les lignes dans les différentes sections de la recette en utilisant [Resources].
     */
    fun categorize(lines: List<String>, res: Resources): RawSections {
        return categorize(lines, OcrVocabulary.fromResources(res))
    }

    /**
     * Classifie les lignes dans les différentes sections de la recette en utilisant [OcrVocabulary].
     * Cette version permet un test unitaire direct sans dépendance au framework Android.
     */
    fun categorize(lines: List<String>, vocabulary: OcrVocabulary): RawSections {
        val results = RawSections()
        var currentSection = SECTION_NONE
        var isIngredientListClosed = false
        val wineState = Ocr3CategorizerWine.WineState()
        val seenIngredientKeywords = mutableSetOf<String>()

        Log.d(TAG, "--- DÉBUT CATÉGORISATION ---")

        val actionVerbs = vocabulary.stepActionKeywords.distinct()
        val commonIngredients = vocabulary.commonIngredientsNoQty
        val excludedKeywords = vocabulary.excludedOcrKeywords
        val preparationModifiers = vocabulary.ingredientPreparationModifiers
        val instructionTriggers = vocabulary.instructionSwitchKeywords
        val semanticExclusions = vocabulary.ingredientSemanticExclusions
        val endMarkers = vocabulary.ingredientEndMarkers
        val subHeaders = vocabulary.ingredientSubHeaders

        val ingredientHeaders = listOf("ingrédients", "ingredients", "composition", "ngrédients")
        val instructionHeaders = listOf("préparation", "instructions", "nstructions", "étapes", "réalisation", "méthode", "progression", "cuisson")

        val servingsRegex = Regex("(?i)(?:pour\\s+)?(\\d+)\\s*(?:personnes?|portions?|pers\\.?|servings?)|(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)(?:\\s*(?:personnes?|portions?|pers\\.?|servings?))?|\\bPOUR\\s+(\\d+)\\b")
        val kcalRegex = Regex("(?i)(\\d+)\\s*(?:kcal|kilo\\s*calories?)(?:/por\\.?)?")
        val difficultyRegex = Regex("(?i)\\b(facile|moyen|difficile|diffcile)\\b")

        val durationPatternStr = "(\\d+\\s*[hH](?:\\s*\\d+)?|\\d+\\s*(?:mn|min|minute|u|h|heure))"
        val timeTypePatternStr = "(préparation|cuisson|repos|prép\\.?|prep\\.?|cuis\\.?|rest\\.?)"
        val timePatternCombined = Regex("(?i)$durationPatternStr\\s*\\+\\s*$durationPatternStr\\s+(?:de\\s+)?cuisson")
        val timePatternPrefix = Regex("(?i)$timeTypePatternStr\\s*:?\\s*(?:de\\s+)?$durationPatternStr")
        val timePatternSuffix = Regex("(?i)$durationPatternStr\\s+(?:de\\s+)?$timeTypePatternStr")

        val hardQtyRegex = Regex("^[\\d¼½¾]|[|Il!](?=[\\s\\d])|\\d+/\\d+", RegexOption.IGNORE_CASE)
        val qtyRegex = Regex("^(?:[\\d\\-*•¼½¾]|un\\b|une\\b|des\\b|du\\b|de la\\b|de l'|le\\b|la\\b|les\\b|l['’]|quelques\\b|plusieurs\\b|un\\s+peu\\b)", RegexOption.IGNORE_CASE)

        val containsActionRegex = if (actionVerbs.isEmpty()) null else Regex("(?i)\\b(?:${actionVerbs.joinToString("|")})\\b")
        val ingredientConnectors = listOf("de", "du", "des", "d'", "au", "aux", "à")

        for (line in lines) {
            var workingLine = line.trim()
            if (workingLine.isEmpty()) continue

            if (Ocr2Cleaner.isTechnicalDimension(workingLine)) {
                results.rawInstructionsList.add(workingLine)
                continue
            }

            // EXTRACTION MÉTADONNÉES
            val kcalMatch = kcalRegex.find(workingLine)
            if (kcalMatch != null) {
                if (results.detectedKcal == null) results.detectedKcal = kcalMatch.groupValues[1]
                workingLine = workingLine.replace(kcalMatch.value, "").trim()
            }
            val diffMatch = difficultyRegex.find(workingLine)
            if (diffMatch != null) {
                if (results.detectedDifficulty == null) results.detectedDifficulty = diffMatch.groupValues[1].lowercase().replace("diffcile", "difficile")
                workingLine = workingLine.replace(diffMatch.value, "").trim()
            }
            val sMatch = servingsRegex.find(workingLine)
            if (sMatch != null) {
                if (results.detectedServings == null) {
                    results.detectedServings = when {
                        sMatch.groupValues[1].isNotEmpty() -> sMatch.groupValues[1]
                        sMatch.groupValues[2].isNotEmpty() -> sMatch.groupValues[2]
                        else -> sMatch.groupValues[3]
                    }
                }
                workingLine = workingLine.replace(sMatch.value, "").trim()
            }
            var foundTime = true
            while (foundTime) {
                val matchComb = timePatternCombined.find(workingLine)
                val matchPre = timePatternPrefix.find(workingLine)
                val matchSuf = timePatternSuffix.find(workingLine)
                when {
                    matchComb != null -> {
                        results.detectedPrepTime = extractMinutes(matchComb.groupValues[1])
                        results.detectedCookTime = extractMinutes(matchComb.groupValues[2])
                        workingLine = workingLine.replace(matchComb.value, "").trim()
                    }
                    matchPre != null -> {
                        val mins = extractMinutes(matchPre.groupValues[2])
                        updateTimeResult(results, matchPre.groupValues[1].lowercase(), mins)
                        workingLine = workingLine.replace(matchPre.value, "").trim()
                    }
                    matchSuf != null -> {
                        val mins = extractMinutes(matchSuf.groupValues[1])
                        updateTimeResult(results, matchSuf.groupValues[2].lowercase(), mins)
                        workingLine = workingLine.replace(matchSuf.value, "").trim()
                    }
                    else -> foundTime = false
                }
            }

            var lowerLine = workingLine.lowercase()
            var containsAction = containsActionRegex?.containsMatchIn(workingLine) ?: false
            var startsWithBullet = workingLine.startsWith("•") || workingLine.startsWith("-") || workingLine.startsWith("*")
            val startsWithConnector = ingredientConnectors.any { lowerLine.startsWith("$it ") || (it.endsWith("'") && lowerLine.startsWith(it)) }

            // 8.5 RATTRAPAGE IMMÉDIAT DE CONTINUATION D'INGRÉDIENT
            val connectorsEnding = listOf(" de", " d'", " du", " des", " ou", " au", " aux")
            val lastRawIng = results.rawIngredientsList.lastOrNull()?.lowercase()?.trim() ?: ""
            val isContinuationOfHangingIng = currentSection == SECTION_INGREDIENTS && connectorsEnding.any { lastRawIng.endsWith(it) }
            if (isContinuationOfHangingIng && !containsAction && !startsWithBullet) {
                val lastIdx = results.rawIngredientsList.lastIndex
                results.rawIngredientsList[lastIdx] = "${results.rawIngredientsList[lastIdx].trim()} $workingLine"
                val keywords = extractKeywordsFromText(workingLine, null)
                seenIngredientKeywords.addAll(keywords)
                Log.d(TAG, "  [FUSION CONTINUATION INGR] Appended: '$workingLine' -> New line: '${results.rawIngredientsList[lastIdx]}'")
                continue
            }

            // DÉTECTION VIN
            if (Ocr3CategorizerWine.processWineLine(
                    workingLine, currentSection, isIngredientListClosed,
                    containsAction, startsWithConnector, vocabulary, wineState
                )) {
                continue
            }

            if (!containsAction && !startsWithConnector) {
                if (OcrHelperUtils.isLikelyProperNameOrSource(workingLine) || SourceParser.isSourceLine(workingLine, vocabulary)) {
                    results.detectedSourceList.add(SourceParser.cleanSourceLine(workingLine, vocabulary))
                    continue
                }
            }

            for (word in excludedKeywords) {
                val pattern = Regex("(?i)(?<!\\p{L})${Pattern.quote(word)}(?!\\p{L})")
                workingLine = pattern.replace(workingLine, "").trim()
            }
            workingLine = workingLine.replace(Regex("\\s+"), " ").trim()
            if (workingLine.isEmpty()) continue

            lowerLine = workingLine.lowercase()
            containsAction = containsActionRegex?.containsMatchIn(workingLine) ?: false
            startsWithBullet = workingLine.startsWith("•") || workingLine.startsWith("-") || workingLine.startsWith("*")

            val weightRegex = Regex("(?i)\\(\\d+\\s*(?:g|kg|ml|cl|l|oz|lb|pcs|pce|un|une)\\)")
            val hasWeight = weightRegex.containsMatchIn(workingLine)
            val hasHardQuantity = hardQtyRegex.containsMatchIn(workingLine) || OcrHelperUtils.countIngredientSequences(workingLine) > 0
            val hasQuantity = qtyRegex.containsMatchIn(workingLine) || hasHardQuantity

            // --- ANALYSE RÉAPPARITION AVEC LOGS DÉTAILLÉS ---
            var matchedKeyword: String? = null
            Log.d(TAG, "LIGNE: '$workingLine' | Déjà inventorisés: $seenIngredientKeywords")
            val isReappearance = !startsWithConnector && seenIngredientKeywords.any { keyword ->
                val pattern = Regex("(?<!\\p{L})${Regex.escape(keyword)}(?!\\p{L})", RegexOption.IGNORE_CASE)
                val match = pattern.containsMatchIn(workingLine)
                if (match) {
                    matchedKeyword = keyword
                    val finalDecision = isIngredientListClosed || (!hasHardQuantity && !hasWeight)
                    Log.d(TAG, "  ? Comparaison: '$keyword' -> MATCH=true | hardQty=$hasHardQuantity | weight=$hasWeight | closed=$isIngredientListClosed | final=$finalDecision")
                }
                val allowWithQty = isIngredientListClosed
                match && (allowWithQty || (!hasHardQuantity && !hasWeight))
            }

            val isIsolatedModifier = preparationModifiers.any { mod ->
                val lowerMod = mod.lowercase()
                lowerLine == lowerMod ||
                        lowerLine.startsWith("$lowerMod ") ||
                        lowerLine.startsWith("${lowerMod}d'") ||
                        lowerLine.startsWith("${lowerMod}de") ||
                        lowerLine.startsWith("${lowerMod}du") ||
                        lowerLine.startsWith("${lowerMod}des")
            }
            val hasInstructionTrigger = instructionTriggers.any { Regex("(?i)\\b${Pattern.quote(it)}\\b").containsMatchIn(workingLine) }
            val isSubHeader = subHeaders.any { it.equals(workingLine, ignoreCase = true) }

            val looksLikeIngredient = (hasQuantity || hasWeight || isIsolatedModifier ||
                    commonIngredients.any { lowerLine.startsWith(it.lowercase()) }) && !hasInstructionTrigger && !isReappearance



            val isInstrHeader = (instructionHeaders.any { lowerLine.contains(it) } || instructionTriggers.any { lowerLine == it.lowercase() }) && workingLine.length < 35 && !containsAction
            val isIngrHeader = (lowerLine.contains("ingrédients") || lowerLine.contains("composition")) && workingLine.length < 35 && !containsAction

            when {
                isInstrHeader -> currentSection = SECTION_INSTRUCTIONS
                isIngrHeader || isSubHeader -> {
                    currentSection = SECTION_INGREDIENTS
                    if (isIngrHeader) seenIngredientKeywords.clear()
                    if (isSubHeader) isIngredientListClosed = false
                }
            }
            workingLine = workingLine.replace(Regex("\\s+"), " ").trim()

            workingLine = OcrHelperUtils.cleanIngredientSemantics(workingLine, excludedKeywords, semanticExclusions)
            if (workingLine.isEmpty() || !workingLine.any { it.isLetter() }) continue

            // Ignorer le bruit de pied de page / étiquette de bouteille après fermeture de la section vin
            if (wineState.isWineSectionClosed && !containsAction && !startsWithBullet) continue

            val isInstructionSignal = hasInstructionTrigger || isReappearance || (containsAction && (workingLine.length > 25 || startsWithBullet))

            Log.d(TAG, "  => BILAN: reappear=$isReappearance | instrSignal=$isInstructionSignal | currentSec=$currentSection")

            // 9. CLASSIFICATION FINALE
            when {
                isInstructionSignal || (isIngredientListClosed && !hasHardQuantity && !hasWeight) -> currentSection = SECTION_INSTRUCTIONS
                currentSection == SECTION_NONE && (looksLikeIngredient || startsWithConnector) -> currentSection = SECTION_INGREDIENTS
            }

            val parsed = IngredientParser.parse(workingLine)

            when (currentSection) {
                SECTION_INGREDIENTS -> {
                    if (!containsAction && ((lowerLine.contains("sel") && lowerLine.contains("poivre")) || lowerLine.contains("sel, poivre") || lowerLine.contains("sel et poivre"))) {
                        isIngredientListClosed = true
                        Log.d(TAG, "  [!] VERROU ACTIVÉ (Marqueur de fin sel/poivre détecté)")
                    }
                    results.rawIngredientsList.add(workingLine)
                    val keywords = extractKeywordsFromText(parsed.name, parsed.supplementalInfo)
                        .filter { kw -> preparationModifiers.none { mod -> mod.equals(kw, ignoreCase = true) } }
                    seenIngredientKeywords.addAll(keywords)
                    Log.d(TAG, "  [INGR] Split: Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}', Info='${parsed.supplementalInfo}' | Keywords added: $keywords")
                }
                SECTION_INSTRUCTIONS -> {
                    val isStrictIngredient = (looksLikeIngredient || startsWithConnector) && !containsAction && !Ocr2Cleaner.isTechnicalDimension(workingLine) && !hasInstructionTrigger
                    val isEndMarkerLine = (lowerLine.contains("sel") && lowerLine.contains("poivre")) || lowerLine == "sel, poivre" || lowerLine == "sel et poivre" || lowerLine == "sel" || lowerLine == "poivre"
                    val shouldPullBack = when {
                        isReappearance -> false
                        isEndMarkerLine && !containsAction -> true
                        isIngredientListClosed -> isStrictIngredient && hasWeight
                        else -> isStrictIngredient && (hasHardQuantity || hasWeight)
                    }

                    if (shouldPullBack) {
                        if (isEndMarkerLine && !containsAction) {
                            isIngredientListClosed = true
                            Log.d(TAG, "  [!] VERROU ACTIVÉ DEPUIS RATTRAPAGE (Marqueur de fin détecté)")
                        }
                        results.rawIngredientsList.add(workingLine)
                        seenIngredientKeywords.addAll(extractKeywordsFromText(parsed.name, parsed.supplementalInfo))
                        Log.d(TAG, "  [RATTRAPAGE] Split: Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}', Info='${parsed.supplementalInfo}'")
                    } else {
                        results.rawInstructionsList.add(workingLine)
                    }
                }
                else -> {
                    if (looksLikeIngredient || startsWithConnector) {
                        results.rawIngredientsList.add(workingLine)
                        seenIngredientKeywords.addAll(extractKeywordsFromText(parsed.name, parsed.supplementalInfo))
                        Log.d(TAG, "  [DEFAUT] Split: Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}', Info='${parsed.supplementalInfo}'")
                    } else {
                        results.rawInstructionsList.add(workingLine)
                    }
                }
            }
        }
        results.detectedWineList.addAll(wineState.detectedWineList)
        results.ingredientKeywords = seenIngredientKeywords
        Log.d(TAG, "--- FIN CATÉGORISATION ---")
        return results
    }

    private fun extractKeywordsFromText(name: String, supplementalInfo: String?): List<String> {
        val stopWords = setOf("dans", "avec", "pour", "plus", "moins", "vers", "sous", "sur", "chez", "entre", "faire", "faites", "votre", "quelques", "plusieurs")
        // "jus" et "zeste" sont maintenant gérés par supplementalInfo dans IngredientParser et sont des mots-clés utiles pour la détection de réapparition
        val fillers = setOf("morceau", "morceaux", "tranche", "tranches", "de", "le", "la", "les", "des", "du", "un", "une")
        val units = setOf("grammes", "kilos", "litres", "centilitres", "millilitres", "cuillere", "soupe", "cafe", "pincee", "gousse", "boite", "boites", "pot", "pots", "cl", "ml", "kg")
        
        val combinedText = if (supplementalInfo != null) "$name $supplementalInfo" else name
        return combinedText.lowercase().split(Regex("[\\s,.'’()\\-*•/0-9]+")).filter { it.length >= 3 && it !in stopWords && it !in units && it !in fillers }
    }

    private fun updateTimeResult(results: RawSections, type: String, mins: String?) {
        when {
            type.startsWith("prép") || type.startsWith("prep") -> results.detectedPrepTime = mins
            type.startsWith("cuis") || type.startsWith("cuisson") -> results.detectedCookTime = mins
            type.startsWith("re") -> results.detectedRestingTime = mins
        }
    }

    private fun extractMinutes(timeStr: String): String? {
        val hPattern = Regex("(\\d+)\\s*[hH]\\s*(\\d*)")
        val mPattern = Regex("(\\d+)\\s*(?:mn|min|minute|u)")
        hPattern.find(timeStr)?.let {
            val h = it.groupValues[1].toIntOrNull() ?: 0
            val m = it.groupValues[2].toIntOrNull() ?: 0
            return (h * 60 + m).toString()
        }
        mPattern.find(timeStr)?.let { return it.groupValues[1] }
        return Regex("(\\d+)").find(timeStr)?.groupValues?.get(1)
    }
}
