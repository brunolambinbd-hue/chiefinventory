package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R
import java.util.regex.Pattern
import kotlin.collections.distinct
import kotlin.collections.plus

/**
 * Step 3 of the OCR pipeline: Line categorization.
 * Responsibility: Classify cleaned and normalized lines into specific recipe sections.
 */
data class RawSections(
    val rawIngredientsList: MutableList<String> = mutableListOf(),
    val rawInstructionsList: MutableList<String> = mutableListOf(),
    val detectedWineList: MutableList<String> = mutableListOf(),
    val detectedSourceList: MutableList<String> = mutableListOf(),
    var detectedServings: String? = null,
    var detectedPrepTime: String? = null,
    var detectedCookTime: String? = null,
    var detectedRestingTime: String? = null,
    var detectedKcal: String? = null,    // Kcal par portion
    var detectedDifficulty: String? = null // Difficulté
)

object Ocr3Categorizer {
    private const val TAG = "RecipeOCR"

    private const val SECTION_NONE = 0
    private const val SECTION_INGREDIENTS = 1
    private const val SECTION_INSTRUCTIONS = 2

    /**
     * Classifie les lignes dans les différentes sections de la recette.
     */
    fun categorize(lines: List<String>, res: Resources): RawSections {
        val results = RawSections()
        var currentSection = SECTION_NONE
        var isIngredientListClosed = false
        val seenIngredientKeywords = mutableSetOf<String>()

        // Chargement des ressources XML
        val xmlactionVerbs = res.getStringArray(R.array.step_action_keywords).toList()
        val actionVerbs = xmlactionVerbs.distinct()
        val commonIngredients = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        val excludedKeywords = res.getStringArray(R.array.excluded_ocr_keywords).toList()
        val preparationModifiers = res.getStringArray(R.array.ingredient_preparation_modifiers).toList()
        val instructionTriggers = res.getStringArray(R.array.instruction_switch_keywords).toList()
        val semanticExclusions = res.getStringArray(R.array.ingredient_semantic_exclusions).toList()
        val endMarkers = res.getStringArray(R.array.ingredient_end_markers).toList()
        val subHeaders = res.getStringArray(R.array.ingredient_sub_headers).toList()

        val wineRes = WineParser.loadResources(res)
        val sourceRes = SourceParser.loadResources(res)

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

        // Séparation des détections de quantité : Forte (chiffres/fractions) vs Faible (articles)
        val hardQtyRegex = Regex("^[\\d¼½¾]|[|Il!](?=[\\s\\d])|\\d+/\\d+", RegexOption.IGNORE_CASE)
        val qtyRegex = Regex("^(?:[\\d\\-*•¼½¾]|un\\b|une\\b|des\\b|du\\b|de la\\b|de l'|le\\b|la\\b|les\\b|l['’]|quelques\\b|plusieurs\\b|un peu\\b|[|Il!](?=[\\s\\d]))", RegexOption.IGNORE_CASE)
        
        val containsActionRegex = Regex("(?i)\\b(?:${actionVerbs.joinToString("|")})\\b")
        val ingredientConnectors = listOf("de", "du", "des", "d'", "au", "aux", "à")

        for (line in lines) {
            var workingLine = line.trim()
            if (workingLine.isEmpty()) continue

            if (Ocr2Cleaner.isTechnicalDimension(workingLine)) {
                results.rawInstructionsList.add(workingLine)
                continue
            }

            // Extraction métadonnées
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

            for (word in excludedKeywords) {
                val pattern = Regex("(?i)(?<!\\p{L})${Pattern.quote(word)}(?!\\p{L})")
                workingLine = pattern.replace(workingLine, "").trim()
            }
            workingLine = workingLine.replace(Regex("\\s+"), " ").trim()
            if (workingLine.isEmpty()) continue

            val lowerLine = workingLine.lowercase()
            val containsAction = containsActionRegex.containsMatchIn(workingLine)
            val startsWithBullet = workingLine.startsWith("•") || workingLine.startsWith("-") || workingLine.startsWith("*")
            val startsWithConnector = ingredientConnectors.any { lowerLine.startsWith("$it ") || (it.endsWith("'") && lowerLine.startsWith(it)) }

            val weightRegex = Regex("(?i)\\(\\d+\\s*(?:g|kg|ml|cl|l|oz|lb|pcs|pce|un|une)\\)")
            val hasWeight = weightRegex.containsMatchIn(workingLine)
            
            val hasHardQuantity = hardQtyRegex.containsMatchIn(workingLine) || OcrHelperUtils.countIngredientSequences(workingLine) > 0
            val hasQuantity = qtyRegex.containsMatchIn(workingLine) || hasHardQuantity

            // Détection de réapparition (Uniquement si pas de quantité forte associée)
            val isReappearance = seenIngredientKeywords.any { word ->
                lowerLine.contains(Regex("\\b${Regex.escape(word)}\\b")) && !hasHardQuantity && !hasWeight
            }

            val isIsolatedModifier = preparationModifiers.any { it.equals(workingLine, ignoreCase = true) }
            val hasInstructionTrigger = instructionTriggers.any { Regex("(?i)\\b${Pattern.quote(it)}\\b").containsMatchIn(workingLine) }
            val isSubHeader = subHeaders.any { it.equals(workingLine, ignoreCase = true) }

            val looksLikeIngredient = (hasQuantity || hasWeight || isIsolatedModifier ||
                    commonIngredients.any { lowerLine.startsWith(it.lowercase()) }) && !hasInstructionTrigger && !isReappearance

            if (!containsAction && !startsWithConnector) {
                val isStrictIngredient = looksLikeIngredient && (currentSection == SECTION_INGREDIENTS || startsWithBullet)
                if (!isStrictIngredient && WineParser.isWineLine(workingLine, wineRes)) {
                    results.detectedWineList.add(WineParser.cleanWineLine(workingLine, wineRes))
                    continue
                }
                if (OcrHelperUtils.isLikelyProperNameOrSource(workingLine) || SourceParser.isSourceLine(workingLine, sourceRes)) {
                    results.detectedSourceList.add(SourceParser.cleanSourceLine(workingLine, sourceRes))
                    continue
                }
            }

            val isInstrHeader = instructionHeaders.any { lowerLine.contains(it) } && workingLine.length < 35 && !containsAction
            val isIngrHeader = ingredientHeaders.any { lowerLine.contains(it) } && workingLine.length < 35 && !containsAction

            if (isInstrHeader) {
                currentSection = SECTION_INSTRUCTIONS
                workingLine = workingLine.replace(Regex("(?i)(?<!\\p{L})(?:${instructionHeaders.joinToString("|")})(?!\\p{L})"), "").trim()
            } else if (isIngrHeader || isSubHeader) {
                currentSection = SECTION_INGREDIENTS
                if (isSubHeader) isIngredientListClosed = false
                workingLine = workingLine.replace(Regex("(?i)(?<!\\p{L})(?:${ingredientHeaders.joinToString("|")})(?!\\p{L})"), "").trim()
            }
            workingLine = workingLine.replace(Regex("\\s+"), " ").trim()

            workingLine = OcrHelperUtils.cleanIngredientSemantics(workingLine, excludedKeywords, semanticExclusions)
            if (workingLine.isEmpty() || !workingLine.any { it.isLetter() }) continue

            val isInstructionSignal = hasInstructionTrigger || isReappearance || (containsAction && (workingLine.length > 25 || startsWithBullet))

            if (isIngredientListClosed && !hasHardQuantity && !hasWeight) {
                currentSection = SECTION_INSTRUCTIONS
            } else if (isInstructionSignal) {
                currentSection = SECTION_INSTRUCTIONS
            } else if (currentSection == SECTION_NONE && (looksLikeIngredient || startsWithConnector)) {
                currentSection = SECTION_INGREDIENTS
            }

            when (currentSection) {
                SECTION_INGREDIENTS -> {
                    val foundMarkers = endMarkers.count { lowerLine.contains(it) }
                    if (!containsAction && (foundMarkers >= 2 || (lowerLine.contains("poivre") && workingLine.length < 20))) {
                        isIngredientListClosed = true
                    }
                    results.rawIngredientsList.add(workingLine)
                    seenIngredientKeywords.addAll(extractIngredientKeywords(workingLine))
                }
                SECTION_INSTRUCTIONS -> {
                    val isStrictIngredient = (looksLikeIngredient || startsWithConnector) && !containsAction && !Ocr2Cleaner.isTechnicalDimension(workingLine) && !hasInstructionTrigger
                    val shouldPullBack = if (isIngredientListClosed) (isStrictIngredient && (hasHardQuantity || hasWeight)) else (isStrictIngredient && workingLine.length < 45)

                    if (shouldPullBack) {
                        results.rawIngredientsList.add(workingLine)
                        seenIngredientKeywords.addAll(extractIngredientKeywords(workingLine))
                    } else {
                        results.rawInstructionsList.add(workingLine)
                    }
                }
                else -> {
                    if (looksLikeIngredient || startsWithConnector) {
                        results.rawIngredientsList.add(workingLine)
                        seenIngredientKeywords.addAll(extractIngredientKeywords(workingLine))
                    } else {
                        results.rawInstructionsList.add(workingLine)
                    }
                }
            }
        }
        return results
    }

    private fun extractIngredientKeywords(line: String): List<String> {
        val stopWords = setOf("dans", "avec", "pour", "plus", "moins", "vers", "sous", "sur", "chez", "entre", "faire", "faites")
        val units = setOf("grammes", "kilos", "litres", "centilitres", "millilitres", "cuillere", "soupe", "cafe", "pincee", "gousse", "boite", "boites", "pot", "pots")
        return line.lowercase().split(Regex("[\\s,.'’()\\-*•/0-9]+")).filter { it.length >= 4 && it !in stopWords && it !in units }
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
