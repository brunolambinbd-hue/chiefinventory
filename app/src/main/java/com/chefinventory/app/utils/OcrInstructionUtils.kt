package com.chefinventory.app.utils

import java.util.regex.Pattern

/**
 * Fonctions utilitaires dédiées à l'analyse et au traitement des consignes d'instructions du pipeline OCR.
 *
 * Responsabilités :
 * - Déléguer la détection de déclencheurs d'instructions à [Ocr3CategorizerInstructionTriggerDetector].
 * - Déterminer si une ligne contient un verbe d'action culinaire à l'impératif (`hasActionVerb`).
 */
object OcrInstructionUtils {

    /**
     * Détermine si une ligne contient un mot-clé de déclenchement d'instructions (`instruction_triggers.xml`).
     */
    fun hasInstructionTrigger(line: String, instructionTriggers: List<String>): Boolean {
        return Ocr3CategorizerInstructionTriggerDetector.hasInstructionTrigger(line, instructionTriggers)
    }

    /**
     * Détermine si la ligne contient au moins un verbe d'action culinaire (`step_keywords.xml`).
     *
     * @param line La ligne de texte à évaluer.
     * @param stepActionKeywords Liste des verbes d'action culinaires à l'impératif.
     * @return `true` si la ligne contient un verbe d'action, `false` sinon.
     */
    fun hasActionVerb(line: String, stepActionKeywords: List<String>): Boolean {
        if (line.isBlank() || stepActionKeywords.isEmpty()) return false

        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val excludedNonVerbs = (vocab.durationUnits + vocab.durationApproximationAdverbs + vocab.timeHeaderKeywords)
            .map { it.lowercase().trim() }
            .toSet()

        val distinctKeywords = stepActionKeywords
            .map { it.replace(OcrRegexes.Beautifier.BRACKET_COMMENT, "").trim().lowercase() }
            .filter { it.length >= 3 && it !in excludedNonVerbs }
            .distinct()
            .sortedByDescending { it.length }

        if (distinctKeywords.isEmpty()) return false

        val regexParts = distinctKeywords.map { kw ->
            if (kw.contains("[") && kw.contains("]")) kw else Pattern.quote(kw)
        }
        val pattern = Pattern.compile("(?<!\\p{L})(?:" + regexParts.joinToString("|") + ")(?!\\p{L})", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
        return pattern.matcher(line).find()
    }
}
