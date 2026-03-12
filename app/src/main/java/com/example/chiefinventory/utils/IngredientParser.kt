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

    private val FRACTION_RULES = listOf(
        Regex("(?i)(?<![\\p{L}\\p{N}])[1Il!|]{2}2(?![\\p{L}\\p{N}])") to "1/2",
        Regex("(?i)(?<![\\p{L}\\p{N}])[1Il!|]{2}4(?![\\p{L}\\p{N}])") to "1/4",
        Regex("^1\\s*12\\b") to "1/2",
        Regex("^1\\s*14\\b") to "1/4",
        Regex("(?i)\\b[UW]2\\b") to "1/2",
        Regex("(?i)\\b11?2\\s+([\\p{L}]+)\\b(?!s)") to "1/2 $1"
    )

    private val NUMBER_CORRECTIONS = listOf(
        Regex("^\\s*[|Il!]\\s+(?=\\p{L})") to "1 ",
        Regex("^[|Il!](?=\\s*\\d)") to "1",
        Regex("(?i)\\b[|Il!](?=/)") to "1",
        Regex("\\s+\\|\\s+") to " 1 ",
        Regex("(?i)\\bdel\\b") to "de 1",
        Regex("(?i)\\bld['’]?eau\\b") to "1 l d'eau"
    )

    private val ARTICLE_CORRECTIONS = listOf(
        Regex("^1\\s+e\\s+") to "le ",
        Regex("^[T1]['’]") to "l'",
        Regex("\\s+[T1]['’]") to " l'"
    )

    private val LINGUISTIC_CORRECTIONS = listOf(
        Regex("([a-zA-Z])d['’]") to "$1 d'",
        Regex("d['’]\\s+") to "d'",
        Regex("(?i)\\beuf(s?)\\b") to "oeuf$1",
        Regex("(?i)\\blajout\\b") to "l'ajout",
        Regex("(?i)\\b[1lI|!]?\\s*ongueur\\b") to "longueur",
        Regex("(?i)\\b[1lI|!]\\s*es\\b") to "les",
        Regex("(?i)\\bgri[b6l]\\b") to "gris"
    )

    private val SPOON_NORMALIZATION = listOf(
        Regex("(?i)\\b[1Il!|]c(?=[àa\\s\\.])") to "1 c",
        Regex("(?i)^[Il!]c\\b") to "1 c",
        Regex("(?i)^[Il!]c\\.") to "1 c.",
        Regex("(?i)\\bc\\.?\\s*[àa](?!\\w)") to "c. à",
        Regex("(?i)\\bc[àa]s\\b") to "c. à s.",
        Regex("(?i)\\bc[àa]c\\b") to "c. à c.",
        Regex("(?i)c\\.?\\s*àfé\\b") to "café",
        Regex("(?i)\\b(supe|sope)(s?)\\b") to "soupe$2",
        Regex("(?i)c\\.\\s*à\\s+(supe|sope)\\b") to "c. à soupe",
        Regex("(?i)\\bc\\.\\s*à\\s*soupe\\b") to "c. à soupe",
        Regex("(?i)\\bc\\.\\s*à\\s*café\\b") to "c. à café"
    )

    private val WORD_CORRECTIONS = listOf(
        Regex("(?i)\\borevettes?\\b") to "crevettes",
        Regex("(?i)\\bfacultari\\b") to "facultatif"
    )

    private val FINAL_NORMALIZATION = listOf(
        Regex("(\\d)([a-zA-Z])") to "$1 $2"
    )

    private val OCR_ONE_CORRECTION = Regex("(?<![\\p{L}\\p{N}])[|Il!T](?=\\s*[\\p{L}\\d/])")

    fun normalizeOcr(cleaned: String): String {


        var text = cleaned
        text = text.replace(Regex("(?i)\\b([0-9/]+)\\s*l\\b"), "$1 l")
        text = text.replace(OCR_ONE_CORRECTION, "1")


        FRACTION_RULES.forEach { (regex, replacement) ->
            text = text.replace(regex, replacement)
        }

        NUMBER_CORRECTIONS.forEach { (regex, replacement) ->
            text = text.replace(regex, replacement)
        }

        ARTICLE_CORRECTIONS.forEach { (regex, replacement) ->
            text = text.replace(regex, replacement)
        }

        LINGUISTIC_CORRECTIONS.forEach { (regex, replacement) ->
            text = text.replace(regex, replacement)
        }

        SPOON_NORMALIZATION.forEach { (regex, replacement) ->
            text = text.replace(regex, replacement)
        }

        WORD_CORRECTIONS.forEach { (regex, replacement) ->
            text = text.replace(regex, replacement)
        }

        FINAL_NORMALIZATION.forEach { (regex, replacement) ->
            text = text.replace(regex, replacement)
        }

        return text.replace(Regex("\\s+"), " ").trim()
    }
    /**
     * Nettoie les erreurs courantes d'OCR et les approximations.
     */
    fun preClean(input: String): String {
        var cleaned = input.trim()
            // Correction des accents mal lus par l'OCR (ex: ế -> é)
            .replace('ế', 'é').replace('ề', 'è').replace('ệ', 'é').replace('ẹ', 'e')
            .replace('ấ', 'â').replace('ầ', 'à').replace('ả', 'a').replace('ã', 'a').replace('ặ', 'a').replace('ậ', 'â').replace('ắ', 'a').replace('ằ', 'à').replace('ạ', 'a')
            .replace("±", "").replace("+/-", "").replace("+-", "")
            .replace(Regex("(?i)\\benviron\\b"), "")
            .replace(Regex("(?i)\\bt\\b\\s*(?=[\\d|Il!])"), "")
            
            // On supprime les symboles parasites devant un vrai nombre ou le mot environ
            .replace(Regex("^[!|Il!|]\\s+(?=\\d|environ\\b)"), "")


        cleaned = normalizeOcr(cleaned)

        // could be remobbed fromn here
        // CORRECTION DES FRACTIONS (Priorité Haute)
//        cleaned = cleaned.replace(Regex("(?i)(?<![\\p{L}\\p{N}])[1Il!|]{2}2(?![\\p{L}\\p{N}])"), "1/2")
//        cleaned = cleaned.replace(Regex("(?i)(?<![\\p{L}\\p{N}])[1Il!|]{2}4(?![\\p{L}\\p{N}])"), "1/4")
//
//        // Correction du 1 mal lu isolée (l, I, |, !) - Version stable sans look-behind variable
//        cleaned = cleaned.replace(Regex("(^|•\\s*|[\\-*]\\s*|\\()([|Il!|])(?=[\\s\\p{L}])")) { match ->
//            val prefix = match.groupValues[1]
//            prefix + "1 "
//        }
//
//        cleaned = cleaned.replace(Regex("^[|Il!](?=\\s*\\d)"), "1")
//        cleaned = cleaned.replace(Regex("(?i)\\b[|Il!](?=/)"), "1")
//        cleaned = cleaned.replace(Regex("\\s+\\|\\s+"), " 1 ")
//        cleaned = cleaned.replace(Regex("(?i)\\bdel\\b"), "de 1")
//        cleaned = cleaned.replace(Regex("(?i)\\bld['’]?eau\\b"), "1 l d'eau")
//
//        cleaned = cleaned.replace(Regex("^1\\s+e\\s+"), "le ")
//        cleaned = cleaned.replace(Regex("^[T1]['’]"), "l'")
//        cleaned = cleaned.replace(Regex("\\s+[T1]['’]"), " l'")
//
//        // RECONSTITUTION LINGUISTIQUE
//        cleaned = cleaned.replace(Regex("([a-zA-Z])d['’]"), "$1 d'")
//        cleaned = cleaned.replace(Regex("d['’]\\s+"), "d'")
//        cleaned = cleaned.replace(Regex("(?i)\\beuf(s?)\\b"), "oeuf$1")
//        cleaned = cleaned.replace(Regex("(?i)\\blajout\\b"), "l'ajout")
//        cleaned = cleaned.replace(Regex("(?i)\\b[1lI|!]?\\s*ongueur\\b"),"longueur")
//        cleaned = cleaned.replace(Regex("(?i)\\b[1lI|!]\\s*es\\b"),"les")
//        cleaned = cleaned.replace(Regex("(?i)\\bgri[b6l]\\b"),"gris")
//
//        // Normalisation des Cuillères
//        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!|]c(?=[àa\\s\\.])"), "1 c")
//        cleaned = cleaned.replace(Regex("(?i)^[Il!]c\\b"), "1 c")
//        cleaned = cleaned.replace(Regex("(?i)^[Il!]c\\."), "1 c.")
//        cleaned = cleaned.replace(Regex("(?i)\\bc\\.?\\s*[àa](?!\\w)"), "c. à")
//        cleaned = cleaned.replace(Regex("(?i)\\bc[àa]s\\b"), "c. à s.")
//        cleaned = cleaned.replace(Regex("(?i)\\bc[àa]c\\b"), "c. à c.")
//        cleaned = cleaned.replace(Regex("(?i)c\\.?\\s*àfé\\b"), "café")
//        cleaned = cleaned.replace(Regex("(?i)\\b(supe|sope)(s?)\\b"), "soupe$2")
//        cleaned = cleaned.replace(Regex("(?i)c\\.\\s*à\\s+(supe|sope)\\b"), "c. à soupe")
//        cleaned = cleaned.replace(Regex("(?i)\\borevettes?\\b"), "crevettes")
//        cleaned = cleaned.replace(Regex("(?i)\\bfacultari\\b"), "facultatif")
//        cleaned = cleaned.replace(Regex("(?i)\\bc\\.\\s*à\\s*soupe\\b"), "c. à soupe")
//        cleaned = cleaned.replace(Regex("(?i)\\bc\\.\\s*à\\s*café\\b"), "c. à café")
//
//        cleaned = cleaned.replace(Regex("(\\d)([a-zA-Z])"), "$1 $2")
//        cleaned = cleaned.replace(Regex("^1\\s*12\\b"), "1/2")
//        cleaned = cleaned.replace(Regex("^1\\s*14\\b"), "1/4")
//        cleaned = cleaned.replace(Regex("(?i)\\b[UW]2\\b"), "1/2")
//        cleaned = cleaned.replace(Regex("(?i)\\b11?2\\s+([\\p{L}]+)\\b(?!s)"), "1/2 $1")
//
//        return cleaned.replace(Regex("\\s+"), " ").trim()

        // until here
        return cleaned.replace(Regex("\\s+"), " ").trim()
    }

    fun parse(input: String): ParsedIngredient {
        val parentheticalMatch = parenthesesRegex.find(input)
        val supplementalInfo = parentheticalMatch?.groupValues?.get(1)?.let { preClean(it) }
        val mainText = input.replace(parenthesesRegex, "").trim()
        val cleanedText = preClean(mainText)
        
        val fractionMatch = fractionRegex.find(cleanedText)
        if (fractionMatch != null) {
            val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 1.0
            val rest = fractionMatch.groupValues[3].trim()
            val unitInfo = findUnit(rest)
            return ParsedIngredient(unitInfo.remainingText, numerator / denominator, unitInfo.unit, supplementalInfo)
        }
        
        val rangeMatch = rangeRegex.find(cleanedText)
        if (rangeMatch != null) {
            val secondQty = rangeMatch.groupValues[2].replace(",", ".").toDoubleOrNull()
            val rest = cleanedText.substring(rangeMatch.range.last + 1).trim()
            val subParsed = parseStandard(rest, supplementalInfo)
            return ParsedIngredient(subParsed.name, secondQty, subParsed.unit, supplementalInfo)
        }
        
        return parseStandard(cleanedText, supplementalInfo)
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
