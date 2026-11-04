package com.chefinventory.app.utils

import java.util.regex.Pattern

/**
 * Résultat de l'analyse du Bloc B (Ingrédient inachevé, Vin, Source & Purge Mots Exclus).
 *
 * @property shouldContinue `true` si la ligne a été absorbée/ignorée et nécessite un `continue`.
 * @property workingLine La ligne purgée des mots exclus.
 */
internal data class BlockBResult(
    val shouldContinue: Boolean,
    val workingLine: String
)

/**
 * Sous-composant du Bloc B de l'Étape 3 : Gère le rattrapage d'ingrédients inachevés, la détection des vins/sources et la purge des mots exclus OCR.
 */
object Ocr3Categorizer2_BlockB_WineSource {

    /**
     * Bloc B : Gère le rattrapage d'ingrédients inachevés, la détection des vins/sources et la purge des mots exclus OCR.
     *
     * @param rawWorkingLine La ligne de travail courante.
     * @param currentSection L'état de section courant.
     * @param containsAction Indique la présence d'un verbe d'action.
     * @param startsWithBullet Indique la présence d'une puce.
     * @param startsWithConnector Indique si la ligne commence par un connecteur.
     * @param isIngredientListClosed Indique si la section ingrédients est verrouillée.
     * @param vocabulary Le vocabulaire métier.
     * @param results Le conteneur [RawSections] d'accumulation.
     * @param seenIngredientKeywords L'ensemble des mots-clés d'ingrédients recensés.
     * @param wineState L'état de la machine à états de détection du vin.
     * @return L'objet [BlockBResult].
     */
    internal fun processBlockB_UnfinishedWineAndExcludedKeywords(
        rawWorkingLine: String,
        currentSection: Int,
        containsAction: Boolean,
        startsWithBullet: Boolean,
        startsWithConnector: Boolean,
        isIngredientListClosed: Boolean,
        vocabulary: OcrVocabulary,
        results: RawSections,
        seenIngredientKeywords: MutableSet<String>,
        wineState: Ocr3CategorizerWine.WineState
    ): BlockBResult {
        var workingLine = rawWorkingLine

        // 8.6 RATTRAPAGE IMMÉDIAT D'UNITÉ D'INGRÉDIENT INCOMPLÈTE (ex: "1 cuillère à soupe" + "de moutarde")
        if (Ocr3CategorizerUnfinishedIngredient.tryMergeUnfinishedQuantityLine(
                workingLine, currentSection, containsAction, startsWithBullet, vocabulary,
                results.rawIngredientsList, seenIngredientKeywords
            )) {
            return BlockBResult(true, workingLine)
        }

        // DÉTECTION VIN
        if (Ocr3CategorizerWine.processWineLine(
                workingLine, currentSection, isIngredientListClosed,
                containsAction, startsWithConnector, vocabulary, wineState
            )) {
            return BlockBResult(true, workingLine)
        }

        if (!containsAction && !startsWithConnector) {
            if (Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource(workingLine, vocabulary) || Ocr3CategorizerSource.isSourceLine(workingLine, vocabulary)) {
                results.detectedSourceList.add(Ocr3CategorizerSource.cleanSourceLine(workingLine, vocabulary))
                return BlockBResult(true, workingLine)
            }
        }

        for (word in vocabulary.excludedOcrKeywords) {
            val pattern = Regex("(?i)(?<!\\p{L})${Pattern.quote(word)}(?!\\p{L})")
            workingLine = pattern.replace(workingLine, "").trim()
        }
        workingLine = workingLine.replace(Regex("\\s+"), " ").trim()
        if (workingLine.isEmpty()) {
            return BlockBResult(true, workingLine)
        }

        return BlockBResult(false, workingLine)
    }
}
