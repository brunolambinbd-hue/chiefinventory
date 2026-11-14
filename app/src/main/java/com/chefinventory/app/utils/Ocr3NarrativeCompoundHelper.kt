package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 3 (Categorizer / BlockD) dédié à la détection isolée des fragments
 * de phrases narratives d'instructions du type "de X et une Y" ou "d'un X et d'une Y".
 */
object Ocr3NarrativeCompoundHelper {

    private val NARRATIVE_COMPOUND_PATTERN = Regex(
        "(?i)^d['’e]\\s*(?:un|une|1|l['’]|le|la|les)?\\s*\\p{L}+.*\\b(?:et|plus)\\s+(?:un|une|1|d['’]un|d['’]une|d['’]|de|du)\\s+\\p{L}+"
    )

    /**
     * Détermine si une ligne de texte correspond à un fragment de phrase narrative d'instruction composé.
     *
     * @param line La ligne de texte à analyser (ex: "de jus d'orange et une pincée de sel.").
     * @return `true` s'il s'agit d'un fragment narratif d'instruction, `false` sinon.
     */
    fun isNarrativeCompoundInstruction(line: String): Boolean {
        if (line.isBlank()) return false
        return NARRATIVE_COMPOUND_PATTERN.containsMatchIn(line.trim())
    }
}
