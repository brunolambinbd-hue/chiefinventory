package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Fonctions utilitaires dédiées à l'analyse et au traitement des consignes d'instructions du pipeline OCR.
 *
 * Responsabilités :
 * - Détecter la présence d'un déclencheur de consignes d'instructions (`hasInstructionTrigger`).
 * - Déterminer si une ligne contient un verbe d'action culinaire à l'impératif (`hasActionVerb`).
 */
object OcrInstructionUtils {

    /**
     * Détermine si une ligne contient un mot-clé de déclenchement d'instructions (`instruction_triggers.xml`).
     *
     * @param line La ligne de texte à évaluer.
     * @param instructionTriggers Liste des déclencheurs d'instructions.
     * @return `true` si la ligne contient un déclencheur d'instruction, `false` sinon.
     */
    fun hasInstructionTrigger(line: String, instructionTriggers: List<String>): Boolean {
        if (line.isBlank() || instructionTriggers.isEmpty()) return false
        return instructionTriggers.any { Regex("(?i)\\b${Pattern.quote(it)}\\b").containsMatchIn(line) }
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
        val distinctKeywords = stepActionKeywords
            .map { it.replace(OcrRegexes.Beautifier.BRACKET_COMMENT, "") }
            .filter { it.length >= 3 && it.lowercase() !in listOf("minutes", "heures", "secondes", "environ") }
        if (distinctKeywords.isEmpty()) return false
        val pattern = Regex("(?i)\\b(?:${distinctKeywords.joinToString("|") { Regex.escape(it) }})\\b")
        return pattern.containsMatchIn(line)
    }
}
