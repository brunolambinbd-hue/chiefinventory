package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Utility to parse ingredient strings like "6 aubergines moyennes", "500g farine" or "1 ou 2 citrons".
 * Handles common OCR errors like '|' instead of '1' and removes approximation symbols.
 */
object IngredientParser {

    data class ParsedIngredient(
        val name: String,
        val quantity: Double? = null,
        val unit: String? = null
    )

    private val units = listOf("g", "kg", "ml", "l", "cl", "dl", "cuillère", "cuillères", "càs", "cac", "pincée", "pincées", "botte", "bottes", "paquet", "paquets", "verre", "verres")
    
    private val rangeRegex = Regex("^\\s*(\\d+)\\s*(?:ou|à|-)\\s*(\\d+[.,]?\\d*)", RegexOption.IGNORE_CASE)
    private val standardRegex = Pattern.compile("^\\s*(\\d+[.,]?\\d*)\\s*([a-zA-Z]*)\\s*(?:de\\s+|d'\\s+)?(.*)$", Pattern.CASE_INSENSITIVE)

    /**
     * Nettoie les erreurs courantes d'OCR et supprime les symboles d'approximation.
     */
    private fun preClean(input: String): String {
        return input.trim()
            // Supprime les symboles d'approximation (±, +/-, +-)
            .replace("±", "")
            .replace("+/-", "")
            .replace("+-", "")
            // Cas spécifique : l'OCR reconnaît ± comme 't' au début avant un chiffre
            .replace(Regex("^t\\s*(?=\\d)"), "")
            // Remplace '|' par '1' s'il est au début ou entouré d'espaces
            .replace(Regex("^\\|\\s+"), "1 ")
            .replace(Regex("\\s+\\|\\s+"), " 1 ")
            // Gère aussi 'I' ou 'l' qui sont souvent confondus avec '1' au début
            .replace(Regex("^[Il]\\s+"), "1 ")
            .trim()
    }

    fun parse(input: String): ParsedIngredient {
        val cleaned = preClean(input)
        
        val rangeMatch = rangeRegex.find(cleaned)
        if (rangeMatch != null) {
            val secondQty = rangeMatch.groupValues[2].replace(",", ".").toDoubleOrNull()
            val rest = cleaned.substring(rangeMatch.range.last + 1).trim()
            
            val subParsed = parseStandard(rest)
            return ParsedIngredient(
                name = subParsed.name,
                quantity = secondQty,
                unit = subParsed.unit
            )
        }

        return parseStandard(cleaned)
    }

    private fun parseStandard(input: String): ParsedIngredient {
        val matcher = standardRegex.matcher(input)

        if (matcher.find()) {
            val qtyStr = matcher.group(1)?.replace(",", ".")
            val unitMaybe = matcher.group(2)?.lowercase()
            val name = matcher.group(3)?.trim() ?: ""

            val qty = qtyStr?.toDoubleOrNull()
            
            return if (unitMaybe != null && units.any { unitMaybe.startsWith(it) }) {
                ParsedIngredient(name, qty, unitMaybe)
            } else {
                val realName = listOfNotNull(unitMaybe, name).joinToString(" ").trim()
                ParsedIngredient(realName, qty, null)
            }
        }

        return ParsedIngredient(input)
    }
}
