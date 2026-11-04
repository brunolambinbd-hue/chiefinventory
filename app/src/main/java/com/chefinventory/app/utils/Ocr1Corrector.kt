package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager
import java.util.regex.Matcher

/**
 * Étape 1 du pipeline OCR : Correction et normalisation des caractères, chiffres, fractions et symboles.
 *
 * Ordre d'Exécution des Traitements :
 * 1. Garde-fou d'entrée et Early-Exit si la ligne est vide.
 * 2. Réparations orthographiques de mots entiers et élisions (`ocr_step1_spelling_repairs.xml`, `getSpellingRepairPattern`).
 * 3. Protection des mots-clés d'en-tête contre la confusion 'I'->'1' ([OcrRegexes.Normalizer.PROTECTED_KEYWORDS]).
 * 4. Nettoyage des préfixes parasites et puces devant un chiffre ([OcrRegexes.Normalizer.PREFIX_T_BEFORE_DIGIT]).
 * 5. Suppression des bruits de dates, devises et prix ([OcrRegexes.Normalizer.NOISE_REMOVAL]).
 * 6. Correction du chiffre '1' mal lu ([OcrRegexes.Normalizer.OCR_ONE_CORRECTION]).
 * 7. Conversions de fractions et nombres si présence de chiffre ([OcrRegexes.Quantities.FRACTION_RULES], `isDigit()`).
 * 8. Conversion des diacritiques et accents parasites Unicode (`ocr_step1_diacritic_repairs.xml`).
 * 9. Normalisation des cuillères si la ligne contient 'c' ([OcrRegexes.Normalizer.SPOON_NORMALIZATION], `contains('c')`).
 * 10. Séparation Chiffre / Unité ([OcrRegexes.Normalizer.NUMBER_UNIT_SEPARATION]).
 * 11. Nettoyage de la ponctuation finale parasite ([OcrRegexes.Normalizer.TRAILING_PUNCTUATION]).
 * 12. Effondrement des espaces multiples et retrait des espaces de bordure ([OcrRegexes.Normalizer.MULTIPLE_SPACES]).
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr1Corrector {

    private const val TAG = "Ocr1Corrector"
    private const val KEYWORD_INGREDIENTS = "ingrédients"
    private const val KEYWORD_INSTRUCTIONS = "instructions"

    /**
     * Charge dynamiquement la liste des paires de réparations diacritiques depuis les ressources du projet.
     *
     * @param projectDir Le répertoire racine du projet à partir duquel localiser `res/values`.
     * @return La liste des paires "fautif|correct", ou une liste vide en cas d'erreur.
     */
    internal fun loadDefaultDiacriticRepairs(projectDir: java.io.File = java.io.File(".")): List<String> {
        return try {
            OcrVocabularyLoader.loadFromProjectResources(projectDir).ocrDiacriticRepairs
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to load default diacritic repairs", e)
            emptyList()
        }
    }

    /** Diacritiques et accents parasites par défaut chargés dynamiquement depuis `R.array.ocr_diacritic_repairs` (`ocr_step1_diacritic_repairs.xml`). */
    internal val defaultDiacriticRepairs: List<String> by lazy {
        loadDefaultDiacriticRepairs()
    }

    /**
     * Effectue la correction complète d'une chaîne de texte brute issue de l'OCR.
     *
     * @param input La chaîne de texte brute.
     * @param repairs Liste facultative de paires de réparations orthographiques (ex: de [OcrVocabulary.ocrSpellingRepairs]).
     * @param diacriticRepairs Liste facultative de réparations diacritiques (ex: de [OcrVocabulary.ocrDiacriticRepairs]).
     * @return La chaîne nettoyée et corrigée.
     */
    fun normalize(input: String, repairs: List<String> = emptyList(), diacriticRepairs: List<String> = emptyList()): String {
        // 1. Garde-fou d'entrée : chaîne vide ou blanche
        if (input.isBlank()) {
            return ""
        }
        var text = input.trim()

        // 2. Sélection de la table de diacritiques (reçue ou par défaut depuis diacritic_repairs.xml)
        val activeDiacritics = diacriticRepairs.ifEmpty { defaultDiacriticRepairs }

        // 3. Application des réparations orthographiques issues de ocr_repairs.xml
        text = applySpellingRepairs(text, repairs)

        // 4. Protection des mots-clés d'en-tête de section ("INGRÉDIENTS", "INSTRUCTIONS") contre la confusion 'I' -> '1'
        text = text.replace(OcrRegexes.Normalizer.PROTECTED_KEYWORDS) { match ->
            val suffix = match.groupValues[1]
            if (suffix.lowercase().startsWith("ng")) {
                KEYWORD_INGREDIENTS
            } else {
                KEYWORD_INSTRUCTIONS
            }
        }

        // 5. Suppression du préfixe 't' parasite devant un chiffre (ex: "t 10 min" -> "10 min")
        text = text.replace(OcrRegexes.Normalizer.PREFIX_T_BEFORE_DIGIT, "")

        // 6. Suppression des symboles de tolérance (±, +/-) et des puces parasites devant un chiffre (ex: "! 100 g" -> "100 g")
        text = text.replace("±", "").replace("+/-", "").replace("+-", "")
            .replace(OcrRegexes.Normalizer.ENVIRON_WORD, "")
            .replace(OcrRegexes.Normalizer.SYMBOL_BEFORE_DIGIT, "")

        // 7. Suppression du bruit de dates, devises et prix (ex: "14/12/94", "15.50 EUR", "20 €")
        OcrRegexes.Normalizer.NOISE_REMOVAL.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 8. Correction de la confusion du chiffre '1' lu comme '|', '!', 'i' ou 'l' ou 'I' ou 'L' devant un mot
        text = text.replace(Regex("(^|•\\s*|[\\-*]\\s*|\\()([|!](?=[\\s\\p{L}])|[IiLl](?=\\s+\\p{L}))")) { match -> match.groupValues[1] + "1 " }

        // 9. Garde-fou de performance Chiffres : ne déclencher FRACTION_RULES et NUMBER_CORRECTIONS que si la ligne contient au moins un chiffre
        if (text.any { it.isDigit() }) {
            OcrRegexes.Quantities.FRACTION_RULES.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        }
        OcrRegexes.Quantities.NUMBER_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 10. Conversion des diacritiques et accents parasites Unicode (diacritic_repairs.xml)
        text = applyDiacriticRepairs(text, activeDiacritics)

        // 11. Corrections des articles ("1 e" -> "le") et liaisons linguistiques ("d' orange" -> "d'orange")
        OcrRegexes.Normalizer.ARTICLE_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        OcrRegexes.Normalizer.LINGUISTIC_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 12. Garde-fou de performance Cuillères : ne déclencher SPOON_NORMALIZATION que si la ligne contient la lettre 'c'
        if (text.contains('c', ignoreCase = true)) {
            text = text.replace(Regex("(?i)\\b(\\d+)(c\\.)"), "$1 $2")
                .replace(Regex("(?i)\\bc\\.\\s*à"), "c. à")
            OcrRegexes.Normalizer.SPOON_NORMALIZATION.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        }

        // 13. Séparation d'un nombre collé à son unité (ex: "500g" -> "500 g", "1l" -> "1 l")
        text = text.replace(OcrRegexes.Normalizer.NUMBER_UNIT_SEPARATION) { match ->
            "${match.groupValues[1]} ${match.groupValues[2].lowercase()}"
        }

        // 14. Nettoyage de la ponctuation finale parasite (virgules, deux-points, points-virgules) tout en préservant le tiret de césure
        text = text.replace(OcrRegexes.Normalizer.TRAILING_PUNCTUATION, "")

        // 15. Effondrement des espaces multiples consécutifs en un espace unique et retrait des espaces de bordure
        return text.replace(OcrRegexes.Normalizer.MULTIPLE_SPACES, " ").trim()
    }

    /**
     * Applique la table de conversion des diacritiques et accents parasites issue de [diacriticRepairs].
     *
     * @param text Le texte sur lequel corriger les diacritiques.
     * @param diacriticRepairs La liste des paires "fautif|correct" (ex: "ế|é").
     * @return Le texte nettoyé de ses diacritiques parasites.
     */
    internal fun applyDiacriticRepairs(text: String, diacriticRepairs: List<String>): String {
        if (diacriticRepairs.isEmpty()) {
            return text
        } else {
            var result = text
            for (repair in diacriticRepairs) {
                val parts = repair.split("|")
                if (parts.size == 2) {
                    result = result.replace(parts[0], parts[1])
                }
            }
            return result
        }
    }

    /**
     * Applique les réparations orthographiques du dictionnaire XML [repairs] sur la chaîne [text].
     *
     * @param text Le texte sur lequel appliquer les réparations.
     * @param repairs La liste des réparations au format "fautif|correct".
     * @return Le texte réparé.
     */
    internal fun applySpellingRepairs(text: String, repairs: List<String>): String {
        if (!text.any { it.isLetter() }) {
            return text
        } else {
            var result = text
            for (repair in repairs) {
                val parts = repair.split("|")
                if (parts.size == 2) {
                    val wrong = parts[0].trim().replace("\\'", "'")
                    val correct = parts[1].trim().replace("\\'", "'")
                    val pattern = OcrRegexes.getSpellingRepairPattern(wrong)
                    result = pattern.matcher(result).replaceAll(Matcher.quoteReplacement(correct))
                }
            }
            return result
        }
    }
}
