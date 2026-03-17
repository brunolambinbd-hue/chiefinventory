package com.example.chiefinventory.utils

/**
 * Step 1 of the OCR pipeline: Character and symbol normalization.
 * Responsibility: Transform "broken" OCR text into standard French text and remove metadata noise.
 */
object Ocr1Normalizer {

    private val FRACTION_RULES = listOf(
        Regex("(?<![\\p{L}\\p{N}])[1il!|]{2}2(?![\\p{L}\\p{N}])") to "1/2",
        Regex("(?<![\\p{L}\\p{N}])[1il!|]{2}4(?![\\p{L}\\p{N}])") to "1/4",
        Regex("^1\\s*12\\b") to "1/2",
        Regex("^1\\s*14\\b") to "1/4",
        Regex("\\b[uw]2\\b") to "1/2",
        Regex("\\b11?2\\s+([\\p{L}]+)\\b(?!s)") to "1/2 $1"
    )

    private val NUMBER_CORRECTIONS = listOf(
        Regex("^[|il!]c(?=[\\s.àa])") to "1 c",
        Regex("\\bld['’]?eau\\b") to "1 l d'eau",
        Regex("\\b1 ajout\\b") to "l'ajout",
        Regex("^[|il!]\\s*(?=\\d)") to "1",
        Regex("[|il!](?=/)") to "1",
        Regex("\\s+\\|\\s+") to " 1 ",
        Regex("\\bdel\\b") to "de 1"
    )

    private val NOISE_REMOVAL = listOf(
        Regex("\\b\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}\\b") to "",
        Regex("\\b\\d+(?:[.,]\\d+)?\\s*(?:f|francs?)\\b") to "",
        Regex("\\b\\d+(?:[.,]\\d+)?\\s*(?:€|eur|euros?)\\b") to ""
    )

    private val ARTICLE_CORRECTIONS = listOf(
        Regex("^1\\s+e\\s+") to "le ",
        Regex("^[t1]['’]") to "l'",
        Regex("\\s+[t1]['’]") to " l'"
    )

    private val VIETNAMESE_ACCENT_MAP = mapOf(
        'ế' to 'é', 'ề' to 'è', 'ệ' to 'é', 'ẹ' to 'e',
        'ấ' to 'â', 'ầ' to 'à', 'ả' to 'a', 'ã' to 'a',
        'ặ' to 'a', 'ậ' to 'â', 'ắ' to 'a', 'ằ' to 'à', 'ạ' to 'a'
    )

    private val LINGUISTIC_CORRECTIONS = listOf(
        Regex("([a-z])d['’]") to "$1 d'",
        Regex("d['’]\\s+") to "d'",
        Regex("\\beuf(s?)\\b") to "oeuf$1",
        Regex("\\blajout\\b") to "l'ajout",
        Regex("\\b[1il|!]?ongueur\\b") to "longueur",
        Regex("\\b[1il|!]\\s*es\\b") to "les",
        Regex("\\bgri[b6l]\\b") to "gris"
    )

    private val SPOON_NORMALIZATION = listOf(
        // Règle spécifique pour le cas "c à fé" (détecté par vos tests)
        Regex("\\bc\\s+[àa]\\s+f[eé](?![a-z])") to "c. à café",
        // Règles générales
        Regex("\\b(supe|sope)(s?)\\b") to "soupe$2",
        Regex("c\\.?\\s*[àa]\\s*(s|soupe)\\b") to "c. à soupe",
        Regex("c\\.?\\s*[àa]\\s*(?:c|caf[eé]|f[eé])\\b") to "c. à café",
        Regex("\\bc[àa]s\\b") to "c. à soupe",
        Regex("\\bc[àa]c\\b") to "c. à café"
    )

    private val PROTECTED_KEYWORDS = Regex("^(?:i|l)\\s*(ngrédients|nstructions)\\b")
    private val OCR_ONE_CORRECTION = Regex("(^|•\\s*|[\\-*]\\s*|\\()([|!](?=[\\s\\p{L}])|[il](?=\\s+\\p{L}))")

    fun normalize(input: String): String {
        if (input.isBlank()) return ""

        // 0. Normalisation de la casse dès le départ (pour harmoniser g/G, CL/cl, etc.)
        var text = input.lowercase().trim()

        // 1. Réparation préalable des mots-clés structurels
        text = text.replace(PROTECTED_KEYWORDS) { match ->
            val suffix = match.groupValues[1]
            if (suffix.startsWith("n")) "ingrédients" else "instructions"
        }
        text = text.replace(Regex("^t\\s*(?=\\d)"), "")

        // 2. Nettoyage global (Symboles, puces devant chiffres)
        text = text.replace("±", "").replace("+/-", "").replace("+-", "")
            .replace(Regex("\\benviron\\b"), "")
            .replace(Regex("^[!|il!|•\\-*]\\s+(?=\\d)"), "")

        // 3. Bruit historique (Dates, Prix)
        NOISE_REMOVAL.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 4. Correction du chiffre 1
        text = text.replace(OCR_ONE_CORRECTION) { match -> match.groupValues[1] + "1 " }

        // 5. Fractions et Corrections de nombres
        FRACTION_RULES.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        NUMBER_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 6. Accents et Linguistique
        text = text.map { VIETNAMESE_ACCENT_MAP[it] ?: it }.joinToString("")
        ARTICLE_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        LINGUISTIC_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        SPOON_NORMALIZATION.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 7. Séparation chiffres/lettres SÉCURISÉE (ex: 200g -> 200 g)
        text = text.replace(Regex("(?<!\\p{L})(\\d+)([a-z]+)"), "$1 $2")

        // 8. Nettoyage final ponctuation et espaces
        text = text.replace(Regex("[,:;\\s\\-]+$"), "")
        return text.replace(Regex("\\s+"), " ").trim()
    }
}
