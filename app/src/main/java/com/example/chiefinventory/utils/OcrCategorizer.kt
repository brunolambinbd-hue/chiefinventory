package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.example.chiefinventory.R
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
    var detectedRestingTime: String? = null
)

object OcrCategorizer {
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
            "enfournez", "mettez", "posez", "étuvez", "Etuvez", "écrasez", "ecrasez", "écalez", "ecalez", "extrayez", "nettoyez", "Placez"
        )

        val wineRes = WineParser.loadResources(res)
        val sourceRes = SourceParser.loadResources(res)
        val xmlactionVerbs = res.getStringArray(R.array.step_action_keywords).toList()
        val actionVerbs = (xmlactionVerbs + extraVerbs).distinct()
        val commonIngredients = res.getStringArray(R.array.common_ingredients_no_qty).toList()
        val excludedKeywords = res.getStringArray(R.array.excluded_ocr_keywords).toList()

        val ingredientHeaders = listOf("ingrédients", "ingredients", "composition", "ngrédients")
        val instructionHeaders = listOf("préparation", "instructions", "nstructions", "étapes", "réalisation", "méthode", "progression", "cuisson")

        val servingsRegex = Regex("(?i)(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)")
        val alternateServingsRegex = Regex("(?i)(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)")
        
        val qtyRegex = Regex("^(?:[\\d\\-*•¼½¾]|un\\b|une\\b|[|Il!](?=[\\s\\d]))", RegexOption.IGNORE_CASE)
        val containsActionRegex = Regex("(?i)\\b(?:${actionVerbs.joinToString("|")})\\b")
        
        val ingredientConnectors = listOf("de", "du", "des", "d'", "au", "aux", "à")

        for (line in lines) {
            var workingLine = line.trim()
            if (workingLine.isEmpty()) continue

            val containsAction = containsActionRegex.containsMatchIn(workingLine)
            val startsWithBullet = workingLine.startsWith("•") || workingLine.startsWith("-") || workingLine.startsWith("*")
            val lowerLine = workingLine.lowercase()
            val startsWithConnector = ingredientConnectors.any { lowerLine.startsWith("$it ") || (it.endsWith("'") && lowerLine.startsWith(it)) }

            // 1. PROTECTION DES DIMENSIONS
            if (OcrCleaner.isTechnicalDimension(workingLine)) {
                results.rawInstructionsList.add(workingLine)
                continue
            }

            // 2. CONSOMMATION DES PORTIONS
            val sMatch = servingsRegex.find(workingLine) ?: alternateServingsRegex.find(workingLine)
            if (sMatch != null) {
                if (results.detectedServings == null) {
                    results.detectedServings = if (sMatch.groupValues[1].any { it.isDigit() }) sMatch.groupValues[1] else sMatch.groupValues[2]
                }
                workingLine = workingLine.replace(sMatch.value, "").trim()
            }

            // 3. CONSOMMATION DES TEMPS
            val timePattern = Regex("(?i)(préparation|cuisson|repos|prep\\.?|cuis\\.?|rest\\.?)\\s*:?\\s*(\\d+\\s*(?:mn|min|h|heure|u))")
            var tMatch = timePattern.find(workingLine)
            while (tMatch != null) {
                val type = tMatch.groupValues[1].lowercase()
                val mins = extractMinutes(tMatch.groupValues[2])
                when {
                    type.startsWith("prép") || type.startsWith("prep") -> results.detectedPrepTime = mins
                    type.startsWith("cuis") || type.startsWith("cuisson") -> results.detectedCookTime = mins
                    type.startsWith("re") -> results.detectedRestingTime = mins
                }
                workingLine = workingLine.replace(tMatch.groupValues[0], "").trim()
                tMatch = timePattern.find(workingLine)
            }

            // 4. CONSOMMATION DES MOTS-CLÉS D'EXCLUSION
            for (word in excludedKeywords) {
                val pattern = Regex("(?i)(?<!\\p{L})${Regex.escape(word)}(?!\\p{L})")
                if (pattern.containsMatchIn(workingLine)) {
                    workingLine = pattern.replace(workingLine, "").trim()
                }
            }

            // 5. VIN ET SOURCE (Sécurisé : On n'extrait la source que si ce n'est PAS une action)
            if (!containsAction && !startsWithConnector) {
                if (WineParser.isWineLine(workingLine, wineRes)) {
                    results.detectedWineList.add(WineParser.cleanWineLine(workingLine, wineRes))
                    continue
                }
                // Détection Source : On ignore si la ligne commence par une minuscule (fragment probable)
                val isLikelySource = (OcrHelperUtils.isLikelyProperNameOrSource(workingLine) || SourceParser.isSourceLine(workingLine, sourceRes))
                if (isLikelySource && workingLine.firstOrNull()?.isUpperCase() == true) {
                    results.detectedSourceList.add(SourceParser.cleanSourceLine(workingLine, sourceRes))
                    continue
                }
            }

            // 6. CONSOMMATION DES HEADERS (BASCULE)
            val isInstrHeader = instructionHeaders.any { workingLine.lowercase().contains(it) } && workingLine.length < 35 && !containsAction
            val isIngrHeader = ingredientHeaders.any { workingLine.lowercase().contains(it) } && workingLine.length < 35 && !containsAction

            if (isInstrHeader) {
                currentSection = SECTION_INSTRUCTIONS
                workingLine = workingLine.replace(Regex("(?i)(?<!\\p{L})(?:${instructionHeaders.joinToString("|")})(?!\\p{L})"), "").trim()
            } else if (isIngrHeader) {
                currentSection = SECTION_INGREDIENTS
                workingLine = workingLine.replace(Regex("(?i)(?<!\\p{L})(?:${ingredientHeaders.joinToString("|")})(?!\\p{L})"), "").trim()
            }

            // 7. NETTOYAGE DU BRUIT RÉSIDUEL
            if (workingLine.isEmpty() || !workingLine.any { it.isLetter() }) continue

            // 8. CLASSIFICATION DE LA LIGNE
            val looksLikeIngredient = qtyRegex.containsMatchIn(workingLine.take(8)) ||
                    commonIngredients.any { workingLine.lowercase().startsWith(it.lowercase()) }

            if (containsAction && (workingLine.length > 25 || startsWithBullet)) {
                currentSection = SECTION_INSTRUCTIONS
            } else if (currentSection == SECTION_NONE && (looksLikeIngredient || startsWithConnector)) {
                currentSection = SECTION_INGREDIENTS
            }

            when (currentSection) {
                SECTION_INGREDIENTS -> results.rawIngredientsList.add(workingLine)
                SECTION_INSTRUCTIONS -> {
                    val isStrictIngredient = (looksLikeIngredient || startsWithConnector) && !containsAction && !OcrCleaner.isTechnicalDimension(workingLine)
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
