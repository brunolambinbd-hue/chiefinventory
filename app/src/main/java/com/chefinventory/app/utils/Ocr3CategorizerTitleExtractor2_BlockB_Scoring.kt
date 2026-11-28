package com.chefinventory.app.utils

/**
 * Sous-composant du Bloc B de l'Étape 3 : Filtrage (`isBaseCandidate`) et Scoring (`evaluateScore`) des candidats titres pour [Ocr3CategorizerTitleExtractor].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerTitleExtractor2_BlockB_Scoring {
    private const val MIN_TITLE_LENGTH_CHARS = 5
    private const val MAX_TITLE_LENGTH_CHARS = 80
    private const val MIN_TITLE_WORDS_COUNT = 2

    private const val SCORE_NO_TRAILING_PERIOD = 10
    private const val SCORE_TITLE_KEYWORD_MATCH = 30
    private const val SCORE_CONNECTOR_MATCH = 10
    private const val SCORE_MULTI_WORD_BONUS = 10
    private const val SCORE_RECIPE_INGREDIENT_MATCH = 15
    private const val SCORE_FIRST_INGREDIENT_MATCH = 30

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
        val trimmed = line.trim().trimEnd('.', '!', '?')
        val lower = trimmed.lowercase()

        if (trimmed.length < MIN_TITLE_LENGTH_CHARS || trimmed.length > MAX_TITLE_LENGTH_CHARS) return false
        val wordList = trimmed.split(Regex("\\s+"))
        val isSingleWord = wordList.size < MIN_TITLE_WORDS_COUNT
        val matchesTitleKeyword = vocabulary.titleKeywords.any { it.equals(trimmed, ignoreCase = true) }
        val hasTitleKeyword = (vocabulary.titleKeywords + listOf("nems", "roulades", "cocktail", "carpaccio", "tartare", "gratin", "quiche", "tarte", "soupe", "mousse", "artichaut", "artichauts")).any { kw ->
            val cleanKw = OcrLevenshteinUtils.stripAccents(kw.lowercase()).removeSuffix("s").removeSuffix("x")
            OcrLevenshteinUtils.stripAccents(trimmed.lowercase()).contains(cleanKw)
        }

        val lastChar = line.trim().lastOrNull()
        if (lastChar != null && lastChar in setOf('.', '!', '?')) return false

        if (OcrRegexes.Titles.DISQUALIFYING_NUMBER_START.containsMatchIn(line.trim())) return false

        val invalidChars = setOf("(", ")", "[")
        if (invalidChars.any { trimmed.contains(it) }) return false
        if (OcrRegexes.Titles.HAS_QUANTITY.containsMatchIn(trimmed)) return false
        if (OcrRegexes.Titles.DISQUALIFYING_FRACTION.containsMatchIn(trimmed)) return false
        if (OcrRegexes.Titles.DISQUALIFYING_SPOON.containsMatchIn(trimmed)) return false
        if (OcrInstructionUtils.hasActionVerb(trimmed, vocabulary.stepActionKeywords)) return false

        val invalidBullets = setOf("•", "-", "*")
        if (invalidBullets.any { trimmed.startsWith(it) }) return false

        val categoryHeaderKeys = vocabulary.recipeCategoryHeaders.map { it.split("|")[0].trim() }
        val hasTitleConnectors = listOf(" de ", " d'", " du ", " des ", " aux ", " au ", " à la ", " a la ", " avec ").any { lower.contains(it) }
        val isCategoryHeader = !hasTitleConnectors && categoryHeaderKeys.any { header ->
            val cleanHeader = OcrLevenshteinUtils.stripAccents(header.lowercase())
            val cleanTrimmed = OcrLevenshteinUtils.stripAccents(trimmed.lowercase())
            cleanTrimmed.startsWith(cleanHeader) && cleanTrimmed.split(Regex("\\s+")).size <= 3
        }
        if (isCategoryHeader) return false

        if (vocabulary.excludedOcrKeywords.isNotEmpty()) {
            val isExcludedKeyword = vocabulary.excludedOcrKeywords.any { it.equals(trimmed, ignoreCase = true) }
            if (isExcludedKeyword) {
                return false
            }
        }

        val cleanIngName = trimmed.replace(OcrRegexes.Titles.DE_PREFIX_FULL, "").trim()
        val isCommonIngr = vocabulary.commonIngredientsNoQty.any { it.equals(trimmed, ignoreCase = true) || it.equals(cleanIngName, ignoreCase = true) }
        val isEndMarkerLine = vocabulary.ingredientEndMarkers.any { marker ->
            val cleanM = marker.trim().lowercase()
            lower == cleanM || lower.startsWith("$cleanM ") || lower.endsWith(" $cleanM")
        }
        if (isCommonIngr || isEndMarkerLine) return false

        val isModifierOnly = vocabulary.ingredientPreparationModifiers.any { OcrRegexes.getSpellingRepairPattern(it).containsMatchIn(trimmed) }
        if (!hasTitleKeyword) {
            if (isModifierOnly) return false
            if (cleanIngName.contains("sésame") || cleanIngName.contains("sesame")) return false
        }

        val isSource = Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource(trimmed, vocabulary) || Ocr3CategorizerSource.isSourceLine(trimmed, vocabulary)
        if (isSource) return false

        if (Ocr3CategorizerWine.isWineLine(trimmed, vocabulary)) return false
        if (lower.contains("nutri")) return false

        val headersAndTimes = (vocabulary.ocr1ProtectedIngredientsHeaders + vocabulary.ocr1ProtectedInstructionsHeaders + vocabulary.timeHeaderKeywords)
            .map { it.lowercase().trim() }
        val connectors = (vocabulary.standaloneArticles + vocabulary.ingredientConnectors)
            .map { it.lowercase().trim() }
            .flatMap { listOf("$it ", "$it'", "$it’") }
        val invalidPrefixes = (headersAndTimes + connectors + listOf("tranches de ", "tranche de ", "quelques ", "plusieurs ", "le jus", "jus de")).distinct()

        if (invalidPrefixes.any { prefix -> lower.startsWith(prefix) }) return false

        val hasPersonnes = lower.contains("personnes")
        val hasPortions = lower.contains("portions")
        if (hasPersonnes || hasPortions) {
            return false
        }

        val lastWordList = lower.split(Regex("\\s+"))
        val lastWord = lastWordList.last()
        val stopLastWords = (vocabulary.standaloneArticles + vocabulary.hangingConnectors).map { it.trim().lowercase() }.distinct()
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
        firstIngredientWord: String?,
        lines2D: List<OcrTextLine> = emptyList(),
        candidateLineIndex: Int = 99
    ): Pair<Int, String> {
        var score = 0
        val details = mutableListOf<String>()

        val trimmed = candidate.trim()
        val lower = trimmed.lowercase()

        val hasTitleKeyword = (vocabulary.titleKeywords + listOf("nems", "roulades", "cocktail", "carpaccio", "tartare", "gratin", "quiche", "tarte", "soupe", "mousse", "artichaut", "artichauts")).any { kw ->
            val cleanKw = OcrLevenshteinUtils.stripAccents(kw.lowercase()).removeSuffix("s").removeSuffix("x")
            OcrLevenshteinUtils.stripAccents(trimmed.lowercase()).contains(cleanKw)
        }

        // Critère 0 : Bonus de position initiale en haut de fiche (Index 0 ou 1 : +30 pts si la ligne ne dérive pas d'un ingrédient pur)
        val matchesIngredientWord = !hasTitleKeyword && recipeIngredientWords.any { ing ->
            val cleanIng = OcrLevenshteinUtils.stripAccents(ing.lowercase())
            cleanIng.length >= 4 && OcrLevenshteinUtils.stripAccents(lower).contains(cleanIng)
        }
        if (candidateLineIndex <= 1 && !matchesIngredientWord) {
            score += 30
            details.add("position_initiale_haut_fiche(idx=$candidateLineIndex):+30")
        }

        // Critère 1 : Ponctuation de fin (Un titre ne finit pas par un point)
        score += SCORE_NO_TRAILING_PERIOD
        details.add("sans_point_fin:+$SCORE_NO_TRAILING_PERIOD")

        // Critère 2D : Grand titre en grande police de caractères (Large Font Title Bonus +60 pts si fontHeight >= 70px)
        val line2D = lines2D.find { 
            val cleanCandidate = OcrLevenshteinUtils.stripAccents(trimmed.lowercase())
            val clean2D = OcrLevenshteinUtils.stripAccents(it.text.trim().lowercase())
            clean2D.contains(cleanCandidate) || cleanCandidate.contains(clean2D)
        }
        if (line2D != null) {
            val fontHeight = line2D.bottom - line2D.top
            if (fontHeight >= 120) {
                score += 60
                details.add("hauteur_police_titre_2d(${fontHeight}px):+60")
            }
        }

        // Critère 2a : Mots-clés de titre par Regex Match Exact (+30 pts par mot-clé exact)
        val matchedExactTitleKeywords = vocabulary.titleKeywords.filter { kw ->
            OcrRegexes.getSpellingRepairPattern(kw).containsMatchIn(trimmed)
        }
        if (matchedExactTitleKeywords.isNotEmpty()) {
            val pts = matchedExactTitleKeywords.size * SCORE_TITLE_KEYWORD_MATCH
            score += pts
            details.add("mots_clés_titre_exact($matchedExactTitleKeywords):+$pts")
        }

        // Critère 2b : Mots-clés de titre par Levenshtein Match Flou / Lettrine (+20 pts par mot-clé réparé)
        val wordsInCandidate = trimmed.split(Regex("\\s+")).map { OcrLevenshteinUtils.stripAccents(it.trim().lowercase().trim('.', ',', ';', ':', '!', '?')) }
        val matchedFuzzyTitleKeywords = vocabulary.titleKeywords.filter { kw ->
            val cleanKw = OcrLevenshteinUtils.stripAccents(kw.lowercase())
            !matchedExactTitleKeywords.contains(kw) && wordsInCandidate.any { word ->
                if (word.length >= 5 && cleanKw.length >= 5) {
                    val dist = OcrLevenshteinUtils.distance(word, cleanKw)
                    val maxAllowed = OcrLevenshteinUtils.getMaxAllowedErrors(cleanKw.length)
                    dist in 1..maxAllowed && dist <= 2
                } else false
            }
        }
        if (matchedFuzzyTitleKeywords.isNotEmpty()) {
            val pts = matchedFuzzyTitleKeywords.size * 20
            score += pts
            details.add("mots_clés_titre_flou_lettrine($matchedFuzzyTitleKeywords):+$pts")
        }

        // Critère 3 : Connecteurs et mots de liaison de titre (et, aux, à la, au, et au, et aux)
        val connectorsList = (vocabulary.titleMultilineConnectors + vocabulary.titleLinkingWords).distinct()
        val connectors = connectorsList.map { " ${it.trim().lowercase()} " }
        val matchedConnectors = connectors.filter { lower.contains(it) || lower.startsWith("${it.trim()} ") }
        if (matchedConnectors.isNotEmpty()) {
            val pts = matchedConnectors.size * SCORE_CONNECTOR_MATCH
            score += pts
            details.add("connecteurs_titre($matchedConnectors):+$pts")
        }

        // Critère 4 : Longueur en mots (Un titre comporte au moins 2 mots : +10 pts, 3+ mots : +20 pts de complétude)
        val wordCount = trimmed.split(Regex("\\s+")).size
        if (wordCount >= MIN_TITLE_WORDS_COUNT) {
            score += SCORE_MULTI_WORD_BONUS
            details.add("plus_de_2_mots:+$SCORE_MULTI_WORD_BONUS")
        }
        if (wordCount >= 3) {
            score += 20
            details.add("titre_long_complexe_3mots:+20")
        }

        // Critère 5 : Mots d'ingrédients présents sur la fiche scannée de la recette (+15 pts par ingrédient)
        val matchedRecipeIngredients = recipeIngredientWords.filter { ingWord ->
            val stem = ingWord.removeSuffix("s").removeSuffix("x")
            OcrRegexes.getStemWordPattern(stem).containsMatchIn(trimmed)
        }
        if (matchedRecipeIngredients.isNotEmpty()) {
            val pts = matchedRecipeIngredients.size * SCORE_RECIPE_INGREDIENT_MATCH
            score += pts
            details.add("ingrédient_fiche_recette($matchedRecipeIngredients):+$pts")
        }

        // Critère 6 : Départage par le premier ingrédient de la recette (+30 pts)
        if (firstIngredientWord != null) {
            val stem = firstIngredientWord.removeSuffix("s").removeSuffix("x")
            if (OcrRegexes.getStemWordPattern(stem).containsMatchIn(trimmed)) {
                score += SCORE_FIRST_INGREDIENT_MATCH
                details.add("premier_ingrédient($firstIngredientWord):+$SCORE_FIRST_INGREDIENT_MATCH")
            }
        }

        return Pair(score, details.joinToString(", "))
    }
}
