package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Résultat de l'analyse du Bloc A (Méta-données & Continuation de phrase suspendue).
 *
 * @property shouldContinue `true` si la ligne a été traitée et nécessite un `continue` dans la boucle principale.
 * @property workingLine La ligne nettoyée en cours de traitement.
 * @property lowerLine La ligne en minuscules.
 * @property containsAction Indique si la ligne contient un verbe d'action culinaire.
 * @property startsWithBullet Indique si la ligne commence par une puce.
 * @property startsWithConnector Indique si la ligne commence par un connecteur d'ingrédient.
 */
internal data class BlockAResult(
    val shouldContinue: Boolean,
    val workingLine: String,
    val lowerLine: String,
    val containsAction: Boolean,
    val startsWithBullet: Boolean,
    val startsWithConnector: Boolean
)

/**
 * Sous-composant du Bloc A de l'Étape 3 : Traite les dimensions techniques, extrait les méta-données et fusionne les phrases suspendues.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3Categorizer1_BlockA_Metadata {
    private const val TAG = "Ocr3Categorizer1_BlockA"

    /**
     * Bloc A : Traite les dimensions techniques, extrait les méta-données et fusionne les phrases suspendues.
     *
     * @param rawLine La ligne brute courante.
     * @param results Le conteneur [RawSections] d'accumulation.
     * @param vocabulary Le vocabulaire métier.
     * @param containsActionRegex L'expression régulière de détection des verbes d'action.
     * @param ingredientConnectors La liste des connecteurs d'ingrédients.
     * @param currentSection L'état de section courant (NONE, INGREDIENTS, INSTRUCTIONS).
     * @param seenIngredientKeywords L'ensemble des mots-clés d'ingrédients déjà recensés.
     * @return L'objet [BlockAResult] contenant le résultat de l'analyse.
     */
    internal fun processBlockA_MetadataAndHangingLine(
        rawLine: String,
        results: RawSections,
        vocabulary: OcrVocabulary,
        containsActionRegex: Regex?,
        ingredientConnectors: List<String>,
        currentSection: Int,
        seenIngredientKeywords: MutableSet<String>
    ): BlockAResult {
        var workingLine = rawLine.trim()
        if (Ocr2Cleaner.isTechnicalDimension(workingLine, vocabulary)) {
            results.rawInstructionsList.add(workingLine)
            return BlockAResult(true, workingLine, "", false, false, false)
        }

        // 1. DÉTECTION ET EXTRACTION DES MÉTA-DONNÉES (RecipeMetadataParser)
        val metaResult = RecipeMetadataParser.extractMetadata(workingLine, results, vocabulary)
        workingLine = metaResult.remainingLine
        if (metaResult.isPureMetadata) {
            LogManager.d(TAG, String.format(vocabulary.logOcr3BlockAMetadataIgnored, rawLine))
            return BlockAResult(true, workingLine, "", false, false, false)
        }

        val lowerLine = workingLine.lowercase()
        val containsAction = if (containsActionRegex != null) {
            containsActionRegex.containsMatchIn(workingLine)
        } else {
            false
        }

        val isBulletDot = workingLine.startsWith("•")
        val isBulletDash = workingLine.startsWith("-")
        val isBulletStar = workingLine.startsWith("*")
        val startsWithBullet = isBulletDot || isBulletDash || isBulletStar

        val startsWithConnector = ingredientConnectors.any { lowerLine.startsWith("$it ") || (it.endsWith("'") && lowerLine.startsWith(it)) }

        // 8.5 RATTRAPAGE IMMÉDIAT DE CONTINUATION DE PHRASE SUSPENDUE (Ingrédients ou Instructions)
        val defaultHanging = listOf("de", "d'", "du", "des", "ou", "au", "aux", "avec", "dans", "pour", "sur", "en", "par", "la", "le", "les")
        val combinedHanging = (vocabulary.hangingConnectors + defaultHanging).distinct()
        val allConnectorsEnding = combinedHanging.map { " ${it.trim().lowercase()}" }

        val targetListForContinuation = if (currentSection == Ocr3Categorizer.SECTION_INGREDIENTS) {
            results.rawIngredientsList
        } else {
            results.rawInstructionsList
        }

        val lastRawElement = targetListForContinuation.lastOrNull()
        val lastRawLine = if (lastRawElement != null) {
            lastRawElement.lowercase().trim()
        } else {
            ""
        }

        val isListNotEmpty = targetListForContinuation.isNotEmpty()
        val endsWithConnector = allConnectorsEnding.any { lastRawLine.endsWith(it) }
        val isContinuationOfHangingLine = isListNotEmpty && endsWithConnector

        if (isContinuationOfHangingLine) {
            if (!containsAction) {
                if (!startsWithBullet) {
                    val lastIdx = targetListForContinuation.lastIndex
                    targetListForContinuation[lastIdx] = "${targetListForContinuation[lastIdx].trim()} $workingLine"
                    if (currentSection == Ocr3Categorizer.SECTION_INGREDIENTS) {
                        val keywords = Ocr3Categorizer.extractKeywordsFromText(workingLine, null)
                        seenIngredientKeywords.addAll(keywords)
                    }
                    LogManager.d(TAG, String.format(vocabulary.logOcr3BlockAFusionContinuation, workingLine, targetListForContinuation[lastIdx]))
                    return BlockAResult(true, workingLine, lowerLine, containsAction, startsWithBullet, startsWithConnector)
                }
            }
        }

        return BlockAResult(false, workingLine, lowerLine, containsAction, startsWithBullet, startsWithConnector)
    }
}
