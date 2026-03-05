package com.example.chiefinventory.utils

/**
 * Data class representing the structured result of an OCR recipe scan.
 */
data class RecipeOcrResult(
    val title: String? = null,
    val ingredients: String? = null,
    val instructions: String? = null,
    val wine: String? = null,
    val source: String? = null,
    val servings: String? = null
)
