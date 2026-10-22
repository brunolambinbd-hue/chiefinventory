package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import java.util.regex.Pattern

/**
 * Conteneur de données regroupant les sections brutes catégorisées et méta-données extraites lors de l'Étape 3.
 *
 * @property rawIngredientsList Liste mutable des lignes catégorisées comme ingrédients bruts.
 * @property rawInstructionsList Liste mutable des lignes catégorisées comme consignes d'instructions brutes.
 * @property detectedWineList Liste mutable des recommandations de vins extraites.
 * @property detectedSourceList Liste mutable des provenances/sources extraites.
 * @property detectedTitle Le titre de la recette s'il est détecté à cette étape, ou null.
 * @property detectedServings Le nombre de personnes/portions extrait (ex: "4").
 * @property detectedPrepTime Le temps de préparation extrait en minutes (ex: "15").
 * @property detectedCookTime Le temps de cuisson extrait en minutes (ex: "45").
 * @property detectedRestingTime Le temps de repos extrait en minutes (ex: "30").
 * @property detectedKcal Les kilocalories par portion extraites (ex: "250").
 * @property detectedDifficulty La difficulté extraite (ex: "Facile").
 * @property ingredientKeywords L'ensemble des mots-clés d'ingrédients inventoriés pour le suivi des réapparitions.
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
    var detectedKcal: String? = null,
    var detectedDifficulty: String? = null,
    var detectedNutritionInfo: String? = null,
    var ingredientKeywords: Set<String> = emptySet()
)

/**
 * Étape 3 du pipeline OCR : Catégorisation intelligente et Machine à États.
 *
 * Responsabilités :
 * - Répartir les lignes de texte nettoyées dans leurs sections respectives (Ingrédients, Instructions, Vin, Méta-données).
 * - Basculer dynamiquement l'état de section via [Ocr3CategorizerHeader].
 * - Extraire automatiquement les méta-données (Portions, Temps, Kcal, Difficulté).
 * - Détecter et isoler la section vin via [Ocr3CategorizerWine].
 * - Fusionner à la volée les quantités/unités inachevées via [Ocr3CategorizerUnfinishedIngredient].
 * - Verrouiller la section ingrédients lors de la rencontre du marqueur fin (ex: "sel et poivre").
 */
object Ocr3Categorizer {
    private const val TAG = "Ocr3Categorizer"

    internal const val SECTION_NONE = 0
    internal const val SECTION_INGREDIENTS = 1
    internal const val SECTION_INSTRUCTIONS = 2

    /**
     * Classifie les lignes dans les différentes sections de la recette en utilisant les ressources [Resources] Android.
     *
     * @param lines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param res Les ressources de l'application Android permettant de charger [OcrVocabulary].
     * @return L'objet [RawSections] contenant les listes brutes catégorisées et méta-données.
     */
    fun categorize(lines: List<String>, res: Resources): RawSections {
        return categorize(lines, OcrVocabulary.fromResources(res))
    }

