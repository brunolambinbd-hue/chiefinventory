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

        val extraVerbs = listOf(
            "plongez", "retirez", "hachez", "ajoutez", "servez", "assaisonnez", "faites", "coupez",
            "mélangez", "préparez", "décorez", "répartissez", "passez", "prélevez", "lavez",
            "mixez", "laissez", "Laissez", "réservez", "poursuivez", "versez", "chauffez", "étalez", "badigeonnez",
            "égouttez", "egouttez", "disposez", "déposez", "deposez", "garnissez", "nappez", "parsemez",
            "enfournez", "mettez", "posez", "étuvez", "Etuvez", "écrasez", "ecrasez", "écalez", "ecalez",
            "extrayez", "nettoyez", "Placez", "Creusez", "obtenez"
        )

        val wineRes = WineParser.loadResources(res)
        val sourceRes = SourceParser.loadResources(res)
        val xmlactionVerbs = res.getStringArray(R.array.step_action_keywords).toList()
        val actionVerbs = (xmlactionVerbs + extraVerbs).distinct()
        val commonIngredients = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        val excludedKeywords = res.getStringArray(R.array.excluded_ocr_keywords).toList()
        val preparationModifiers = res.getStringArray(R.array.ingredient_preparation_modifiers).toList()
        val instructionTriggers = res.getStringArray(R.array.instruction_switch_keywords).toList()

        val ingredientHeaders = listOf("ingrédients", "ingredients", "composition", "ngrédients")
        val instructionHeaders = listOf("préparation", "instructions", "nstructions", "étapes", "réalisation", "méthode", "progression", "cuisson")

        val servingsRegex = Regex("(?i)(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)")
        val alternateServingsRegex = Regex("(?i)(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)")

        // Regex pour Kcal/portion (ex: 500 kcal/por, 500 kilo calories)
        val kcalRegex = Regex("(?i)(\\d+)\\s*(?:kcal|kilo\\s*calories?)(?:/por\\.?)?")
        
        // Regex pour la difficulté (facile, moyen, difficile)
        val difficultyRegex = Regex("(?i)\\b(facile|moyen|difficile|diffcile)\\b")

        // Configuration pour la détection des temps
        val durationPatternStr = "(\\d+\\s*[hH](?:\\s*\\d+)?|\\d+\\s*(?:mn|min|minute|u|h|heure))"
        val timeTypePatternStr = "(préparation|cuisson|repos|prép\\.?|prep\\.?|cuis\\.?|rest\\.?)"
        val timePatternCombined = Regex("(?i)$durationPatternStr\\s*\\+\\s*$durationPatternStr\\s+(?:de\\s+)?cuisson")
        val timePatternPrefix = Regex("(?i)$timeTypePatternStr\\s*:?\\s*(?:de\\s+)?$durationPatternStr")
        val timePatternSuffix = Regex("(?i)$durationPatternStr\\s+(?:de\\s+)?$timeTypePatternStr")

        val qtyRegex = Regex("^(?:[\\d\\-*•¼½¾]|un\\b|une\\b|quelques\\b|plusieurs\\b|un peu\\b|[|Il!](?=[\\s\\d]))", RegexOption.IGNORE_CASE)
        val containsActionRegex = Regex("(?i)\\b(?:${actionVerbs.joinToString("|")})\\b")

        val ingredientConnectors = listOf("de", "du", "des", "d'", "au", "aux", "à")

        for (line in lines) {
            var workingLine = line.trim()
            if (workingLine.isEmpty()) continue

            // 1. PROTECTION DES DIMENSIONS
            if (Ocr2Cleaner.isTechnicalDimension(workingLine)) {
                results.rawInstructionsList.add(workingLine)
                continue
            }

            // 2. CONSOMMATION DES KCAL ET DIFFICULTÉ (Priorité sur les portions pour éviter la confusion)
            val kcalMatch = kcalRegex.find(workingLine)
            if (kcalMatch != null) {
                if (results.detectedKcal == null) {
                    results.detectedKcal = kcalMatch.groupValues[1]
                }
                workingLine = workingLine.replace(kcalMatch.value, "").trim()
            }
            
            val diffMatch = difficultyRegex.find(workingLine)
            if (diffMatch != null) {
                if (results.detectedDifficulty == null) {
                    results.detectedDifficulty = diffMatch.groupValues[1].lowercase().replace("diffcile", "difficile")
                }
                workingLine = workingLine.replace(diffMatch.value, "").trim()
            }

            // 3. CONSOMMATION DES PORTIONS
            val sMatch = servingsRegex.find(workingLine) ?: alternateServingsRegex.find(workingLine)
            if (sMatch != null) {
                if (results.detectedServings == null) {
                    results.detectedServings = sMatch.groupValues[1]
                }
                workingLine = workingLine.replace(sMatch.value, "").trim()
            }

            // 4. CONSOMMATION DES TEMPS
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
                        val type = matchPre.groupValues[1].lowercase()
                        updateTimeResult(results, type, mins)
                        workingLine = workingLine.replace(matchPre.value, "").trim()
                    }
                    matchSuf != null -> {
                        val mins = extractMinutes(matchSuf.groupValues[1])
                        val type = matchSuf.groupValues[2].lowercase()
                        updateTimeResult(results, type, mins)
                        workingLine = workingLine.replace(matchSuf.value, "").trim()
                    }
                    else -> foundTime = false
                }
            }

            // 5. CONSOMMATION DES MOTS-CLÉS D'EXCLUSION
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

            // A. CLASSIFICATION PRÉCOCE
            val weightRegex = Regex("(?i)\\(\\d+\\s*(?:g|kg|ml|cl|l|oz|lb|pcs|pce|un|une)\\)")
            val hasWeight = weightRegex.containsMatchIn(workingLine)
            val hasQuantity = qtyRegex.containsMatchIn(workingLine) ||
                    OcrHelperUtils.countIngredientSequences(workingLine) > 0

            val wordsList = workingLine.split(Regex("\\s+"))
            val isIsolatedModifier = wordsList.size <= 3 && preparationModifiers.any { lowerLine.startsWith(it.lowercase()) }
            val hasInstructionTrigger = instructionTriggers.any { Regex("(?i)\\b${Pattern.quote(it)}\\b").containsMatchIn(workingLine) }

            val looksLikeIngredient = (hasQuantity || hasWeight || isIsolatedModifier ||
                    commonIngredients.any { lowerLine.startsWith(it.lowercase()) }) && !hasInstructionTrigger

            // 6. VIN ET SOURCE
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

            // 7. CONSOMMATION DES HEADERS
            val isInstrHeader = instructionHeaders.any { lowerLine.contains(it) } && workingLine.length < 35 && !containsAction
            val isIngrHeader = ingredientHeaders.any { lowerLine.contains(it) } && workingLine.length < 35 && !containsAction

            if (isInstrHeader) {
                currentSection = SECTION_INSTRUCTIONS
                workingLine = workingLine.replace(Regex("(?i)(?<!\\p{L})(?:${instructionHeaders.joinToString("|")})(?!\\p{L})"), "").trim()
            } else if (isIngrHeader) {
                currentSection = SECTION_INGREDIENTS
                workingLine = workingLine.replace(Regex("(?i)(?<!\\p{L})(?:${ingredientHeaders.joinToString("|")})(?!\\p{L})"), "").trim()
            }
            workingLine = workingLine.replace(Regex("\\s+"), " ").trim()

            // 8. NETTOYAGE DU BRUIT RÉSIDUEL
            if (workingLine.isEmpty() || !workingLine.any { it.isLetter() }) continue

            // 9. CLASSIFICATION FINALE
            val isInstructionSignal = hasInstructionTrigger || (containsAction && (workingLine.length > 25 || startsWithBullet))
            
            if (isInstructionSignal) {
                currentSection = SECTION_INSTRUCTIONS
            } else if (currentSection == SECTION_NONE && (looksLikeIngredient || startsWithConnector)) {
                currentSection = SECTION_INGREDIENTS
            }

            when (currentSection) {
                SECTION_INGREDIENTS -> results.rawIngredientsList.add(workingLine)
                SECTION_INSTRUCTIONS -> {
                    val isStrictIngredient = (looksLikeIngredient || startsWithConnector) && !containsAction && !Ocr2Cleaner.isTechnicalDimension(workingLine) && !hasInstructionTrigger
                    if (isStrictIngredient && workingLine.length < 45) {
                        results.rawIngredientsList.add(workingLine)
                    } else {
                        results.rawInstructionsList.add(workingLine)
                    }
                }
                else -> {
                    if (looksLikeIngredient || startsWithConnector) results.rawIngredientsList.add(workingLine)
                    else results.rawInstructionsList.add(workingLine)
                }
            }
        }
        return results
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
