package com.example.parabdcollector.dao

/**
 * Represent a flat line of the hierarchy for the global plan.
 */
data class FullHierarchyItem(
    val superCategorie: String,
    val categorie: String,
    val possessedCount: Int,
    val totalCount: Int
)