    /**
     * Classifie les lignes dans les différentes sections de la recette en utilisant le vocabulaire métier [OcrVocabulary] (TDD/Tests).
     *
     * @param lines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param vocabulary Le vocabulaire métier data-driven contenant les dictionnaires XML du projet.
     * @return L'objet [RawSections] contenant les listes brutes catégorisées et méta-données.
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
        val instructionHeaders = listOf("préparation", "preparation", "instructions", "nstructions", "étapes", "réalisation", "méthode", "progression", "cuisson")

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

            // 1. DÉTECTION ET EXTRACTION DES MÉTA-DONNÉES (RecipeMetadataParser)
            val metaResult = RecipeMetadataParser.extractMetadata(workingLine, results)
            workingLine = metaResult.remainingLine
            if (metaResult.isPureMetadata) continue

            var lowerLine = workingLine.lowercase()
            var containsAction = containsActionRegex?.containsMatchIn(workingLine) ?: false
            var startsWithBullet = workingLine.startsWith("•") || workingLine.startsWith("-") || workingLine.startsWith("*")
            val startsWithConnector = ingredientConnectors.any { lowerLine.startsWith("$it ") || (it.endsWith("'") && lowerLine.startsWith(it)) }

            // 8.5 RATTRAPAGE IMMÉDIAT DE CONTINUATION DE PHRASE SUSPENDUE (Ingrédients ou Instructions)
            val baseConnectors = listOf(" de", " d'", " du", " des", " ou", " au", " aux", " avec", " dans", " pour", " sur", " en", " par", " la", " le", " les")
            val allConnectorsEnding = (baseConnectors + vocabulary.hangingConnectors.map { " ${it.trim().lowercase()}" }).distinct()

            val targetListForContinuation = if (currentSection == SECTION_INGREDIENTS) results.rawIngredientsList else results.rawInstructionsList
            val lastRawLine = targetListForContinuation.lastOrNull()?.lowercase()?.trim() ?: ""
            val isContinuationOfHangingLine = targetListForContinuation.isNotEmpty() && allConnectorsEnding.any { lastRawLine.endsWith(it) }

            if (isContinuationOfHangingLine && !containsAction && !startsWithBullet) {
                val lastIdx = targetListForContinuation.lastIndex
                targetListForContinuation[lastIdx] = "${targetListForContinuation[lastIdx].trim()} $workingLine"
                if (currentSection == SECTION_INGREDIENTS) {
                    val keywords = extractKeywordsFromText(workingLine, null)
                    seenIngredientKeywords.addAll(keywords)
                }
                Log.d(TAG, "  [FUSION CONTINUATION] Appended: '$workingLine' -> New line: '${targetListForContinuation[lastIdx]}'")
                continue
            }

            // 8.6 RATTRAPAGE IMMÉDIAT D'UNITÉ D'INGRÉDIENT INCOMPLÈTE (ex: "1 cuillère à soupe" + "de moutarde")
            if (Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
                    workingLine, currentSection, containsAction, startsWithBullet, vocabulary,
                    results.rawIngredientsList, seenIngredientKeywords
                )) {
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
                if (OcrHelperUtils.isLikelyProperNameOrSource(workingLine, vocabulary) || SourceParser.isSourceLine(workingLine, vocabulary)) {
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
                    val isCommonIngredient = commonIngredients.any { lowerLine.contains(it.lowercase()) }
                    val shouldPullBack = when {
                        isReappearance -> false
                        isEndMarkerLine && !containsAction -> true
                        isIngredientListClosed -> (isStrictIngredient && hasWeight) || (isCommonIngredient && !containsAction && !startsWithBullet)
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

    /**
     * Extrait les mots-clés d'ingrédients pertinents à partir du nom nettoyé et de l'information supplémentaire.
     *
     * @param name Le nom principal de l'ingrédient.
     * @param supplementalInfo L'information supplémentaire associée.
     * @return La liste des mots-clés significatifs de longueur >= 3.
     */
    private fun extractKeywordsFromText(name: String, supplementalInfo: String?): List<String> {
        val stopWords = setOf("dans", "avec", "pour", "plus", "moins", "vers", "sous", "sur", "chez", "entre", "faire", "faites", "votre", "quelques", "plusieurs")
        val fillers = setOf("morceau", "morceaux", "tranche", "tranches", "de", "le", "la", "les", "des", "du", "un", "une")
        val units = setOf("grammes", "kilos", "litres", "centilitres", "millilitres", "cuillere", "soupe", "cafe", "pincee", "gousse", "boite", "boites", "pot", "pots", "cl", "ml", "kg")
        
        val combinedText = if (supplementalInfo != null) "$name $supplementalInfo" else name
        return combinedText.lowercase().split(Regex("[\\s,.'’()\\-*•/0-9]+")).filter { it.length >= 3 && it !in stopWords && it !in units && it !in fillers }
    }

}
