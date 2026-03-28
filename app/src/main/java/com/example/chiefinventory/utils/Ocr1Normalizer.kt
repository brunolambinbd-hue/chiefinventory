package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Step 1 of the OCR pipeline: Character and symbol normalization.
 * Responsibility: Transform "broken" OCR text into standard French text and remove metadata noise.
 * Case-preserving targeted normalization using XML-based spelling repairs.
 */
object Ocr1Normalizer {

    private val FRACTION_RULES = listOf(
        Regex("(?i)(?<![\\p{L}\\p{N}])[1il!|]{2}2(?![\\p{L}\\p{N}])") to "1/2",
        Regex("(?i)(?<![\\p{L}\\p{N}])[1il!|]{2}4(?![\\p{L}\\p{N}])") to "1/4",
        Regex("^1\\s*12\\b") to "1/2",
        Regex("^1\\s*14\\b") to "1/4",
        Regex("(?i)\\b[uw]2\\b") to "1/2",
        Regex("(?i)\\b11?2\\s+([\\p{L}]+)\\b(?!s)") to "1/2 $1"
    )

    private val NUMBER_CORRECTIONS = listOf(
        Regex("(?i)^[|il!]c(?=[\\s.àa])") to "1 c",
        // ld'eau après un chiffre -> l. d'eau (on ne rajoute pas de 1)
        Regex("(?i)(?<=\\d)\\s*l[d'’\\s]+eau\\b") to " l. d'eau",
        // ld'eau seul ou après du texte -> 1 l. d'eau
        Regex("(?i)(?<!\\d)(?<!\\d\\s)\\bl[d'’\\s]+eau\\b") to "1 l. d'eau",
        Regex("(?i)\\b1 ajout\\b") to "l'ajout",
        Regex("(?i)^[|il!]\\s*(?=\\d)") to "1",
        // Correction fraction : remplace |il! par 1 uniquement si pas précédé d'une lettre (évite KCAL/POR -> KCA1/POR)
        Regex("(?i)(?<!\\p{L})[|il!](?=/)") to "1",
        Regex("\\s+\\|\\s+") to " 1 ",
        Regex("(?i)\\bdel\\b") to "de 1"
    )

    private val NOISE_REMOVAL = listOf(
        Regex("\\b\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}\\b") to "",
        Regex("(?i)\\b\\d+(?:[.,]\\d+)?\\s*(?:f|francs?)\\b") to "",
        Regex("(?i)\\b\\d+(?:[.,]\\d+)?\\s*(?:€|eur|euros?)\\b") to ""
    )

    private val ARTICLE_CORRECTIONS = listOf(
        Regex("(?i)^1\\s+e\\s+") to "le ",
        Regex("(?i)^[t1]['’]") to "l'",
        Regex("(?i)\\s+[t1]['’]") to " l'"
    )

    private val VIETNAMESE_ACCENT_MAP = mapOf(
        'ế' to 'é', 'Ế' to 'É', 'ề' to 'è', 'Ề' to 'È', 'ệ' to 'é', 'Ệ' to 'É',
        'ấ' to 'â', 'Ấ' to 'Â', 'ầ' to 'à', 'À' to 'À', 'ả' to 'a', 'Ả' to 'A',
        'ặ' to 'a', 'Ặ' to 'A', 'ậ' to 'â', 'Ậ' to 'Â', 'ắ' to 'a', 'Ắ' to 'A', 'ạ' to 'a', 'Ạ' to 'A'
    )

    private val LINGUISTIC_CORRECTIONS = listOf(
        Regex("(?i)([a-z])d['’]") to "$1 d'",
        Regex("(?i)d['’]\\s+") to "d'",
        Regex("(?i)\\beuf(s?)\\b") to "oeuf$1",
        Regex("(?i)\\blajout\\b") to "l'ajout",
        Regex("(?i)\\b[1il|!]?ongueur\\b") to "longueur",
        Regex("(?i)\\b[1il|!]\\s*es\\b") to "les",
        Regex("(?i)\\bgri[b6l]\\b") to "gris"
    )

