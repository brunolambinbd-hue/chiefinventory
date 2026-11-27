package com.chefinventory.app.utils

import java.util.regex.Pattern

/**
 * Assistant de l'Étape 3 (Catégorisation) dédié au nettoyage et à la purge des mots d'exclusion OCR.
 */
object Ocr3CategorizerLineCleaner {

    /**
     * Purge les mots d'exclusion OCR (ex: INGRÉDIENTS, PAGE, SAVEUR) d'une ligne de travail.
     *
     * @param rawLine La ligne de travail courante.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La ligne nettoyée et sans mots d'exclusion.
     */
    fun purgeExcludedKeywords(rawLine: String, containsAction: Boolean, vocabulary: OcrVocabulary): String {
        var workingLine = rawLine
        val sectionHeadersToProtect = setOf("ingrédients", "ingredients", "instructions", "préparation", "preparation", "consignes")

        for (word in vocabulary.excludedOcrKeywords) {
            val cleanWord = word.trim().lowercase()
            if (cleanWord.isEmpty()) {
                continue
            }

            val isProtectedHeaderInSentence = containsAction && cleanWord in sectionHeadersToProtect
            if (!isProtectedHeaderInSentence) {
                val pattern = Regex("(?i)(?<!\\p{L})${Pattern.quote(word)}(?!\\p{L})")
                workingLine = pattern.replace(workingLine, "").trim()
            }
        }
        return workingLine.replace(Regex("\\s+"), " ").trim()
    }
}
