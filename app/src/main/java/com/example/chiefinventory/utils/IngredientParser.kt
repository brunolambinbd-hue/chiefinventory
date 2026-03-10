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
        val supplementalInfo: String? = null // Nouveau champ pour les infos entre parenthèses
    )

    // Changement en internal pour être accessible par OcrCategorizer
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
     * Nettoie les erreurs courantes d'OCR et les approximations.
     */
    fun preClean(input: String): String {
        var cleaned = input.trim()
            // Correction des accents mal lus par l'OCR (ex: ế -> é)
            .replace('ế', 'é')
            .replace('ề', 'è')
            .replace('ệ', 'é')
            .replace('ẹ', 'e')
            
            // Correction pour la lettre 'a' (variantes OCR courantes)
            .replace('ấ', 'â')
            .replace('ầ', 'à')
            .replace('ả', 'a')
            .replace('ã', 'a')
            .replace('ặ', 'a')
            .replace('ậ', 'â')
            .replace('ắ', 'a')
            .replace('ằ', 'à')
            .replace('ạ', 'a')

            .replace("±", "")
            .replace("+/-", "")
            .replace("+-", "")
            .replace(Regex("(?i)\\benviron\\b"), "")
            .replace(Regex("(?i)\\bt\\b\\s*(?=[\\d|Il!])"), "")
            
            // Correction robuste du chiffre 1 mal lu (l, I, |, !)
            // Fonctionne au début, après un espace ou après une parenthèse
            .replace(Regex("(?<=[\\s(]|^)[|Il!|](?=\\s+[a-zA-Z])"), "1")
            
            .replace(Regex("^[|Il!](?=\\s*\\d)"), "1")
            // Correction des fractions type l/2 ou I/2 même au milieu du texte
            .replace(Regex("(?i)\\b[|Il!](?=/)"), "1")
            .replace(Regex("^[|Il!]\\s+(?=[a-zA-Z])"), "1 ")
            
            .replace(Regex("\\s+\\|\\s+"), " 1 ")
            .replace(Regex("(?i)\\bdel\\b"), "de 1")
            
            // Correction de "Ldeau" (1 litre d'eau fusionné)
            .replace(Regex("(?i)\\bld['’]?eau\\b"), "1 l d'eau")

        cleaned = cleaned.replace(Regex("^1\\s+e\\s+"), "le ")
        cleaned = cleaned.replace(Regex("^[T1]['’]"), "l'")
        cleaned = cleaned.replace(Regex("\\s+[T1]['’]"), " l'")

        // RECONSTITUTION LINGUISTIQUE (ex: jauned' euf -> jaune d'oeuf)
        cleaned = cleaned.replace(Regex("([a-zA-Z])d['’]"), "$1 d'")
        cleaned = cleaned.replace(Regex("d['’]\\s+"), "d'")
        cleaned = cleaned.replace(Regex("(?i)\\beuf(s?)\\b"), "oeuf$1")

        // Correction des préfixes de cuillères (lc, 1c, !c, |c) même si collés à "à"
        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!|]c(?=[àa\\s\\.])"), "1 c")

        cleaned = cleaned.replace(Regex("(?i)^[Il!]c\\b"), "1 c")
        cleaned = cleaned.replace(Regex("(?i)^[Il!]c\\."), "1 c.")
        
        // Normalisation de "c à", "c.à", "cà" en "c. à"
        cleaned = cleaned.replace(Regex("(?i)\\bc\\.?\\s*[àa]\\b"), "c. à")
        
        // Correction de "supe" en "soupe" après "c. à"
        cleaned = cleaned.replace(Regex("(?i)c\\.\\s*à\\s+supe\\b"), "c. à soupe")
        
        // Correction de "facultari" en "facultatif"
        cleaned = cleaned.replace(Regex("(?i)\\bfacultari\\b"), "facultatif")
        
        // Normalisation de la casse pour les unités classiques
        cleaned = cleaned.replace(Regex("(?i)\\bc\\.\\s*à\\s*soupe\\b"), "c. à soupe")
        cleaned = cleaned.replace(Regex("(?i)\\bc\\.\\s*à\\s*café\\b"), "c. à café")

        cleaned = cleaned.replace(Regex("(\\d)([a-zA-Z])"), "$1 $2")

        cleaned = cleaned.replace(Regex("^1\\s*12\\b"), "1/2")
        cleaned = cleaned.replace(Regex("^1\\s*14\\b"), "1/4")
        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!]{1,2}[I|!l]2\\b"), "1/2")
        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!]{1,2}[I|!l]4\\b"), "1/4")
        
        // Correction de 12 lu à la place de 1/2 devant un nom singulier (ex: 12 citron -> 1/2 citron)
        cleaned = cleaned.replace(Regex("(?i)\\b12\\s+(citron|avocat|orange|oignon|gousse|pamplemousse)\\b(?!s)"), "1/2 $1")

        return cleaned.trim()
    }

    fun parse(input: String): ParsedIngredient {
        // 1. Extraire et stocker l'information entre parenthèses
        val parentheticalMatch = parenthesesRegex.find(input)
        val supplementalInfo = parentheticalMatch?.groupValues?.get(1)?.let { preClean(it) }
        val mainText = input.replace(parenthesesRegex, "").trim()

        // 2. Continuer le parsing sur le texte principal nettoyé
        val cleanedText = preClean(mainText)
        
        val fractionMatch = fractionRegex.find(cleanedText)
        if (fractionMatch != null) {
            val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 1.0
            val rest = fractionMatch.groupValues[3].trim()
            val subParsed = parseStandard(rest, supplementalInfo)
            return ParsedIngredient(name = subParsed.name, quantity = numerator / denominator, unit = subParsed.unit, supplementalInfo = supplementalInfo)
        }
        
        val rangeMatch = rangeRegex.find(cleanedText)
        if (rangeMatch != null) {
            val secondQty = rangeMatch.groupValues[2].replace(",", ".").toDoubleOrNull()
            val rest = cleanedText.substring(rangeMatch.range.last + 1).trim()
            val subParsed = parseStandard(rest, supplementalInfo)
            return ParsedIngredient(name = subParsed.name, quantity = secondQty, unit = subParsed.unit, supplementalInfo = supplementalInfo)
        }
        
        return parseStandard(cleanedText, supplementalInfo)
    }

    private fun parseStandard(input: String, supplementalInfo: String?): ParsedIngredient {
        val matcher = Pattern.compile("^\\s*(\\d+[.,]?\\d*)\\s*(.*)$").matcher(input)
        if (!matcher.find()) return ParsedIngredient(input, supplementalInfo = supplementalInfo)
        
        val qty = matcher.group(1)?.replace(",", ".")?.toDoubleOrNull()
        val rest = matcher.group(2)?.trim() ?: ""
        if (rest.isEmpty()) return ParsedIngredient("", qty, null, supplementalInfo)
        
        for (unit in units.sortedByDescending { it.length }) {
            val unitPattern = Regex("^${Pattern.quote(unit)}(?:\\s+|de\\s+|d['’]\\s*|\\.|\\b)", RegexOption.IGNORE_CASE)
            val match = unitPattern.find(rest)
            if (match != null) {
                val namePart = rest.substring(match.range.last + 1).trim()
                val finalName = namePart.replace(Regex("^(?:de\\s+|d['’]\\s*)", RegexOption.IGNORE_CASE), "").trim()
                return ParsedIngredient(if (finalName.isEmpty()) rest else finalName, qty, unit, supplementalInfo)
            }
        }
        
        return ParsedIngredient(rest, qty, null, supplementalInfo)
    }
}
