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
    val servings: String? = null,
    val prepTime: String? = null,
    val cookTime: String? = null,
    val restingTime: String? = null,
    val kcalPerServing: String? = null, // Nouveau champ pour l'OCR
    val difficulty: String? = null      // Nouveau champ pour l'OCR
)
