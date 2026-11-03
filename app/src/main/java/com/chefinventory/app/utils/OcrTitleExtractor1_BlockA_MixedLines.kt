package com.chefinventory.app.utils

import android.util.Log

/**
 * Sous-composant du Bloc A de l'Étape 2.5 : Traite et scinde les lignes mixtes "Sel et Poivre" suivies d'un potentiel titre pour [OcrTitleExtractor].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OcrTitleExtractor1_BlockA_MixedLines {
    private const val TAG = "OcrTitleExtractor1_BlockA"

    internal fun hasEndingConnector(text: String): Boolean {
        val lower = text.lowercase()
        if (lower.endsWith(" et")) return true
        if (lower.endsWith(" d'")) return true
        if (lower.endsWith(" de")) return true
        return lower.endsWith(" aux")
    }

    internal fun processSelPoivreLine(
        workingLine: String,
        lineIndex: Int,
        lines: List<String>,
        vocabulary: OcrVocabulary,
        recipeIngredientWords: Set<String>,
        containsActionRegex: Regex?,
        firstIngredientWord: String?,
        candidates: MutableList<ScoredTitleCandidate>
    ): Boolean {
        val lowerLine = workingLine.lowercase()
        val isSelPoivreLine = lowerLine.contains("sel et poivre")

        if (isSelPoivreLine) {
            val parts = workingLine.split(OcrRegexes.Titles.SPLIT_SEL_POIVRE, 2)
            val rawTitlePart = parts[1].trim()
            if (rawTitlePart.isNotEmpty()) {
                var potentialTitle = OcrTitleExtractor3_BlockC_PostProcess.stripRecipeNumber(rawTitlePart)
                val hasNextLine = lineIndex + 1 < lines.size
                val endingConn = hasEndingConnector(potentialTitle)
                val nextConn = if (hasNextLine) {
                    OcrTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector(lines[lineIndex + 1], vocabulary)
                } else {
                    false
                }

                val isMultiLineCandidate = endingConn || nextConn
                var skipCount = 0
                if (isMultiLineCandidate) {
                    if (hasNextLine) {
                        potentialTitle = "$potentialTitle ${lines[lineIndex + 1].trim()}"
                        skipCount = 1
                    }
                }

                potentialTitle = OcrTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine(potentialTitle, recipeIngredientWords, vocabulary)
                val isBase = OcrTitleExtractor2_BlockB_Scoring.isBaseCandidate(potentialTitle, vocabulary, containsActionRegex)
                if (isBase) {
                    val (score, details) = OcrTitleExtractor2_BlockB_Scoring.evaluateScore(potentialTitle, vocabulary, recipeIngredientWords, firstIngredientWord)
                    candidates.add(ScoredTitleCandidate(potentialTitle, lineIndex, skipCount, score, details))
                    Log.d(TAG, "TITLE EXTRACTOR: Candidate extracted from sel/poivre line -> '$potentialTitle' (score=$score)")
                    return true
                }
            }
        }
        return false
    }
}
