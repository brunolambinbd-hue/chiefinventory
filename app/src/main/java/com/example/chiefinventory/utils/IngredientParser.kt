package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Utility to parse ingredient strings.
 * Now simplified: relies on OcrNormalizer for text cleaning.
 */
object IngredientParser {

    data class ParsedIngredient(
        val name: String,
        val quantity: Double? = null,
        val unit: String? = null,
        val supplementalInfo: String? = null
    )

    internal val units = listOf(
        "g", "kg", "ml", "l", "cl", "dl", "litre", "litres",
        "cuillère", "cuillères", "c. à soupe", "c à soupe", "c.à soupe", "c. a soupe", "c a soupe",
        "c. à café", "c à café", "c.à café", "c. a café", "c a café",
        "càs", "càc", "c. à s.", "c. à c.",
        "pincée", "pincées", "botte", "bottes", "paquet", "paquets", "verre", "verres", "tranche", "tranches",
        "filet", "filets", "trait", "traits", "brins", "brin"
    )
    
    private val rangeRegex = Regex("^\\s*(\\d+)\\s*(?:ou|à|-)\\s*(\\d+[.,]?\\d*)", RegexOption.IGNORE_CASE)
    private val fractionRegex = Regex("^\\s*(\\d+)/(\\d+)\\s*(.*)$")
    private val parenthesesRegex = Regex("\\s*\\((.*?)\\)")

    /**
     * Parses an ingredient line into structured data.
     * Assumes the input has already been normalized by OcrNormalizer.
     */
    fun parse(input: String): ParsedIngredient {
        // 1. Extract supplemental info from parentheses
        val parentheticalMatch = parenthesesRegex.find(input)
        val supplementalInfo = parentheticalMatch?.groupValues?.get(1)
        val mainText = input.replace(parenthesesRegex, "").trim()

        // 2. Fraction handling (ex: 1/2)
        val fractionMatch = fractionRegex.find(mainText)
        if (fractionMatch != null) {
            val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 1.0
            val rest = fractionMatch.groupValues[3].trim()
            val unitInfo = findUnit(rest)
            return ParsedIngredient(unitInfo.remainingText, numerator / denominator, unitInfo.unit, supplementalInfo)
        }
        
        // 3. Range handling (ex: 2 à 3)
        val rangeMatch = rangeRegex.find(mainText)
        if (rangeMatch != null) {
            val secondQty = rangeMatch.groupValues[2].replace(",", ".").toDoubleOrNull()
            val rest = mainText.substring(rangeMatch.range.last + 1).trim()
            val subParsed = parseStandard(rest, supplementalInfo)
            return ParsedIngredient(subParsed.name, secondQty, subParsed.unit, supplementalInfo)
        }
        
        return parseStandard(mainText, supplementalInfo)
    }

    private data class UnitInfo(val unit: String?, val remainingText: String)

    private fun findUnit(text: String): UnitInfo {
        for (unit in units.sortedByDescending { it.length }) {
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

    private fun parseStandard(input: String, supplementalInfo: String?): ParsedIngredient {
        val matcher = Pattern.compile("^\\s*(\\d+[.,]?\\d*)\\s*(.*)$").matcher(input)
        if (!matcher.find()) return ParsedIngredient(input, supplementalInfo = supplementalInfo)
        
        val qty = matcher.group(1)?.replace(",", ".")?.toDoubleOrNull()
        val rest = matcher.group(2)?.trim() ?: ""
        if (rest.isEmpty()) return ParsedIngredient("", qty, null, supplementalInfo)
        
        val unitInfo = findUnit(rest)
        return ParsedIngredient(unitInfo.remainingText, qty, unitInfo.unit, supplementalInfo)
    }
}
