package com.example.chiefinventory.dao

/**
 * Data class to hold the raw counts from the database for category statistics.
 * This is used as a temporary object before being mapped to the final CategoryInfo model.
 */
data class CategoryInfoFromDb(
    val name: String,
    val possessedCount: Int,
    val totalCount: Int
)
