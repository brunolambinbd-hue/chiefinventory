package com.example.chiefinventory.utils

/**
 * Step 1 of the OCR pipeline: Character and symbol normalization.
 * Responsibility: Transform "broken" OCR text into standard French text and remove metadata noise.
 * NEW: Target-based normalization (Preserves original case while cleaning noise).
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
        Regex("(?i)\\bld['’]?eau\\b") to "1 l d'eau",
        Regex("(?i)\\b1 ajout\\b") to "l'ajout",
        Regex("(?i)^[|il!]\\s*(?=\\d)") to "1",
        Regex("(?i)[|il!](?=/)") to "1",
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
        'ấ' to 'â', 'Ấ' to 'Â', 'ầ' to 'à', 'Ầ' to 'À', 'ả' to 'a', 'Ả' to 'A',
        'ặ' to 'a', 'Ặ' to 'A', 'ậ' to 'â', 'Ậ' to 'Â', 'ắ' to 'a', 'Ắ' to 'A'
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
    
    /**
     * Règle de détection du 1 plus fine :
     * - Symboles (| ou !) : Convertis si suivis d'une lettre ou d'un espace.
     * - Lettres (i ou l) : Convertis UNIQUEMENT si suivis d'un espace (isolés).
     */
    private val OCR_ONE_CORRECTION = Regex("(?i)(^|•\\s*|[\\-*]\\s*|\\()([|!|](?=[\\s\\p{L}])|[il](?=\\s+\\p{L}))")

    fun normalize(input: String): String {
        if (input.isBlank()) return ""

        // A. ON NE PASSE PLUS TOUT EN LOWERCASE. On travaille sur le texte original.
        var text = input.trim()

        // 1. Réparation préalable des mots-clés (garde la casse probable)
        text = text.replace(PROTECTED_KEYWORDS) { match ->
            val suffix = match.groupValues[1]
            if (suffix.lowercase().startsWith("n")) "ingrédients" else "instructions"
        }
        text = text.replace(Regex("(?i)^t\\s*(?=\\d)"), "")

        // 2. Nettoyage global (Symboles, puces devant chiffres)
        text = text.replace("±", "").replace("+/-", "").replace("+-", "")
            .replace(Regex("(?i)\\benviron\\b"), "")
            .replace(Regex("(?i)^[!|il!|•\\-*]\\s+(?=\\d)"), "")

        // 3. Bruit historique (Dates, Prix)
        NOISE_REMOVAL.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 4. Correction du chiffre 1 (Sécurisée par la règle de l'espace)
        text = text.replace(OCR_ONE_CORRECTION) { match -> match.groupValues[1] + "1 " }

        // 5. Fractions et Corrections de nombres
        FRACTION_RULES.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        NUMBER_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 6. Accents (Gère majuscules et minuscules via la map enrichie)
        text = text.map { VIETNAMESE_ACCENT_MAP[it] ?: it }.joinToString("")
        
        ARTICLE_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        LINGUISTIC_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        SPOON_NORMALIZATION.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 7. Séparation chiffres/lettres SÉCURISÉE
        // On normalise les unités en minuscules dans le remplacement (ex: 200G -> 200 g)
        text = text.replace(Regex("(?i)(?<!\\p{L})(\\d+)([a-z]+)")) { match ->
            "${match.groupValues[1]} ${match.groupValues[2].lowercase()}"
        }

        // 8. Nettoyage final ponctuation et espaces
        text = text.replace(Regex("[,:;\\s\\-]+$"), "")
        return text.replace(Regex("\\s+"), " ").trim()
    }
}
