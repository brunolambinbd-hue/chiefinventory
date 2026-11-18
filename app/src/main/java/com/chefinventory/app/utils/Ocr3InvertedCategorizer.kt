package com.chefinventory.app.utils

/**
 * Façade de compatibilité pour le Categorizer 1 Colonne Instructions en Premier ([Ocr3CategorizerOneColumnInstructionFirst]).
 */
object Ocr3InvertedCategorizer {
    fun isInvertedRecipe(lines: List<String>, vocabulary: OcrVocabulary): Boolean {
        return Ocr3CategorizerOneColumnInstructionFirst.isInvertedRecipe(lines, vocabulary)
    }

    fun categorizeInvertedRecipe(
        lines: List<String>,
        vocabulary: OcrVocabulary,
        originalCleanedLines: List<String> = emptyList()
    ): RawSections {
        return Ocr3CategorizerOneColumnInstructionFirst.categorizeInvertedRecipe(lines, vocabulary, originalCleanedLines)
    }
}
