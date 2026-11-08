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

    /**
     * Extrait le titre principal de la recette d'après la liste des lignes nettoyées [lines] et le vocabulaire [vocabulary].
     *
     * @param lines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param vocabulary Le vocabulaire métier data-driven contenant les dictionnaires XML du projet.
     * @return L'objet [TitleExtractionResult] contenant le titre retenu et la liste des lignes restantes.
     */
    fun extractTitle(lines: List<String>, vocabulary: OcrVocabulary): TitleExtractionResult {
        val candidates = mutableListOf<ScoredTitleCandidate>()
        val actionVerbs = vocabulary.stepActionKeywords.distinct()
        val containsActionRegex = if (actionVerbs.isEmpty()) null else OcrRegexes.getInstructionPrefixPattern(actionVerbs)

        val recipeIngredientWords = Ocr3CategorizerTitleExtractor0_Context.extractRecipeIngredientWords(lines, vocabulary)
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

            // 2. Détection de titre sur 2 lignes (ex: "Büchettes de melon" \n "au jambon italien et au porto")
            if (i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                if (Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector(nextLine, vocabulary) && !OcrInstructionUtils.hasActionVerb(nextLine, vocabulary.stepActionKeywords)) {
                    val combined = "$workingLine $nextLine"
                    val cleanCombined = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber(combined), recipeIngredientWords, vocabulary)
                    val baseCand = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate(cleanCombined, vocabulary, containsActionRegex)
                    if (baseCand) {
                        val (score, details) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(cleanCombined, vocabulary, recipeIngredientWords, firstIngredientWord)
                        candidates.add(ScoredTitleCandidate(cleanCombined, i, 1, score, details))
                    }
                }
            }

            // 3. Traitement de la ligne courante (Exclut les lignes ayant des quantités d'ingrédients)
            val parsedRawLine = IngredientParser.parse(workingLine, vocabulary.culinaryMeasurementUnits)
            val isIngredientQuantityLine = parsedRawLine.quantity != null && 
                    (parsedRawLine.unit != null || parsedRawLine.quantity!! < 50.0)

            if (!isIngredientQuantityLine) {
                val cleanLine = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.postProcessTitle(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber(workingLine), vocabulary).replace(Regex("\\s+[a-zA-Z]$"), "").trim()
                val repairedCandidate = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine(cleanLine, recipeIngredientWords, vocabulary)
                if (Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate(repairedCandidate, vocabulary, containsActionRegex)) {
                    val (score, details) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(repairedCandidate, vocabulary, recipeIngredientWords, firstIngredientWord)
                    candidates.add(ScoredTitleCandidate(repairedCandidate, i, 0, score, details))
                }
            }
        }

        // Logcat de tous les candidats évalués avec leurs détails
        candidates.forEachIndexed { index, candidate ->
            if (vocabulary.logOcr3TitleExtractorCandidateScore.isNotBlank()) {
                LogManager.d(TAG, String.format(vocabulary.logOcr3TitleExtractorCandidateScore, index, candidate.candidateText, candidate.score, candidate.scoreDetails))
            }
        }

        val MIN_TITLE_SCORE_THRESHOLD = 35
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
                skipCounter = bestCandidate.originalLinesToSkip
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
