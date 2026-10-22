package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Domain model representing all vocabulary and keywords required by the OCR pipeline.
 * This class is a pure Kotlin data structure, independent of the Android Framework,
 * making the OCR pipeline fast and easy to test without mocking [Resources].
 */
data class OcrVocabulary(
    val stepActionKeywords: List<String> = emptyList(),
    val commonIngredientsNoQty: List<String> = emptyList(),
    val excludedOcrKeywords: List<String> = emptyList(),
    val ingredientPreparationModifiers: List<String> = emptyList(),
    val instructionSwitchKeywords: List<String> = emptyList(),
    val ingredientSemanticExclusions: List<String> = emptyList(),
    val ingredientEndMarkers: List<String> = emptyList(),
    val ingredientSubHeaders: List<String> = emptyList(),
    val sourceKeywords: List<String> = emptyList(),
    val phonePrefixes: List<String> = emptyList(),
    val wineAppellations: List<String> = emptyList(),
    val wineProducers: List<String> = emptyList(),
    val wineKeywords: List<String> = emptyList(),
    val wineTitleKeywords: List<String> = emptyList(),
    val wineRemovePattern: String = "",
    val ocrSpellingRepairs: List<String> = emptyList(),
    val adExclusions: List<String> = emptyList(),
    val titleKeywords: List<String> = emptyList(),
    val mandatoryComplementIngredients: List<String> = emptyList(),
    val hangingConnectors: List<String> = emptyList(),
    val ingredientVarietyQualifiers: List<String> = emptyList(),
    val baseIngredientsExpectingVariety: List<String> = emptyList(),
    val standaloneArticles: List<String> = emptyList(),
    val culinaryMeasurementUnits: List<String> = emptyList(),
    val titleSpellingRepairs: List<String> = emptyList(),
    val ocrDiacriticRepairs: List<String> = emptyList()
) {
    companion object {
        /**
         * Factory method to load an [OcrVocabulary] instance from Android [Resources].
         * This bridges the Android Framework layer with the domain layer.
         */
        fun fromResources(res: Resources): OcrVocabulary {
            return OcrVocabulary(
                stepActionKeywords = getStringArraySafely(res, R.array.step_action_keywords),
                commonIngredientsNoQty = getStringArraySafely(res, R.array.common_ingredients_no_qty),
                excludedOcrKeywords = getStringArraySafely(res, R.array.excluded_ocr_keywords),
                ingredientPreparationModifiers = getStringArraySafely(res, R.array.ingredient_preparation_modifiers),
                instructionSwitchKeywords = getStringArraySafely(res, R.array.instruction_switch_keywords),
                ingredientSemanticExclusions = getStringArraySafely(res, R.array.ingredient_semantic_exclusions),
                ingredientEndMarkers = getStringArraySafely(res, R.array.ingredient_end_markers),
                ingredientSubHeaders = getStringArraySafely(res, R.array.ingredient_sub_headers),
                sourceKeywords = getStringArraySafely(res, R.array.source_keywords),
                phonePrefixes = getStringArraySafely(res, R.array.phone_prefixes),
                wineAppellations = getStringArraySafely(res, R.array.wine_appellations),
                wineProducers = getStringArraySafely(res, R.array.wine_producers),
                wineKeywords = getStringArraySafely(res, R.array.wine_keywords),
                wineTitleKeywords = getStringArraySafely(res, R.array.wine_title_keywords),
                wineRemovePattern = getStringSafely(res, R.string.wine_remove_pattern),
                ocrSpellingRepairs = getStringArraySafely(res, R.array.ocr_spelling_repairs),
                adExclusions = getStringArraySafely(res, R.array.advertisement_exclusions),
                titleKeywords = getStringArraySafely(res, R.array.title_keywords),
                mandatoryComplementIngredients = getStringArraySafely(res, R.array.ingredient_mandatory_complements),
                hangingConnectors = getStringArraySafely(res, R.array.hanging_connectors),
                ingredientVarietyQualifiers = getStringArraySafely(res, R.array.ingredient_variety_qualifiers),
                baseIngredientsExpectingVariety = getStringArraySafely(res, R.array.base_ingredients_expecting_variety),
                standaloneArticles = getStringArraySafely(res, R.array.standalone_articles),
                culinaryMeasurementUnits = getStringArraySafely(res, R.array.culinary_measurement_units),
                titleSpellingRepairs = getStringArraySafely(res, R.array.title_spelling_repairs),
                ocrDiacriticRepairs = getStringArraySafely(res, R.array.ocr_diacritic_repairs)
            )
        }

        private fun getStringArraySafely(res: Resources, id: Int): List<String> {
            return try {
                res.getStringArray(id).toList()
            } catch (_: Exception) {
                emptyList()
            }
        }

        private fun getStringSafely(res: Resources, id: Int): String {
            return try {
                res.getString(id)
            } catch (_: Exception) {
                ""
            }
        }
    }
}
