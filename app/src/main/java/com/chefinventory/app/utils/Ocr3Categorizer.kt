package com.chefinventory.app.utils

import android.content.res.Resources

/**
 * Façade de compatibilité pour le Categorizer 1 Colonne Ingrédients en Premier ([Ocr3CategorizerOneColumnIngredientFirst]).
 */
object Ocr3Categorizer {
    internal const val SECTION_NONE = Ocr3CategorizerOneColumnIngredientFirst.SECTION_NONE
    internal const val SECTION_INGREDIENTS = Ocr3CategorizerOneColumnIngredientFirst.SECTION_INGREDIENTS
    internal const val SECTION_INSTRUCTIONS = Ocr3CategorizerOneColumnIngredientFirst.SECTION_INSTRUCTIONS

    fun categorize(lines: List<String>, res: Resources): RawSections {
        return Ocr3CategorizerOneColumnIngredientFirst.categorize(lines, res)
    }

    fun categorize(lines: List<String>, vocabulary: OcrVocabulary): RawSections {
        return Ocr3CategorizerOneColumnIngredientFirst.categorize(lines, vocabulary)
    }

    internal fun extractKeywordsFromText(name: String, supplementalInfo: String?): List<String> {
        return Ocr3CategorizerOneColumnIngredientFirst.extractKeywordsFromText(name, supplementalInfo)
    }
}
