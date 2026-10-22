package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Utility to parse ingredient strings.
 */
object IngredientParser {

    data class ParsedIngredient(
        val name: String,
        val quantity: Double? = null,
        val unit: String? = null,
        val supplementalInfo: String? = null
    )

    internal val defaultUnits: List<String> by lazy {
        try {
            OcrVocabularyLoader.loadFromProjectResources().culinaryMeasurementUnits
        } catch (e: Exception) {
            emptyList()
        }
    }

    private val rangeRegex = Regex("^\\s*(\\d+)\\s*(?:ou|à|-)\\s*(\\d+[.,]?\\d*)", RegexOption.IGNORE_CASE)
    private val fractionRegex = Regex("(\\d+)/(\\d+)")
    private val parenthesesRegex = Regex("\\s*\\((.*?)\\)")

    // Articles et quantifieurs vagues
    private val vagueQuantityRegex = Regex("^(quelques|plusieurs|un peu de|une poignée de|des|du|de la|de l'|le|la|les|l['’]|un|une)\\s+", RegexOption.IGNORE_CASE)
    // Adjectifs courants
    private val adjectiveRegex = Regex("^(petites?|petits?|grosses?|gros|fines?|fins?|belles?|beau|beaux|beau)\\s+", RegexOption.IGNORE_CASE)
    // Préfixes de préparation à mettre en info supp (ex: le jus de)
    private val prepPrefixRegex = Regex("^(?:le\\s+|la\\s+|l['’])?(jus|zeste|zestes|morceau|morceaux|tranche|tranches)\\s+(?:de\\s+|d['’]\\s*)", RegexOption.IGNORE_CASE)

    fun parse(input: String, customUnits: List<String> = emptyList()): ParsedIngredient {
        val activeUnits = if (customUnits.isNotEmpty()) {
            (customUnits + defaultUnits).distinct().sortedByDescending { it.length }
        } else {
            defaultUnits.sortedByDescending { it.length }
        }

        val parentheticalMatch = parenthesesRegex.find(input)
        var supplementalInfo = parentheticalMatch?.groupValues?.get(1)
        var mainText = input.replace(parenthesesRegex, "").trim()

        val (textAfterFirstExtraction, infoAfterFirstExtraction) = extractExtra(mainText, supplementalInfo)
        mainText = textAfterFirstExtraction
        supplementalInfo = infoAfterFirstExtraction

        val fractionMatch = fractionRegex.find(mainText)
        if (fractionMatch != null) {
            val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 1.0
            val rest = mainText.replace(fractionMatch.value, "").trim()
            val unitInfo = findUnit(rest, activeUnits)
            val (finalName, finalInfo) = extractExtra(unitInfo.remainingText, supplementalInfo)
            return ParsedIngredient(finalName, numerator / denominator, unitInfo.unit, finalInfo)
        }

        val rangeMatch = rangeRegex.find(mainText)
        if (rangeMatch != null) {
            val secondQty = rangeMatch.groupValues[2].replace(",", ".").toDoubleOrNull()
            val rest = mainText.substring(rangeMatch.range.last + 1).trim()
            val standard = parseStandard(rest, supplementalInfo, activeUnits)
            return ParsedIngredient(standard.name, secondQty, standard.unit, standard.supplementalInfo)
        }

        return parseStandard(mainText, supplementalInfo, activeUnits)
    }

    private fun extractExtra(text: String, currentInfo: String?): Pair<String, String?> {
        var mainText = text.trim()
        var supplementalInfo = currentInfo
        var continueExtraction = true
        while (continueExtraction) {
            val prepMatch = prepPrefixRegex.find(mainText)
            val vagueMatch = vagueQuantityRegex.find(mainText)
            val adjMatch = adjectiveRegex.find(mainText)

            if (prepMatch != null) {
                val word = prepMatch.value.trim()
                supplementalInfo = if (supplementalInfo.isNullOrBlank()) word else "$supplementalInfo $word"
                mainText = mainText.replaceFirst(prepMatch.value, "").trim()
            } else if (vagueMatch != null) {
                val word = vagueMatch.groupValues[1]
                supplementalInfo = if (supplementalInfo.isNullOrBlank()) word else "$word $supplementalInfo"
                mainText = mainText.replaceFirst(vagueMatch.value, "").trim()
            } else if (adjMatch != null) {
                val word = adjMatch.groupValues[1]
                supplementalInfo = if (supplementalInfo.isNullOrBlank()) word else "$word $supplementalInfo"
                mainText = mainText.replaceFirst(adjMatch.value, "").trim()
            } else {
                continueExtraction = false
            }
        }
        return mainText to supplementalInfo
    }

    private data class UnitInfo(val unit: String?, val remainingText: String)

    private fun findUnit(text: String, activeUnits: List<String> = defaultUnits): UnitInfo {
        for (unit in activeUnits) {
            val unitPattern = Regex("^${Pattern.quote(unit)}(?:\\s+|de\\s+|d['’]\\s*|\\.|\\b)", RegexOption.IGNORE_CASE)
            val match = unitPattern.find(text)
            if (match != null) {
                val namePart = text.substring(match.range.last + 1).trim()
                val finalName = namePart.replace(Regex("^(?:de\\s+|d['’]\\s*)", RegexOption.IGNORE_CASE), "").trim()
                return UnitInfo(unit, if (finalName.isEmpty()) text else finalName)
            }
        }
        return UnitInfo(null, text)
    }

    private fun parseStandard(input: String, supplementalInfo: String?, activeUnits: List<String>): ParsedIngredient {
        val matcher = Pattern.compile("^\\s*(\\d+[.,]?\\d*)\\s*(.*)$").matcher(input)
        var qty: Double? = null
        var rest = input.trim()

        if (matcher.find()) {
            val qtyStr = matcher.group(1)
            if (!qtyStr.isNullOrEmpty()) {
                qty = qtyStr.replace(",", ".").toDoubleOrNull()
                rest = matcher.group(2)?.trim() ?: ""
            }
        }

        val unitInfo = findUnit(rest, activeUnits)
        val (finalName, finalInfo) = extractExtra(unitInfo.remainingText, supplementalInfo)
        return ParsedIngredient(finalName, qty, unitInfo.unit, finalInfo)
    }
}
