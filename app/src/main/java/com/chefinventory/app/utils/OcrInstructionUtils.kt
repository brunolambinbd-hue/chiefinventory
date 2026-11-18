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
     * Nettoie une consigne d'instruction en supprimant tout artefact de puce orpheline
     * (ex: une seule lettre 'O', 'o', 'A', 'i' ou symbole de puce suivi d'un espace en début de ligne d'instruction).
     *
     * @param line La ligne de texte d'instruction.
     * @return La consigne d'instruction assainie de sa puce orpheline initiale.
     */
    fun stripSingleLeadingAlphaOrBulletPrefix(line: String): String {
        if (line.isBlank()) return ""
        return line.replace(Regex("(?iu)^\\s*(?:[•.*\\-]|\\([a-zA-Z]\\)|[a-zA-Z])\\s+(?=[\\p{Lu}\\p{Ll}])"), "").trim()
    }

    /**
     * Détermine de manière centralisée si une ligne commence par un verbe d'action impératif principal,
     * en gérant toutes les formes de puces et artefacts caméra ('O', '0', '•', '*', '-').
     *
     * @param line La ligne de texte à évaluer.
     * @param startVerbs Liste des verbes d'action impératifs de démarrage (ex: `step_start_verbs`).
     * @return `true` si la ligne commence par un verbe de démarrage, `false` sinon.
     */
    fun hasStartVerb(line: String, startVerbs: List<String>): Boolean {
        if (line.isBlank() || startVerbs.isEmpty()) return false
        val cleanLine = stripSingleLeadingAlphaOrBulletPrefix(line)
        return startVerbs.any { verb ->
            val cleanV = verb.trim().lowercase()
            cleanV.isNotEmpty() && Regex("(?iu)^\\s*(?<!\\p{L})${Regex.escape(cleanV)}(?!\\p{L})").containsMatchIn(cleanLine)
        }
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
