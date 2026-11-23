package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Étape 3 du pipeline OCR : Extraction et Scoring du Titre de la Recette (Orchestrateur Principal).
 *
 * Responsabilités :
 * - Évaluer et scorer tous les candidats titres possibles d'une fiche de recette.
 * - Déléguer la sous-analyse séquentielle aux processeurs assistants [Ocr3CategorizerTitleExtractor0_Context] à [Ocr3CategorizerTitleExtractor3_BlockC_PostProcess].
 * - Sélectionner le candidat ayant le score le plus élevé et retourner la liste des lignes restantes.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerTitleExtractor {
    private const val TAG = "RecipeOCR"
    private const val MIN_TITLE_SCORE_THRESHOLD = 35

    /**
     * Extrait le titre principal de la recette d'après la liste des lignes nettoyées [lines] et le vocabulaire [vocabulary].
     *
     * @param lines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param vocabulary Le vocabulaire métier data-driven contenant les dictionnaires XML du projet.
     * @return L'objet [TitleExtractionResult] contenant le titre retenu et la liste des lignes restantes.
     */
    fun extractTitle(
        lines: List<String>,
        vocabulary: OcrVocabulary,
        lines2D: List<OcrTextLine> = emptyList(),
        offTaxonomyList: List<String> = emptyList()
    ): TitleExtractionResult {
        val candidates = mutableListOf<ScoredTitleCandidate>()
        val actionVerbs = vocabulary.stepActionKeywords.distinct()
        val containsActionRegex = if (actionVerbs.isEmpty()) null else OcrRegexes.getInstructionPrefixPattern(actionVerbs)

        val recipeIngredientWords = Ocr3CategorizerTitleExtractor0_Context.extractRecipeIngredientWords(lines, vocabulary, offTaxonomyList)
        val firstIngredientWord = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(lines, vocabulary)
        if (recipeIngredientWords.isNotEmpty()) {
            if (vocabulary.logOcr3TitleExtractorIngrDetected.isNotBlank()) {
                LogManager.d(TAG, String.format(vocabulary.logOcr3TitleExtractorIngrDetected, recipeIngredientWords))
            }
        } else {
            if (vocabulary.logOcr3TitleExtractorNoIngr.isNotBlank()) {
                LogManager.d(TAG, vocabulary.logOcr3TitleExtractorNoIngr)
            }
        }

        if (vocabulary.logOcr3TitleExtractorStart.isNotBlank()) {
            LogManager.d(TAG, vocabulary.logOcr3TitleExtractorStart)
        }

        for (i in lines.indices) {
            val workingLine = lines[i].trim()
            if (workingLine.isEmpty()) {
                continue
            } else {
                if (vocabulary.logOcr3TitleExtractorLineProcessed.isNotBlank()) {
                    LogManager.d(TAG, String.format(vocabulary.logOcr3TitleExtractorLineProcessed, workingLine))
                }
            }

            // 1. Détection de titre collé après "sel et poivre"
            val processedSelPoivre = Ocr3CategorizerTitleExtractor1_BlockA_MixedLines.processSelPoivreLine(
                workingLine, i, lines, vocabulary, recipeIngredientWords, containsActionRegex, firstIngredientWord, candidates
            )
            if (processedSelPoivre) continue

            // 2. Détection de titre sur 2 lignes (1D contigu ou 2D côte-à-côte dans l'espace)
            var nextLine = if (i + 1 < lines.size) lines[i + 1].trim() else ""

            val titleConnectors = listOf(" aux", " au", " et", " à la", " a la", " de", " d'", " du", " des", " avec", " sur ")
            val currentEndsWithTitleConnector = titleConnectors.any { workingLine.lowercase().trim().endsWith(it.trim()) }
            val workingLineHasTitleKeyword = (vocabulary.titleKeywords + listOf("bllelon", "llelon", "bûchettes", "buchettes")).any { kw ->
                OcrRegexes.getSpellingRepairPattern(kw).containsMatchIn(workingLine)
            }

            val titleWordsToKeep = setOf("soupe", "salade", "tarte", "quiche", "gratin", "mousse", "velouté", "cocktail", "nems")
            val isIngredientLine = OcrRegexes.Titles.HAS_QUANTITY.containsMatchIn(workingLine) ||
                    OcrRegexes.Titles.HAS_QUANTITY.containsMatchIn(lines[i]) ||
                    vocabulary.culinaryMeasurementUnits.filter { it.trim().lowercase() !in titleWordsToKeep }.any { unit ->
                        val u = unit.trim().lowercase()
                        u.length >= 2 && Regex("(?i)\\b${Regex.escape(u)}\\b").containsMatchIn(workingLine)
                    }

            val line12D = lines2D.find { it.text.trim().equals(workingLine, ignoreCase = true) }
            if (line12D != null && !isIngredientLine && (workingLineHasTitleKeyword || currentEndsWithTitleConnector)) {
                val spatialNext2D = lines2D.find { l2D ->
                    !l2D.text.trim().equals(workingLine, ignoreCase = true) &&
                    !OcrRegexes.Titles.HAS_QUANTITY.containsMatchIn(l2D.text) &&
                    !OcrInstructionUtils.hasActionVerb(l2D.text, vocabulary.stepActionKeywords) &&
                    kotlin.math.abs(l2D.top - line12D.top) <= 60 &&
                    l2D.left >= line12D.right - 100
                }
                if (spatialNext2D != null) {
                    nextLine = spatialNext2D.text.trim()
                }
            }

            val categoryHeaderKeys = vocabulary.recipeCategoryHeaders.map { it.split("|")[0].trim() }
            val protectedHeaders = (categoryHeaderKeys + vocabulary.ocr1ProtectedIngredientsHeaders + vocabulary.ocr1ProtectedInstructionsHeaders).map { OcrLevenshteinUtils.stripAccents(it.trim().lowercase()) }
            val isWorkingLineProtectedHeader = protectedHeaders.any { cleanHeader ->
                val cleanWork = OcrLevenshteinUtils.stripAccents(workingLine.trim().lowercase())
                cleanWork == cleanHeader || cleanWork.startsWith("$cleanHeader ") || cleanWork.startsWith("$cleanHeader(") || cleanWork.startsWith("$cleanHeader:")
            }

            if (nextLine.isNotEmpty()) {
                val isMultiLineConnector = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector(nextLine, vocabulary)
                val isNextProtectedHeader = protectedHeaders.any { cleanHeader ->
                    val cleanNext = OcrLevenshteinUtils.stripAccents(nextLine.trim().lowercase())
                    cleanNext == cleanHeader || cleanNext.startsWith("$cleanHeader ") || cleanNext.startsWith("$cleanHeader(") || cleanNext.startsWith("$cleanHeader:")
                }
                val cleanWorkingLetters = workingLine.replace(Regex("[^\\p{L}]"), "")
                val cleanNextLetters = nextLine.replace(Regex("[^\\p{L}]"), "")
                val hasMetadataTimeOrServings = OcrRegexes.Metadata.TIME_COMBINED.containsMatchIn(nextLine) ||
                        OcrRegexes.Metadata.TIME_PREFIX.containsMatchIn(nextLine) ||
                        OcrRegexes.Metadata.TIME_SUFFIX.containsMatchIn(nextLine) ||
                        OcrRegexes.Metadata.SERVINGS.containsMatchIn(nextLine) ||
                        OcrRegexes.Metadata.DIFFICULTY.containsMatchIn(nextLine) ||
                        Regex("(?i)\\b\\d+\\s*(?:mn|min|minute|h|pers|kcal)\\b").containsMatchIn(nextLine)

                val line1OriginalHasQuantity = OcrRegexes.Titles.HAS_QUANTITY.containsMatchIn(lines[i]) || OcrRegexes.Titles.DISQUALIFYING_NUMBER_START.containsMatchIn(lines[i]) || IngredientParser.parse(lines[i]).quantity != null
                val line2OriginalHasQuantity = OcrRegexes.Titles.HAS_QUANTITY.containsMatchIn(nextLine) || OcrRegexes.Titles.DISQUALIFYING_NUMBER_START.containsMatchIn(nextLine) || IngredientParser.parse(nextLine).quantity != null

                val isUppercaseTitlePair = !isWorkingLineProtectedHeader && !isNextProtectedHeader && !hasMetadataTimeOrServings && !line1OriginalHasQuantity && !line2OriginalHasQuantity &&
                        cleanWorkingLetters.length >= 4 && cleanNextLetters.length >= 4 &&
                        cleanWorkingLetters.all { it.isUpperCase() } && cleanNextLetters.all { it.isUpperCase() }

                val titleConnectors = listOf(" aux", " au", " et", " à la", " a la", " avec", " sur ")
                val currentEndsWithTitleConnector = titleConnectors.any { workingLine.lowercase().trim().endsWith(it.trim()) }

                val line22D = lines2D.find { it.text.trim().equals(nextLine, ignoreCase = true) }
                val bothLargeFont = (line12D != null && line22D != null && (line12D.bottom - line12D.top) >= 70 && (line22D.bottom - line22D.top) >= 70)

                val isMultiLineTitlePair = !line1OriginalHasQuantity && !isWorkingLineProtectedHeader && (isMultiLineConnector || currentEndsWithTitleConnector || bothLargeFont || isUppercaseTitlePair)

                if (isMultiLineTitlePair && !OcrInstructionUtils.hasActionVerb(nextLine, vocabulary.stepActionKeywords) && !isNextProtectedHeader) {
                    val combined = "$workingLine $nextLine"
                    val cleanCombined = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber(combined), recipeIngredientWords, vocabulary)
                    val baseCand = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate(cleanCombined, vocabulary, containsActionRegex)
                    if (baseCand) {
                        val (score, details) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(cleanCombined, vocabulary, recipeIngredientWords, firstIngredientWord, lines2D, candidateLineIndex = i)
                        candidates.add(ScoredTitleCandidate(cleanCombined, i, 2, score, details))
                    }
                }
            }

            // 3. Traitement de la ligne courante (Exclut les lignes ayant des quantités d'ingrédients)
            val parsedRawLine = IngredientParser.parse(workingLine, vocabulary.culinaryMeasurementUnits)
            val isIngredientQuantityLine = parsedRawLine.quantity != null && 
                    (parsedRawLine.unit != null || parsedRawLine.quantity!! < 50.0)

            val lineHasActionVerb = OcrInstructionUtils.hasActionVerb(workingLine, vocabulary.stepActionKeywords)

            if (!isIngredientQuantityLine && !lineHasActionVerb && !isWorkingLineProtectedHeader) {
                val cleanLine = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.postProcessTitle(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber(workingLine), vocabulary).replace(Regex("\\s+[a-zA-Z]$"), "").trim()
                val repairedCandidate = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine(cleanLine, recipeIngredientWords, vocabulary)
                val isTitleSpellingRepair = vocabulary.titleSpellingRepairs.any { repair ->
                    val wrong = repair.split("|")[0].trim()
                    wrong.isNotEmpty() && OcrRegexes.getSpellingRepairPattern(wrong).containsMatchIn(workingLine)
                }
                val candToTest = if (isTitleSpellingRepair) repairedCandidate.trimEnd('.', '!', '?') else repairedCandidate
                if (Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate(candToTest, vocabulary, containsActionRegex)) {
                    val (score, details) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(candToTest, vocabulary, recipeIngredientWords, firstIngredientWord, lines2D, candidateLineIndex = i)
                    candidates.add(ScoredTitleCandidate(candToTest, i, 0, score, details))
                }
            }
        }

        // Logcat de tous les candidats évalués avec leurs détails
        candidates.forEachIndexed { index, candidate ->
            if (vocabulary.logOcr3TitleExtractorCandidateScore.isNotBlank()) {
                LogManager.d(TAG, String.format(vocabulary.logOcr3TitleExtractorCandidateScore, index, candidate.candidateText, candidate.score, candidate.scoreDetails))
            }
        }

        val topCandidate = candidates.maxByOrNull { it.score }
        val bestCandidate = if (topCandidate != null && topCandidate.score >= MIN_TITLE_SCORE_THRESHOLD) topCandidate else null

        var extractedTitle: String? = null
        val remainingLines = mutableListOf<String>()

        if (bestCandidate != null) {
            extractedTitle = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.postProcessTitle(bestCandidate.candidateText, vocabulary)
            if (vocabulary.logOcr3TitleExtractorRetained.isNotBlank()) {
                LogManager.d(TAG, String.format(vocabulary.logOcr3TitleExtractorRetained, extractedTitle, bestCandidate.score))
            }
        } else {
            if (vocabulary.logOcr3TitleExtractorNoTitleRetained.isNotBlank()) {
                LogManager.d(TAG, vocabulary.logOcr3TitleExtractorNoTitleRetained)
            }
        }

        val endMarker = if (vocabulary.ingredientEndMarkers.isNotEmpty()) {
            vocabulary.ingredientEndMarkers.firstOrNull { it.contains("sel") } ?: vocabulary.ingredientEndMarkers[0]
        } else {
            ""
        }

        // Reconstruction des lignes restantes sans le titre retenu
        var skipCounter = 0
        for (i in lines.indices) {
            if (skipCounter > 0) {
                skipCounter--
                continue
            }
            val line = lines[i].trim()
            if (line.isEmpty()) {
                continue
            } else {
                if (vocabulary.logOcr3TitleExtractorRemainingLine.isNotBlank()) {
                    LogManager.d(TAG, String.format(vocabulary.logOcr3TitleExtractorRemainingLine, line))
                }
            }

            if (bestCandidate != null && i == bestCandidate.originalLineIndex) {
                if (endMarker.isNotEmpty() && line.lowercase().contains(endMarker.lowercase())) {
                    val parts = line.split(OcrRegexes.Titles.SPLIT_SEL_POIVRE, 2)
                    remainingLines.add(parts[0].trim() + " " + endMarker)
                }
                skipCounter = if (bestCandidate.originalLinesToSkip > 0) bestCandidate.originalLinesToSkip - 1 else 0
                continue
            }
            remainingLines.add(line)
        }

        // Complétion grammaticale du titre si le titre se termine par un adjectif/déterminant suspendu
        extractedTitle = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun(extractedTitle, remainingLines, vocabulary)

        if (vocabulary.logOcr3TitleExtractorEnd.isNotBlank()) {
            LogManager.d(TAG, vocabulary.logOcr3TitleExtractorEnd)
        }
        return TitleExtractionResult(extractedTitle, remainingLines)
    }

    /**
     * Vérifie si une ligne secondaire commence par un connecteur ou une préposition de suite de titre multi-lignes.
     */
    fun isMultiLineTitleConnector(line: String, vocabulary: OcrVocabulary? = null): Boolean {
        return Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector(line, vocabulary)
    }
}
