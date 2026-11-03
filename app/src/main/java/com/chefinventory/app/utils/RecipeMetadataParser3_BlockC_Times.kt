package com.chefinventory.app.utils

import android.util.Log

/**
 * Sous-composant du Bloc C de l'Étape 2.5 : Extraction des durées (Préparation, Cuisson, Repos) pour [RecipeMetadataParser].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object RecipeMetadataParser3_BlockC_Times {
    private const val TAG = "RecipeMetadataParser3_BlockC"

    /**
     * Mis à jour le résultat de temps extrait dans l'objet [results].
     *
     * @param results L'objet [RawSections] mis à jour.
     * @param type Le type de temps ("prép", "cuisson", "repos").
     * @param mins Le nombre de minutes sous forme de texte.
     */
    internal fun updateTimeResult(results: RawSections, type: String, mins: String?) {
        val lowerType = type.lowercase()
        val isPrepAcc = lowerType.startsWith("prép")
        val isPrepNoAcc = lowerType.startsWith("prep")
        val isPrep = if (isPrepAcc) true else isPrepNoAcc

        val isCuisShort = lowerType.startsWith("cuis")
        val isCuisson = lowerType.startsWith("cuisson")
        val isCook = if (isCuisShort) true else isCuisson

        val isRest = lowerType.startsWith("re")

        if (isPrep) {
            results.detectedPrepTime = mins
        } else if (isCook) {
            results.detectedCookTime = mins
        } else if (isRest) {
            results.detectedRestingTime = mins
        } else {
            Log.d(TAG, "  [SKIP] Type de temps non reconnu : '$type'")
        }
    }

    /**
     * Convertit une chaîne de durée sous forme de texte (ex: "1 h 30", "45 min") en nombre de minutes sous forme de texte.
     *
     * @param timeStr La chaîne de durée brute.
     * @return Le nombre de minutes calculé sous forme de texte (ex: "90", "45"), ou null si non convertible.
     */
    internal fun extractMinutes(timeStr: String): String? {
        val hoursMatch = OcrRegexes.Metadata.HOURS_PATTERN.find(timeStr)
        if (hoursMatch != null) {
            val hParsed = hoursMatch.groupValues[1].toIntOrNull()
            val h = if (hParsed != null) hParsed else 0

            val mParsed = hoursMatch.groupValues[2].toIntOrNull()
            val m = if (mParsed != null) mParsed else 0

            return (h * 60 + m).toString()
        }

        val minutesMatch = OcrRegexes.Metadata.MINUTES_PATTERN.find(timeStr)
        if (minutesMatch != null) {
            return minutesMatch.groupValues[1]
        }

        val digitMatch = Regex("(\\d+)").find(timeStr)
        if (digitMatch != null) {
            return digitMatch.groupValues[1]
        }

        return null
    }

    /**
     * Traite l'extraction d'une durée autonome (ex: "15 min").
     */
    internal fun processStandaloneTime(
        workingLine: String,
        isInstructionSentence: Boolean,
        results: RawSections
    ): String {
        var resultLine = workingLine
        val standaloneTimeMatch = Regex("(?i)^(\\d+\\s*(?:mn|min|minute|h|heure|heures))\\b").find(resultLine)
        if (standaloneTimeMatch != null) {
            val isShort = resultLine.length < 12
            if (isShort) {
                val mins = extractMinutes(standaloneTimeMatch.groupValues[1])
                if (results.detectedPrepTime == null) {
                    results.detectedPrepTime = mins
                } else if (results.detectedCookTime == null) {
                    results.detectedCookTime = mins
                } else {
                    Log.d(TAG, "  [INFO] Prép et cuisson déjà renseignées")
                }

                if (!isInstructionSentence) {
                    resultLine = resultLine.replace(standaloneTimeMatch.value, "").trim()
                } else {
                    Log.d(TAG, "  [INFO] Consigne d'instruction - temps conservé")
                }
            } else {
                Log.d(TAG, "  [INFO] Ligne de temps autonome >= 12 chars")
            }
        }
        return resultLine
    }

    /**
     * Tente d'extraire les métadonnées de durées de la ligne [rawWorkingLine].
     *
     * @return La ligne nettoyée des durées extraites.
     */
    internal fun processTimesBlock(rawWorkingLine: String, line: String, vocabulary: OcrVocabulary?, results: RawSections): String {
        var workingLine = rawWorkingLine
        val actionKeywords = if (vocabulary != null) vocabulary.stepActionKeywords else emptyList()
        val hasVerb = OcrInstructionUtils.hasActionVerb(line, actionKeywords)
        val startsBullet = line.trim().startsWith("•")
        val startsNumber = Regex("^\\s*\\d{1,2}\\s*[.)\\-]").containsMatchIn(line)
        val isInstructionSentence = hasVerb || startsBullet || startsNumber

        while (true) {
            val matchComb = OcrRegexes.Metadata.TIME_COMBINED.find(workingLine)
            val matchPre = OcrRegexes.Metadata.TIME_PREFIX.find(workingLine)
            val matchSuf = OcrRegexes.Metadata.TIME_SUFFIX.find(workingLine)

            if (matchComb != null) {
                results.detectedPrepTime = extractMinutes(matchComb.groupValues[1])
                results.detectedCookTime = extractMinutes(matchComb.groupValues[2])
                if (!isInstructionSentence) {
                    workingLine = workingLine.replace(matchComb.value, "").trim()
                } else {
                    break
                }
            } else if (matchPre != null) {
                val mins = extractMinutes(matchPre.groupValues[2])
                updateTimeResult(results, matchPre.groupValues[1].lowercase(), mins)
                if (!isInstructionSentence) {
                    workingLine = workingLine.replace(matchPre.value, "").trim()
                } else {
                    break
                }
            } else if (matchSuf != null) {
                val mins = extractMinutes(matchSuf.groupValues[1])
                updateTimeResult(results, matchSuf.groupValues[2].lowercase(), mins)
                if (!isInstructionSentence) {
                    workingLine = workingLine.replace(matchSuf.value, "").trim()
                } else {
                    break
                }
            } else {
                workingLine = processStandaloneTime(workingLine, isInstructionSentence, results)
                break
            }
        }
        return workingLine
    }
}
