package com.chefinventory.app.utils

import java.util.regex.Pattern

/**
 * Assistant de l'Étape 3 (Catégorisation) dédié à la détection des mots-clés de déclenchement d'instructions.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerInstructionTriggerDetector {

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
}
