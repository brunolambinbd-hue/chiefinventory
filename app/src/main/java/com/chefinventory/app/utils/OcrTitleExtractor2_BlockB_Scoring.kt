package com.chefinventory.app.utils

import android.util.Log

/**
 * Sous-composant du Bloc B de l'Étape 2.5 : Filtrage (`isBaseCandidate`) et Scoring (`evaluateScore`) des candidats titres pour [OcrTitleExtractor].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OcrTitleExtractor2_BlockB_Scoring {
    private const val TAG = "OcrTitleExtractor2_BlockB"

    /**
     * Détermine si la ligne constitue une ébauche de candidat titre valide d'après les critères fondamentaux.
     *
     * @param line La ligne de texte à évaluer.
     * @param vocabulary Le vocabulaire métier OCR.
     * @param containsActionRegex Expression régulière pré-compilée des verbes d'action culinaires.
     * @return `true` si la ligne est un candidat titre valide, `false` si elle est disqualifiée.
     */
    internal fun isBaseCandidate(
        line: String,
        vocabulary: OcrVocabulary,
        containsActionRegex: Regex?
    ): Boolean {
        val trimmed = line.trim()
        val lower = trimmed.lowercase()

        if (trimmed.length < 5 || trimmed.length > 80) return false
        val wordList = trimmed.split(Regex("\\s+"))
        val isSingleWord = wordList.size < 2
        val matchesTitleKeyword = vocabulary.titleKeywords.any { it.equals(trimmed, ignoreCase = true) }
        if (isSingleWord && !matchesTitleKeyword) return false

        val endsWithDot = trimmed.endsWith(".")
        val endsWithExcl = trimmed.endsWith("!")
        val endsWithQuestion = trimmed.endsWith("?")
        if (endsWithDot || endsWithExcl || endsWithQuestion) return false

        if (OcrRegexes.Titles.DISQUALIFYING_NUMBER_START.containsMatchIn(line.trim())) return false

        val invalidChars = setOf("(", ")", "[")
        if (invalidChars.any { trimmed.contains(it) }) return false
        if (OcrRegexes.Titles.HAS_QUANTITY.containsMatchIn(trimmed)) return false
        if (OcrRegexes.Titles.DISQUALIFYING_FRACTION.containsMatchIn(trimmed)) return false
        if (OcrRegexes.Titles.DISQUALIFYING_SPOON.containsMatchIn(trimmed)) return false
        if (OcrInstructionUtils.hasActionVerb(trimmed, vocabulary.stepActionKeywords)) return false

        val invalidBullets = setOf("•", "-", "*")
        if (invalidBullets.any { trimmed.startsWith(it) }) return false

        if (vocabulary.excludedOcrKeywords.isNotEmpty()) {
            val isExcludedKeyword = vocabulary.excludedOcrKeywords.any { it.equals(trimmed, ignoreCase = true) }
            if (isExcludedKeyword) {
                return false
            } else {
                Log.d(TAG, "  [OK] Pas de mot-clé d'exclusion OCR")
            }
        } else {
            Log.d(TAG, "  [OK] Liste de mots-clés d'exclusion vide")
        }

        val cleanIngName = trimmed.replace(OcrRegexes.Titles.DE_PREFIX_FULL, "").trim()
        val isCommonIngr = vocabulary.commonIngredientsNoQty.any { it.equals(trimmed, ignoreCase = true) || it.equals(cleanIngName, ignoreCase = true) }
        if (isCommonIngr) return false

        val hasTitleKeyword = vocabulary.titleKeywords.any { OcrRegexes.getSpellingRepairPattern(it).matcher(trimmed).find() }
        val isModifierOnly = vocabulary.ingredientPreparationModifiers.any { OcrRegexes.getSpellingRepairPattern(it).matcher(trimmed).find() }
        if (!hasTitleKeyword && isModifierOnly) return false

        val isSource = OcrHelperUtils.isLikelyProperNameOrSource(trimmed, vocabulary) || SourceParser.isSourceLine(trimmed, vocabulary)
        if (isSource) return false

        if (WineParser.isWineLine(trimmed, vocabulary)) return false

        val invalidPrefixes = setOf(
            "d'", "l'", "de ", "du ", "des ", "de la ", "de l'", "ou ", "à ", "a ",
            "tranches de ", "tranche de ", "quelques ", "plusieurs ", "ingrédients",
            "ingredients", "préparation", "cuisson", "repos", "pour ", "le jus", "jus de"
        )
        if (invalidPrefixes.any { lower.startsWith(it) }) return false

        val hasPersonnes = lower.contains("personnes")
        if (hasPersonnes) {
            return false
        } else {
            val hasPortions = lower.contains("portions")
            if (hasPortions) {
                return false
            } else {
                Log.d(TAG, "  [OK] Pas de mention de personnes/portions")
            }
        }

        val lastWordList = lower.split(Regex("\\s+"))
        val lastWord = lastWordList.last()
        val stopLastWords = (vocabulary.standaloneArticles + vocabulary.hangingConnectors + listOf("ce", "que")).map { it.trim().lowercase() }.distinct()
        if (lastWord in stopLastWords) return false

        val letterCount = trimmed.count { it.isLetter() }
        val digitCount = trimmed.count { it.isDigit() }
        val hasEnoughLetters = letterCount >= 4
        val hasFewDigits = digitCount <= 2
        return hasEnoughLetters && hasFewDigits
    }

    /**
     * Évalue et calcule le score numérique d'un candidat titre d'après un barème de 7 critères pondérés.
     *
     * @param candidate Le texte du candidat titre à évaluer.
     * @param vocabulary Le vocabulaire métier data-driven (`ocr_shared_title_keywords.xml`).
     * @param recipeIngredientWords Les mots d'ingrédients inventoriés sur la fiche scannée.
     * @param firstIngredientWord Le nom du premier ingrédient de la recette pour le départage.
     * @return Une paire `(scoreTotal, detailReglesCalculees)` permettant d'expliquer le score dans les logs Logcat.
     */
    internal fun evaluateScore(
        candidate: String,
        vocabulary: OcrVocabulary,
        recipeIngredientWords: Set<String>,
        firstIngredientWord: String?
    ): Pair<Int, String> {
        var score = 0
        val details = mutableListOf<String>()

        val trimmed = candidate.trim()
        val lower = trimmed.lowercase()

        // Critère 1 : Ponctuation de fin (Un titre ne finit pas par un point)
        score += 10
        details.add("sans_point_fin:+10")

        // Critère 2 : Mots-clés de titre (fichiers XML : ocr_shared_title_keywords.xml)
        val matchedTitleKeywords = vocabulary.titleKeywords.filter { kw ->
            OcrRegexes.getSpellingRepairPattern(kw).matcher(trimmed).find()
        }
        if (matchedTitleKeywords.isNotEmpty()) {
            val pts = matchedTitleKeywords.size * 30
            score += pts
            details.add("mots_clés_titre($matchedTitleKeywords):+$pts")
        }

        // Critère 3 : Connecteurs de titre (et, aux, à la, au)
        val defaultConnectors = listOf(" et ", " aux ", " à la ", " au ", " d'", " de ")
        val rawConnectors = vocabulary.titleMultilineConnectors
        val connectorsList = if (rawConnectors.isEmpty()) {
            defaultConnectors
        } else {
            rawConnectors
        }
        val connectors = connectorsList.map { " ${it.trim()} " }
        val matchedConnectors = connectors.filter { lower.contains(it) }
        if (matchedConnectors.isNotEmpty()) {
            val pts = matchedConnectors.size * 10
            score += pts
            details.add("connecteurs_titre($matchedConnectors):+$pts")
        }

        // Critère 4 : Longueur en mots (Un titre comporte au moins 2 mots)
        val wordCount = trimmed.split(Regex("\\s+")).size
        if (wordCount >= 2) {
            score += 10
            details.add("plus_de_2_mots:+10")
        }

        // Critère 5 : Mots d'ingrédients présents sur la fiche scannée de la recette (+15 pts par ingrédient)
        val matchedRecipeIngredients = recipeIngredientWords.filter { ingWord ->
            val stem = ingWord.removeSuffix("s").removeSuffix("x")
            OcrRegexes.getStemWordPattern(stem).containsMatchIn(trimmed)
        }
        if (matchedRecipeIngredients.isNotEmpty()) {
            val pts = matchedRecipeIngredients.size * 15
            score += pts
            details.add("ingrédient_fiche_recette($matchedRecipeIngredients):+$pts")
        }

        // Critère 6 : Départage par le premier ingrédient de la recette (+30 pts)
        if (firstIngredientWord != null) {
            val stem = firstIngredientWord.removeSuffix("s").removeSuffix("x")
            if (OcrRegexes.getStemWordPattern(stem).containsMatchIn(trimmed)) {
                score += 30
                details.add("premier_ingrédient($firstIngredientWord):+30")
            }
        }

        return Pair(score, details.joinToString(", "))
    }
}
