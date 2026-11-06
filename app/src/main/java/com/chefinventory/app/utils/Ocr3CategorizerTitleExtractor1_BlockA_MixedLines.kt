package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Sous-composant du Bloc A de l'Étape 3 : Traite et scinde les lignes mixtes "Sel et Poivre" suivies d'un potentiel titre pour [Ocr3CategorizerTitleExtractor].
 *
 * Responsabilités :
 * - Détecter si une ligne se termine par un connecteur de continuation de titre multi-lignes.
 * - Isoler et extraire un titre de recette accidentellement accolé après une ligne de fin d'ingrédients ("Sel et poivre").
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerTitleExtractor1_BlockA_MixedLines {
    private const val TAG = "Ocr3TitleExtractor1_BlockA"

    /**
     * Détermine si le texte [text] se termine par un connecteur ou une préposition de liaison multi-lignes.
     *
     * @param text Le texte de la ligne à vérifier.
     * @param vocabulary Le vocabulaire métier data-driven contenant les connecteurs de titres.
     * @return `true` si le texte se termine par un connecteur suspendu, `false` sinon.
     */
    internal fun hasEndingConnector(text: String, vocabulary: OcrVocabulary? = null): Boolean {
        val lower = text.lowercase()
        val activeVocab = vocabulary ?: OcrVocabularyLoader.loadFromProjectResources()
        val connectors = (activeVocab.titleMultilineConnectors + activeVocab.hangingConnectors)
            .map { " ${it.trim().lowercase()}" }
            .distinct()
        return connectors.any { lower.endsWith(it) }
    }

    /**
     * Traite et extrait un potentiel candidat titre situé sur la même ligne qu'un marqueur de fin d'ingrédients ("Sel et poivre").
     *
     * @param workingLine La ligne nettoyée courante.
     * @param lineIndex L'index de la ligne dans la liste globale de la recette.
     * @param lines La liste complète des lignes de la recette.
     * @param vocabulary Le vocabulaire métier data-driven.
     * @param recipeIngredientWords L'ensemble des mots-clés d'ingrédients vus sur la fiche.
     * @param containsActionRegex Expression régulière des verbes d'action.
     * @param firstIngredientWord Le premier mot d'ingrédient significatif.
     * @param candidates La liste mutable des candidats titres scorés à alimenter.
     * @return `true` si un candidat titre a été extrait et ajouté depuis cette ligne mixte, `false` sinon.
     */
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
        val endMarker = vocabulary.ingredientEndMarkers.firstOrNull { it.contains("sel") } ?: ""
        val isSelPoivreLine = endMarker.isNotEmpty() && lowerLine.contains(endMarker.lowercase())

        if (isSelPoivreLine) {
            val parts = workingLine.split(OcrRegexes.Titles.SPLIT_SEL_POIVRE, 2)
            val rawTitlePart = if (parts.size > 1) parts[1].trim() else ""
            if (rawTitlePart.isNotEmpty()) {
                var potentialTitle = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber(rawTitlePart)
                val hasNextLine = lineIndex + 1 < lines.size
                val endingConn = hasEndingConnector(potentialTitle, vocabulary)
                val nextConn = if (hasNextLine) {
                    Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector(lines[lineIndex + 1], vocabulary)
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

                potentialTitle = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine(potentialTitle, recipeIngredientWords, vocabulary)
                val isBase = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.isBaseCandidate(potentialTitle, vocabulary, containsActionRegex)
                if (isBase) {
                    val (score, details) = Ocr3CategorizerTitleExtractor2_BlockB_Scoring.evaluateScore(potentialTitle, vocabulary, recipeIngredientWords, firstIngredientWord)
                    candidates.add(ScoredTitleCandidate(potentialTitle, lineIndex, skipCount, score, details))
                    LogManager.d(TAG, String.format(vocabulary.logOcr3TitleExtractorSelPoivreCandidate, potentialTitle, score))
                    return true
                }
            }
        }
        return false
    }
}