    private val SPOON_NORMALIZATION = listOf(
        Regex("(?i)\\bc\\s+[àa]\\s+f[eé](?![a-z])") to "c. à café",
        Regex("(?i)\\b(supe|sope)(s?)\\b") to "soupe$2",
        Regex("(?i)c\\.?\\s*[àa]\\s*(s|soupe)\\b") to "c. à soupe",
        Regex("(?i)c\\.?\\s*[àa]\\s*(?:c|caf[eé]|f[eé])\\b") to "c. à café",
        Regex("(?i)\\bc[àa]s\\b") to "c. à soupe",
        Regex("(?i)\\bc[àa]c\\b") to "c. à café"
    )

    private val PROTECTED_KEYWORDS = Regex("(?i)^(?:i|l)\\s*(ngrédients|nstructions)\\b")
    private val OCR_ONE_CORRECTION = Regex("(?i)(^|•\\s*|[\\-*]\\s*|\\()([|!|](?=[\\s\\p{L}])|[il](?=\\s+\\p{L}))")

    /**
     * Normalizes a single line of OCR text.
     * @param input The raw line from ML Kit.
     * @param repairs List of spelling repairs from XML (format: "wrong|correct").
     */
    fun normalize(input: String, repairs: List<String> = emptyList()): String {
        if (input.isBlank()) return ""

        // A. On préserve la casse originale pour garder les noms propres (Stage 4)
        var text = input.trim()

        // 1. Appliquer les corrections orthographiques spécifiques (Dictionnaire XML)
        // Utile pour corriger "dépinards" -> "d'épinards" avant tout traitement
        text = applySpellingRepairs(text, repairs)

        // 2. Réparation préalable des mots-clés structurels
        text = text.replace(PROTECTED_KEYWORDS) { match ->
            val suffix = match.groupValues[1]
            if (suffix.lowercase().startsWith("n")) "ingrédients" else "instructions"
        }
        text = text.replace(Regex("(?i)^t\\s*(?=\\d)"), "")

        // 3. Nettoyage global (Symboles, puces devant chiffres)
        text = text.replace("±", "").replace("+/-", "").replace("+-", "")
            .replace(Regex("(?i)\\benviron\\b"), "")
            .replace(Regex("(?i)^[!|il!|•\\-*]\\s+(?=\\d)"), "")

        NOISE_REMOVAL.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 4. Correction du chiffre 1 mal lu
        text = text.replace(OCR_ONE_CORRECTION) { match -> match.groupValues[1] + "1 " }

        // 5. Fractions et Corrections de nombres
        FRACTION_RULES.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        NUMBER_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        text = text.map { VIETNAMESE_ACCENT_MAP[it] ?: it }.joinToString("")
        
        ARTICLE_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        LINGUISTIC_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        SPOON_NORMALIZATION.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 6. Séparation chiffres/lettres SÉCURISÉE
        // On normalise les unités en minuscules dans le remplacement (ex: 200G -> 200 g)
        text = text.replace(Regex("(?i)(?<!\\p{L})(\\d+)([a-z]+)")) { match ->
            "${match.groupValues[1]} ${match.groupValues[2].lowercase()}"
        }

        // 7. Nettoyage final ponctuation et espaces
        text = text.replace(Regex("[,:;\\s\\-]+$"), "")
        return text.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Applique les paires de corrections définies dans le XML.
     */
    private fun applySpellingRepairs(text: String, repairs: List<String>): String {
        var result = text
        for (repair in repairs) {
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim()
                val correct = parts[1].trim()
                // On utilise \b pour ne remplacer que le mot entier (insensible à la casse)
                val pattern = Regex("(?i)\\b${Pattern.quote(wrong)}\\b")
                result = result.replace(pattern, correct)
            }
        }
        return result
    }
}
