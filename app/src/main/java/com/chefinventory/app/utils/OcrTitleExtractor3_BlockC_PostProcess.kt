package com.chefinventory.app.utils

import android.util.Log
import java.util.regex.Matcher

/**
 * Sous-composant du Bloc C de l'Étape 2.5 : Post-traitement, réparation de lettrine et nettoyage du titre pour [OcrTitleExtractor].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OcrTitleExtractor3_BlockC_PostProcess {
    private const val TAG = "OcrTitleExtractor3_BlockC"

    /**
     * Post-traite et applique les réparations orthographiques du dictionnaire XML [vocabulary.titleSpellingRepairs] au titre retenu.
     *
     * @param title Le titre à réparer.
     * @param vocabulary Le vocabulaire métier contenant les réparations de titres (`ocr_step25_title_repairs.xml`).
     * @return Le titre réparé.
     */
    internal fun postProcessTitle(title: String, vocabulary: OcrVocabulary): String {
        var result = title.trim()
        for (repair in vocabulary.titleSpellingRepairs) {
            val parts = repair.split("|")
            val isTwoParts = parts.size == 2
            if (isTwoParts) {
                val wrong = parts[0].trim().replace("\\'", "'")
                val correct = parts[1].trim().replace("\\'", "'")
                val pattern = OcrRegexes.getSpellingRepairPattern(wrong)
                result = pattern.matcher(result).replaceAll(Matcher.quoteReplacement(correct))
            } else {
                Log.d(TAG, "  [SKIP] Règle de réparation invalide sans séparateur: '$repair'")
            }
        }
        return result
    }

    /**
     * Vérifie si une ligne secondaire commence par un connecteur ou une préposition de suite de titre multi-lignes.
     *
     * @param line La ligne secondaire à analyser.
     * @param vocabulary Le vocabulaire métier data-driven (`ocr_step25_title_repairs.xml`).
     * @return `true` si la ligne commence par un connecteur de titre (ex: "au jambon", "aux crevettes"), `false` sinon.
     */
    internal fun isMultiLineTitleConnector(line: String, vocabulary: OcrVocabulary?): Boolean {
        val lower = line.lowercase().trim()
        val defaultConnectors = listOf("au ", "aux ", "et ", "à la ", "a la ", "à ", "de ", "d'", "du ", "des ", "avec ", "sur ")
        val rawConnectors = if (vocabulary != null) {
            vocabulary.titleMultilineConnectors
        } else {
            emptyList()
        }
        val connectors = if (rawConnectors.isEmpty()) {
            defaultConnectors
        } else {
            rawConnectors.map { " ${it.trim().lowercase()} " }
        }
        return connectors.any { lower.startsWith(it.trimStart()) || lower.startsWith(it.trim()) }
    }

    internal fun isMultiLineTitleConnector(line: String): Boolean {
        return isMultiLineTitleConnector(line, null)
    }

    /**
     * Répare la lettrine / première lettre du premier mot du titre si le suffixe matche un ingrédient de la fiche scannée.
     *
     * @param titleCandidate Le candidat titre à réparer (ex: "Drabe mangue").
     * @param candidateIngredients Les mots d'ingrédients inventoriés sur la fiche (ex: "crabe").
     * @param vocabulary Le vocabulaire métier OCR.
     * @return Le titre réparé (ex: "Crabe mangue").
     */
    internal fun repairFirstWordLettrine(titleCandidate: String, candidateIngredients: Set<String>, vocabulary: OcrVocabulary): String {
        val trimmed = titleCandidate.trim()
        if (trimmed.isEmpty()) return trimmed

        val words = trimmed.split(Regex("\\s+"))
        val firstWord = words[0]
        val cleanFirstWord = firstWord.replace(OcrRegexes.Titles.NON_LETTER_PREFIX, "").replace(OcrRegexes.Titles.SLASH_PIPE_SYMBOLS, "")
        if (cleanFirstWord.length < 3) return trimmed

        val lowerFirst = cleanFirstWord.lowercase()
        val allIngredients = (candidateIngredients + vocabulary.titleKeywords + vocabulary.commonIngredientsNoQty).map { it.lowercase() }.distinct()

        if (allIngredients.any { it == lowerFirst }) {
            return trimmed
        }

        var matchedIngredient = allIngredients.firstOrNull { ing ->
            ing.length > lowerFirst.length && ing.endsWith(lowerFirst)
        }

        if (matchedIngredient == null) {
            val suffix = lowerFirst.drop(1)
            matchedIngredient = allIngredients.firstOrNull { ing ->
                ing.length > suffix.length && ing.endsWith(suffix)
            }
        }

        if (matchedIngredient != null) {
            val firstCharUpper = matchedIngredient[0].uppercaseChar()
            val repairedFirstWord = firstCharUpper + matchedIngredient.substring(1)
            val repairedText = (listOf(repairedFirstWord) + words.drop(1)).joinToString(" ")
            Log.d(TAG, "TITLE EXTRACTOR: Lettrine réparée -> '$titleCandidate' devenant '$repairedText' (via '$matchedIngredient')")
            return repairedText
        }

        return trimmed
    }

    /**
     * Détermine si une ligne nettoyée est un nom orphelin éligible pour compléter un titre suspendu.
     */
    internal fun isEligibleOrphanNoun(
        line: String,
        isFeminineAdj: Boolean,
        actionKeywords: List<String>
    ): Boolean {
        val cleanL = line.lowercase().trim().removeSuffix(".")
        val numberWords = setOf("un", "une", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix")
        val stopInOrphanNoun = setOf("les", "des", "pour", "dans", "sur", "avec", "par", "tout", "melon")

        val matchesGender = if (isFeminineAdj) cleanL.endsWith("e") else true
        val isCorrectLen = cleanL.length in 3..20
        val noSpace = !cleanL.contains(" ")
        val isNotNumber = cleanL !in numberWords
        val hasVerb = OcrInstructionUtils.hasActionVerb(cleanL, actionKeywords)
        val isNotStop = cleanL !in stopInOrphanNoun

        if (!isCorrectLen) return false
        if (!noSpace) return false
        if (!matchesGender) return false
        if (!isNotNumber) return false
        if (hasVerb) return false
        return isNotStop
    }

    /**
     * Completes suspended orphan nouns at the end of titles (e.g. "Petite salade").
     */
    internal fun recoverOrphanNoun(
        extractedTitle: String?,
        remainingLines: MutableList<String>,
        vocabulary: OcrVocabulary
    ): String? {
        if (extractedTitle == null) return null
        var titleResult = extractedTitle
        val lowerTitle = titleResult.lowercase().trim()
        val trailingAdjRegex = Regex("(?i)\\b(?:petite|petites|grand|grande|grands|grandes|beau|beaux|belle|belles|façon|facon|fa\u00e7ons|facons|sa|son|ses|aux|au|avec)\\s*[a-z]?$")

        if (trailingAdjRegex.containsMatchIn(lowerTitle)) {
            val isPetite = lowerTitle.endsWith("petite")
            val isPetiteS = lowerTitle.endsWith("petite s")
            val isGrande = lowerTitle.endsWith("grande")
            val isBelle = lowerTitle.endsWith("belle")
            val isFeminineAdj = if (isPetite) {
                true
            } else if (isPetiteS) {
                true
            } else if (isGrande) {
                true
            } else {
                isBelle
            }

            val orphanNounIdx = remainingLines.indexOfFirst { line ->
                isEligibleOrphanNoun(line, isFeminineAdj, vocabulary.stepActionKeywords)
            }

            if (orphanNounIdx != -1) {
                val orphanNoun = remainingLines[orphanNounIdx].trim().removeSuffix(".")
                val baseTitle = titleResult.replace(Regex("\\s+[a-zA-Z]$"), "").trim()
                titleResult = "$baseTitle $orphanNoun"
                remainingLines.removeAt(orphanNounIdx)
                Log.d(TAG, "TITLE EXTRACTOR: Title completed with orphan noun -> '$titleResult' (consumed line: '$orphanNoun')")
            } else {
                Log.d(TAG, "  [INFO] Aucun nom orphelin trouvé dans les lignes restantes")
            }
        }
        return titleResult
    }

    /**
     * Supprime le numéro de fiche ou de recette en début de titre (ex: "12 Salade grecque" -> "Salade grecque").
     *
     * @param text Le texte brut du candidat titre.
     * @return Le titre sans son numéro de fiche initial.
     */
    internal fun stripRecipeNumber(text: String): String {
        var cleaned = text.trim()
        cleaned = cleaned.replace(OcrRegexes.Titles.RECIPE_NUMBER_PREFIX, "")
        if (cleaned.isNotEmpty()) {
            val firstCharUpper = cleaned[0].uppercaseChar()
            cleaned = firstCharUpper + cleaned.substring(1)
        } else {
            Log.d(TAG, "  [INFO] Chaîne vide après stripRecipeNumber")
        }
        return cleaned
    }
}
