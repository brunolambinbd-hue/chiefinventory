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

        val ingredientHeaders = listOf("ingrédients", "ingredients", "composition")
        val instructionHeaders = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression", "cuisson")

        val servingsRegex = Regex("(?i)(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)")
        val alternateServingsRegex = Regex("(?i)(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)")
        
        val qtyRegex = Regex("^(?:[\\d\\-*•¼½¾]|un\\b|une\\b|[|Il!](?=[\\s\\d]))", RegexOption.IGNORE_CASE)
        val containsActionRegex = Regex("(?i)\\b(?:${actionVerbs.joinToString("|")})\\b")

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            val lowerLine = trimmedLine.lowercase()
            val containsAction = containsActionRegex.containsMatchIn(trimmedLine)

            // 1. PROTECTION DES DIMENSIONS (ex: 5 mm sur 5 mm) -> Toujours Instruction
            if (OcrCleaner.isTechnicalDimension(trimmedLine)) {
                results.rawInstructionsList.add(trimmedLine)
                continue
            }

            // 2. DÉTECTION DES TEMPS ET PORTIONS
            // Portions (Pers.)
            val sMatch = servingsRegex.find(trimmedLine) ?: alternateServingsRegex.find(trimmedLine)
            if (sMatch != null) {
                if (results.detectedServings == null) {
                    results.detectedServings = if (sMatch.groupValues[1].any { it.isDigit() }) sMatch.groupValues[1] else sMatch.groupValues[2]
                    Log.d(TAG, "PERS détecté: ${results.detectedServings}")
                }
            }

            // Temps (Prép, Cuisson, Repos) - Sécurisé : pas d'action narrative et ligne courte
            val timePattern = Regex("(?i)(préparation|cuisson|repos|prep\\.?|cuis\\.?|rest\\.?)\\s*:?\\s*(\\d+\\s*(?:mn|min|h|heure|u))")
            val tMatch = timePattern.find(trimmedLine)
            if (tMatch != null && !containsAction && trimmedLine.length < 35) {
                val type = tMatch.groupValues[1].lowercase()
                val mins = extractMinutes(tMatch.groupValues[2])
                when {
                    type.startsWith("prép") || type.startsWith("prep") -> results.detectedPrepTime = mins
                    type.startsWith("cuis") || type.startsWith("cuisson") -> results.detectedCookTime = mins
                    type.startsWith("re") -> results.detectedRestingTime = mins
                }
                Log.d(TAG, "TIME détecté: ${tMatch.groupValues[1]} -> $mins min")

                val remainder = trimmedLine.replace(tMatch.groupValues[0], "").replace(Regex("^[:.,\\s]+"), "").trim()
                if (remainder.length <= 3) continue
            }

            // 3. DÉTECTION VIN
            if (!containsAction && WineParser.isWineLine(trimmedLine, wineRes)) {
                results.detectedWineList.add(WineParser.cleanWineLine(trimmedLine, wineRes))
                continue
            }

            // 4. DÉTECTION SOURCE (Auteur, Hôtel...)
            if (OcrHelperUtils.isLikelyProperNameOrSource(trimmedLine) || SourceParser.isSourceLine(trimmedLine, sourceRes)) {
                results.detectedSourceList.add(SourceParser.cleanSourceLine(trimmedLine, sourceRes))
                continue
            }

            // 5. CHANGEMENT DE SECTION (Headers)
            val isInstrHeader = instructionHeaders.any { lowerLine.contains(it) } && trimmedLine.length < 35 && !containsAction
            val isIngrHeader = ingredientHeaders.any { lowerLine.contains(it) } && trimmedLine.length < 35 && !containsAction

            if (isInstrHeader) { currentSection = SECTION_INSTRUCTIONS; continue }
            if (isIngrHeader) { currentSection = SECTION_INGREDIENTS; continue }

            // 6. CLASSIFICATION DE LA LIGNE
            val looksLikeIngredient = qtyRegex.containsMatchIn(trimmedLine.take(8)) ||
                    commonIngredients.any { lowerLine.startsWith(it.lowercase()) }

            // Priorité aux verbes d'action pour les étapes numérotées
            if (containsAction && trimmedLine.length > 25) {
                currentSection = SECTION_INSTRUCTIONS
            } else if (currentSection == SECTION_NONE && looksLikeIngredient) {
                currentSection = SECTION_INGREDIENTS
            }

            when (currentSection) {
                SECTION_INGREDIENTS -> results.rawIngredientsList.add(trimmedLine)
                SECTION_INSTRUCTIONS -> {
                    // Protection contre la récupération d'ingrédients égarés (exclure les dimensions techniques)
                    val isStrictIngredient = looksLikeIngredient && !containsAction && !OcrCleaner.isTechnicalDimension(trimmedLine)
                    if (isStrictIngredient && trimmedLine.length < 45) {
                        results.rawIngredientsList.add(trimmedLine)
                    } else {
                        results.rawInstructionsList.add(trimmedLine)
                    }
                }
                else -> {
                    if (looksLikeIngredient) results.rawIngredientsList.add(trimmedLine)
                    else results.rawInstructionsList.add(trimmedLine)
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
