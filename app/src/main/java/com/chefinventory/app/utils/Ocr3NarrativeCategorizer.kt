package com.chefinventory.app.utils

/**
 * Façade de compatibilité pour le Categorizer 1 Colonne Instructions Seules ([Ocr3CategorizerOneColumnOnlyInstructions]).
 */
object Ocr3NarrativeCategorizer {
    fun isNarrativeInstructionOnlyRecipe(lines: List<String>, vocabulary: OcrVocabulary): Boolean {
        return Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(lines, vocabulary)
    }

    fun categorizeNarrativeRecipe(lines: List<String>, vocabulary: OcrVocabulary): RawSections {
        return Ocr3CategorizerOneColumnOnlyInstructions.categorizeNarrativeRecipe(lines, vocabulary)
    }
}
