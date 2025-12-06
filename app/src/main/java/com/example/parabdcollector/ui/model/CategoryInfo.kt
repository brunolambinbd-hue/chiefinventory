package com.example.parabdcollector.ui.model

/**
 * A data class to hold the aggregated count of items within a category.
 *
 * @property name The name of the category (e.g., "Image", "Album").
 * @property possessedCount The number of items the user owns in this category.
 * @property totalCount The total number of items (owned and sought) in this category.
 */
data class CategoryInfo(val name: String, val possessedCount: Int, val totalCount: Int)
