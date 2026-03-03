package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Utility to parse ingredient strings like "6 aubergines moyennes", "500g farine", "1 ou 2 citrons" or "1/2 salade".
 * Handles common OCR errors like '|' instead of '1' and '112' instead of '1/2'.
 */
object IngredientParser {

    data class ParsedIngredient(
        val name: String,
        val quantity: Double? = null,
        val unit: String? = null
    )

    private val units = listOf(
        "g", "kg", "ml", "l", "cl", "dl", 
        "cuillère", "cuillères", "c. à soupe", "c à soupe", "c.à soupe", "c. a soupe", "c a soupe",
        "c. à café", "c à café", "c.à café", "c. a café", "c a café",
        "càs", "càc", "c. à s.", "c. à c.",
        "pincée", "pincées", "botte", "bottes", "paquet", "paquets", "verre", "verres", "tranche", "tranches"
    )
    
    private val rangeRegex = Regex("^\\s*(\\d+)\\s*(?:ou|à|-)\\s*(\\d+[.,]?\\d*)", RegexOption.IGNORE_CASE)
    private val fractionRegex = Regex("^\\s*(\\d+)/(\\d+)\\s*(.*)$")

    /**
     * Nettoie les erreurs courantes d'OCR et corrige les fractions mal lues.
     */
    fun preClean(input: String): String {
        var cleaned = input.trim()
            .replace("±", "")
            .replace("+/-", "")
            .replace("+-", "")
            .replace(Regex("^t\\s*(?=\\d)"), "")
            
            // Remplace '|', 'I', 'l', '!' par '1' au début de la ligne
            .replace(Regex("^[|Il!](?=\\s*\\d)"), "1")   // | 250 -> 1 250
            .replace(Regex("^[|Il!](?=\\s*/)"), "1")     // ! / 2 -> 1 / 2
            .replace(Regex("^[|Il!]\\s*(?=[a-zA-Z])"), "1 ") // | orange -> 1 orange
            
            .replace(Regex("\\s+\\|\\s+"), " 1 ")

        // Normalise 'c.à', 'c. a', etc. en 'c. à'
        cleaned = cleaned.replace(Regex("(?i)c\\.\\s*[àa]"), "c. à")
        cleaned = cleaned.replace(Regex("(?i)c\\s+[àa]"), "c. à")

        // Insère un espace entre un chiffre et une lettre s'ils sont collés
        cleaned = cleaned.replace(Regex("(\\d)([a-zA-Z])"), "$1 $2")

        // Cas spécifique : l'OCR lit '112' au lieu de '1/2' ou '114' au lieu de '1/4'
        // On gère aussi le cas où il y a un espace : '1 12'
        cleaned = cleaned.replace(Regex("^1\\s*12\\b"), "1/2")
        cleaned = cleaned.replace(Regex("^1\\s*14\\b"), "1/4")

        return cleaned.trim()
    }

    fun parse(input: String): ParsedIngredient {
        val cleaned = preClean(input)
        
        // 1. Détection des fractions (ex: 1/2 salade)
        val fractionMatch = fractionRegex.find(cleaned)
        if (fractionMatch != null) {
            val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 1.0
            val rest = fractionMatch.groupValues[3].trim()
            val qty = numerator / denominator
            val subParsed = parseStandard(rest)
            return ParsedIngredient(name = subParsed.name, quantity = qty, unit = subParsed.unit)
        }

        // 2. Détection des intervalles (ex: 1 ou 2 citrons)
        val rangeMatch = rangeRegex.find(cleaned)
        if (rangeMatch != null) {
            val secondQty = rangeMatch.groupValues[2].replace(",", ".").toDoubleOrNull()
            val rest = cleaned.substring(rangeMatch.range.last + 1).trim()
            val subParsed = parseStandard(rest)
            return ParsedIngredient(name = subParsed.name, quantity = secondQty, unit = subParsed.unit)
        }

        return parseStandard(cleaned)
    }

    private fun parseStandard(input: String): ParsedIngredient {
        // 1. Séparer le nombre au début
        val matcher = Pattern.compile("^\\s*(\\d+[.,]?\\d*)\\s*(.*)$").matcher(input)
        if (!matcher.find()) return ParsedIngredient(input)

        val qtyStr = matcher.group(1)?.replace(",", ".")
        val qty = qtyStr?.toDoubleOrNull()
        val rest = matcher.group(2)?.trim() ?: ""

        if (rest.isEmpty()) return ParsedIngredient("", qty, null)

        // 2. Chercher l'unité dans le texte restant
        val sortedUnits = units.sortedByDescending { it.length }
        for (unit in sortedUnits) {
            val unitPattern = Regex("^${Pattern.quote(unit)}(?:\\s+|de\\s+|d['’]\\s*|\\.|\\b)", RegexOption.IGNORE_CASE)
            val match = unitPattern.find(rest)
            if (match != null) {
                val namePart = rest.substring(match.range.last + 1).trim()
                val finalName = namePart.replace(Regex("^(?:de\\s+|d['’]\\s*)", RegexOption.IGNORE_CASE), "").trim()
                return ParsedIngredient(if (finalName.isEmpty()) rest else finalName, qty, unit)
            }
        }

        return ParsedIngredient(rest, qty, null)
    }
}
