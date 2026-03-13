package com.example.chiefinventory.utils

/**
 * Step 1 of the OCR pipeline: Character and symbol normalization.
 * Responsibility: Transform "broken" OCR text into standard French text.
 */
object OcrNormalizer {

    private val FRACTION_RULES = listOf(
        // Correction des fractions type ll2, lI2, 112 en utilisant des frontières souples
        Regex("(?i)(?<![\\p{L}\\p{N}])[1Il!|]{2}2(?![\\p{L}\\p{N}])") to "1/2",
        Regex("(?i)(?<![\\p{L}\\p{N}])[1Il!|]{2}4(?![\\p{L}\\p{N}])") to "1/4",
        Regex("^1\\s*12\\b") to "1/2",
        Regex("^1\\s*14\\b") to "1/4",
        Regex("(?i)\\b[UW]2\\b") to "1/2",
        Regex("(?i)\\b11?2\\s+([\\p{L}]+)\\b(?!s)") to "1/2 $1"
    )

    private val NUMBER_CORRECTIONS = listOf(
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

    // Correction du chiffre 1 mal lu (l, I, |, !) au début de la ligne ou après une puce
    // On normalise systématiquement avec un espace ("1 ") pour aider le parseur ultérieur
    private val OCR_ONE_CORRECTION = Regex("(^|•\\s*|[\\-*]\\s*|\\()([|Il!|])(?=[\\s\\p{L}])")

    /**
     * Main entry point for text normalization.
     */
    fun normalize(input: String): String {
        if (input.isBlank()) return ""

        var text = input.trim()
            // 1. Correction des accents OCR (Vietnamien -> Français)
            .replace('ế', 'é').replace('ề', 'è').replace('ệ', 'é').replace('ẹ', 'e')
            .replace('ấ', 'â').replace('ầ', 'à').replace('ả', 'a').replace('ã', 'a')
            .replace('ặ', 'a').replace('ậ', 'â').replace('ắ', 'a').replace('ằ', 'à').replace('ạ', 'a')
            
            // 2. Suppression des symboles de tolérance
            .replace("±", "").replace("+/-", "").replace("+-", "")
            .replace(Regex("(?i)\\benviron\\b"), "")
            .replace(Regex("(?i)\\bt\\b\\s*(?=[\\d|Il!])"), "")

        // 3. Correction du chiffre 1 mal lu isolée ou collé à une unité
        text = text.replace(OCR_ONE_CORRECTION) { match ->
            match.groupValues[1] + "1 "
        }

        // 4. Application des règles par groupes
        FRACTION_RULES.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        NUMBER_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        ARTICLE_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        LINGUISTIC_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        SPOON_NORMALIZATION.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 5. Normalisation des espaces
        text = text.replace(Regex("(\\d)([a-zA-Z])"), "$1 $2")
        
        return text.replace(Regex("\\s+"), " ").trim()
    }
}
