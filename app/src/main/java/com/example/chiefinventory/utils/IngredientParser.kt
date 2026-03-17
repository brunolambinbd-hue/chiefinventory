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
        "c. à dessert", "c à dessert", "cuillère à dessert",
        "càs", "càc", "c. à s.", "c. à c.",
        "pincée", "pincées", "botte", "bottes", "paquet", "paquets", "verre", "verres", "tranche", "tranches",
        "filet", "filets", "trait", "traits", "brins", "brin", "feuilles", "feuille", "branche", "branches"
    )

    private val rangeRegex = Regex("^\\s*(\\d+)\\s*(?:ou|à|-)\\s*(\\d+[.,]?\\d*)", RegexOption.IGNORE_CASE)
    private val fractionRegex = Regex("^\\s*(\\d+)/(\\d+)\\s*(.*)$")
    private val parenthesesRegex = Regex("\\s*\\((.*?)\\)")

    // Liste des quantificateurs indéfinis à extraire vers supplementalInfo
    private val vagueQuantityRegex = Regex("^(quelques|plusieurs|un peu de|une poignée de|des|du|de la|de l'|un|une)\\s+", RegexOption.IGNORE_CASE)

    // Liste des adjectifs qualificatifs à déplacer vers supplementalInfo pour libérer l'unité
    private val adjectiveRegex = Regex("^(petites?|petits?|grosses?|gros|fines?|fins?|belles?|beau|beaux)\\s+", RegexOption.IGNORE_CASE)

    /**
     * Parses an ingredient line into structured data.
     * Assumes the input has already been normalized by OcrNormalizer.
     */
    fun parse(input: String): ParsedIngredient {
        // 1. Extraire les infos entre parenthèses
        val parentheticalMatch = parenthesesRegex.find(input)
        var supplementalInfo = parentheticalMatch?.groupValues?.get(1)
        var mainText = input.replace(parenthesesRegex, "").trim()

        // 2. Extraire récursivement tout ce qui n'est pas le NOM de l'ingrédient (ex: "des petites")
        var continueExtraction = true
        while (continueExtraction) {
            val vagueMatch = vagueQuantityRegex.find(mainText)
            val adjMatch = adjectiveRegex.find(mainText)

            if (vagueMatch != null) {
                val word = vagueMatch.groupValues[1]
                supplementalInfo = if (supplementalInfo != null) "$word, $supplementalInfo" else word
                mainText = mainText.replaceFirst(vagueMatch.value, "").trim()
            } else if (adjMatch != null) {
                val word = adjMatch.groupValues[1]
                supplementalInfo = if (supplementalInfo != null) "$word, $supplementalInfo" else word
                mainText = mainText.replaceFirst(adjMatch.value, "").trim()
            } else {
                continueExtraction = false
            }
        }

        // 3. Gestion des fractions (ex: 1/2)
        val fractionMatch = fractionRegex.find(mainText)
        if (fractionMatch != null) {
            val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 1.0
            val rest = fractionMatch.groupValues[3].trim()
            val unitInfo = findUnit(rest)
            return ParsedIngredient(unitInfo.remainingText, numerator / denominator, unitInfo.unit, supplementalInfo)
        }

        // 4. Gestion des intervalles (ex: 2 à 3)
        val rangeMatch = rangeRegex.find(mainText)
        if (rangeMatch != null) {
            val secondQty = rangeMatch.groupValues[2].replace(",", ".").toDoubleOrNull()
            val rest = mainText.substring(rangeMatch.range.last + 1).trim()
            val standard = parseStandard(rest, supplementalInfo)
            return ParsedIngredient(standard.name, secondQty, standard.unit, standard.supplementalInfo)
        }

        // 5. Parsing standard
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
        var qty: Double? = null
        var rest = input.trim()

        if (matcher.find()) {
            val qtyStr = matcher.group(1)
            if (!qtyStr.isNullOrEmpty()) {
                qty = qtyStr.replace(",", ".").toDoubleOrNull()
                rest = matcher.group(2)?.trim() ?: ""
            }
        }

        val unitInfo = findUnit(rest)
        return ParsedIngredient(unitInfo.remainingText, qty, unitInfo.unit, supplementalInfo)
    }
}