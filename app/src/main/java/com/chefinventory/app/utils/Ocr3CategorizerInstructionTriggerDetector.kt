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
        if (line.isBlank()) return false
        if (instructionTriggers.isEmpty()) return false

        val sortedTriggers = instructionTriggers
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedByDescending { it.length }

        if (sortedTriggers.isEmpty()) return false

        return sortedTriggers.any { Regex("(?iu)(?<!\\p{L})${Regex.escape(it)}(?!\\p{L})").containsMatchIn(line) }
    }
}
