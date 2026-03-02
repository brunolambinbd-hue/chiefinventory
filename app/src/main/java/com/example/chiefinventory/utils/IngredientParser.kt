package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Utility to parse ingredient strings like "6 aubergines moyennes", "500g farine", "1 ou 2 citrons" or "1/2 salade".
 * Handles common OCR errors like '|' instead of '1' and complex units like "c. à soupe".
 */
object IngredientParser {

    data class ParsedIngredient(
        val name: String,
        val quantity: Double? = null,
        val unit: String? = null
    )

    // Liste étendue des unités incluant les formes sans accents pour l'OCR
    private val units = listOf(
        "g", "kg", "ml", "l", "cl", "dl", 
        "cuillère", "cuillères", "c. à soupe", "c à soupe", "c.à soupe", "c. a soupe", "c a soupe",
        "c. à café", "c à café", "c.à café", "c. a café", "c a café",
        "càs", "càc", "c. à s.", "c. à c.",
        "pincée", "pincées", "botte", "bottes", "paquet", "paquets", "verre", "verres", "tranche", "tranches"
    )
    
    private val rangeRegex = Regex("^\\s*(\\d+)\\s*(?:ou|à|-)\\s*(\\d+[.,]?\\d*)", RegexOption.IGNORE_CASE)
    private val fractionRegex = Regex("^\\s*(\\d+)/(\\d+)\\s*(.*)$")
    
    // Regex standard améliorée pour capturer les unités avec espaces/points (ex: "c. à soupe")
    private val standardRegex = Pattern.compile("^\\s*(\\d+[.,]?\\d*)\\s*([a-zA-Zàâäéèêëïîôöùûüÿç.\\s]*?)(?:\\s+(?:de\\s+|d'\\s+)|\\s+)(.*)$", Pattern.CASE_INSENSITIVE)

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
            .replace(Regex("^[|Il!](?=\\d)"), "1")   // !12 -> 112
            .replace(Regex("^[|Il!](?=/)"), "1")     // !/2 -> 1/2
            .replace(Regex("^[|Il!](?=[a-zA-Z])"), "1 ") // |c. -> 1 c.
            
            .replace(Regex("\\s+\\|\\s+"), " 1 ")

        // Normalise 'c.à', 'c. a', etc. en 'c. à' pour assurer le découpage
        cleaned = cleaned.replace(Regex("c\\.\\s*[àa]"), "c. à")
        cleaned = cleaned.replace(Regex("c\\s+[àa]"), "c. à")

        // Insère un espace entre un chiffre et une lettre s'ils sont collés (ex: 2c. -> 2 c.)
        cleaned = cleaned.replace(Regex("(\\d)([a-zA-Z])"), "$1 $2")

        // Cas spécifique : l'OCR lit '112' au lieu de '1/2' ou '114' au lieu de '1/4'
        if (cleaned.startsWith("112")) {
            cleaned = cleaned.replaceFirst("112", "1/2")
        } else if (cleaned.startsWith("114")) {
            cleaned = cleaned.replaceFirst("114", "1/4")
        }

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
        val matcher = standardRegex.matcher(input)

        if (matcher.find()) {
            val qtyStr = matcher.group(1)?.replace(",", ".")
            val unitMaybe = matcher.group(2)?.trim()?.lowercase()
            val name = matcher.group(3)?.trim() ?: ""

            val qty = qtyStr?.toDoubleOrNull()
            
            // On vérifie si ce qu'on a capturé comme unité correspond à notre liste
            return if (unitMaybe != null && units.any { it == unitMaybe || unitMaybe.startsWith(it) }) {
                ParsedIngredient(name, qty, unitMaybe)
            } else {
                // Si pas d'unité reconnue, tout le texte après le nombre est le nom
                val realName = listOfNotNull(unitMaybe, name).joinToString(" ").trim()
                ParsedIngredient(realName, qty, null)
            }
        }

        return ParsedIngredient(input)
    }
}
