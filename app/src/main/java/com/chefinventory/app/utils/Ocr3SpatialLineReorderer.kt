package com.chefinventory.app.utils

/**
 * Façade de compatibilité pour le Categorizer 2 Colonnes Ingrédients en Premier ([Ocr3CategorizerTwoColumnsIngredientFirst]).
 */
object Ocr3SpatialLineReorderer {
    fun reorder2DLines(
        lines2D: List<OcrTextLine>,
        vocabulary: OcrVocabulary,
        results: RawSections
    ): List<String> {
        return Ocr3CategorizerTwoColumnsIngredientFirst.reorder2DLines(lines2D, vocabulary, results)
    }
}
