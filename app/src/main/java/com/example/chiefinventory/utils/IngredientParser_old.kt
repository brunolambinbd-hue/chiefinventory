package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Utility to parse ingredient strings.
 */
object IngredientParser_old {

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
            .replace('ế', 'é').replace('ề', 'è').replace('ệ', 'é').replace('ẹ', 'e')
            .replace('ấ', 'â').replace('ầ', 'à').replace('ả', 'a').replace('ã', 'a').replace('ặ', 'a').replace('ậ', 'â').replace('ắ', 'a').replace('ằ', 'à').replace('ạ', 'a')
            .replace("±", "").replace("+/-", "").replace("+-", "")
            .replace(Regex("(?i)\\benviron\\b"), "")
            .replace(Regex("(?i)\\bt\\b\\s*(?=[\\d|Il!])"), "")

            // On supprime les symboles parasites devant un vrai nombre ou le mot environ
            .replace(Regex("^[!|Il!|]\\s+(?=\\d|environ\\b)"), "")

            // Correction du 1 mal lu sans look-behind variable (évite le crash Android)
            // Supporte maintenant le cas collé (ex: !c. -> 1 c.)
            .replace(Regex("(^|•\\s*|[\\-*]\\s*|\\()([|Il!|])(?=[\\s\\p{L}])")) { match ->
                val prefix = match.groupValues[1]
                prefix + "1 "
            }

            .replace(Regex("^[|Il!](?=\\s*\\d)"), "1")
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
        cleaned = cleaned.replace(Regex("(?i)\\blajout\\b"), "l'ajout")

        // Correction des préfixes de cuillères (lc, 1c, !c, |c) même si collés à "à"
        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!|]c(?=[àa\\s\\.])"), "1 c")

        cleaned = cleaned.replace(Regex("(?i)^[Il!]c\\b"), "1 c")
        cleaned = cleaned.replace(Regex("(?i)^[Il!]c\\."), "1 c.")

        // Normalisation de "c à", "c.à", "cà" en "c. à"
        cleaned = cleaned.replace(Regex("(?i)\\bc\\.?\\s*[àa](?!\\w)"), "c. à")

        // Normalisation de "càs" / "càc" (avec ou sans accent)
        cleaned = cleaned.replace(Regex("(?i)\\bc[àa]s\\b"), "c. à s.")
        cleaned = cleaned.replace(Regex("(?i)\\bc[àa]c\\b"), "c. à c.")

        // Correction de "c. àfé" en "café" (OCR a confondu "ca" avec "c. à")
        cleaned = cleaned.replace(Regex("(?i)c\\.?\\s*àfé\\b"), "café")

        // Correction de "supe" ou "sope" en "soupe" après "c. à" ou isolés
        cleaned = cleaned.replace(Regex("(?i)\\b(supe|sope)(s?)\\b"), "soupe$2")
        cleaned = cleaned.replace(Regex("(?i)c\\.\\s*à\\s+(supe|sope)\\b"), "c. à soupe")

        // Correction de "orevettes" en "crevettes"
        cleaned = cleaned.replace(Regex("(?i)\\borevettes?\\b"), "crevettes")

        // Correction de "facultari" en "facultatif"
        cleaned = cleaned.replace(Regex("(?i)\\bfacultari\\b"), "facultatif")

        // Normalisation de la casse pour les unités classiques
        cleaned = cleaned.replace(Regex("(?i)\\bc\\.\\s*à\\s*soupe\\b"), "c. à soupe")
        cleaned = cleaned.replace(Regex("(?i)\\bc\\.\\s*à\\s*café\\b"), "c. à café")

        cleaned = cleaned.replace(Regex("(\\d)([a-zA-Z])"), "$1 $2")

        cleaned = cleaned.replace(Regex("^1\\s*12\\b"), "1/2")
        cleaned = cleaned.replace(Regex("^1\\s*14\\b"), "1/4")

        // Correction des fractions 1/2 lues comme 112, ll2, etc. (support du pipe |)
        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!|]{2}2\\b"), "1/2")
        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!|]{2}4\\b"), "1/4")

        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!]{1,2}[I|!l]2\\b"), "1/2")
        cleaned = cleaned.replace(Regex("(?i)\\b[1Il!]{1,2}[I|!l]4\\b"), "1/4")

        // Correction de "U2" ou "W2" lu à la place de 1/2
        cleaned = cleaned.replace(Regex("(?i)\\b[UW]2\\b"), "1/2")

        // Correction de 12 ou 112 lu à la place de 1/2 devant un nom singulier
        cleaned = cleaned.replace(Regex("(?i)\\b11?2\\s+([\\p{L}]+)\\b(?!s)"), "1/2 $1")

        // Nettoyage final des espaces redondants
        return cleaned.replace(Regex("\\s+"), " ").trim()
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
            // On cherche l'unité dans le reste sans chercher de nouveau chiffre
            val unitInfo = findUnit(rest)
            return ParsedIngredient(
                name = unitInfo.remainingText,
                quantity = numerator / denominator,
                unit = unitInfo.unit,
                supplementalInfo = supplementalInfo
            )
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