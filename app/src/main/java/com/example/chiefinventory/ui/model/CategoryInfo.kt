package com.example.chiefinventory.ui.model

import androidx.annotation.ColorRes

/**
 * Represents the statistical information for a single category, including item counts.
 *
 * @property name The name of the category.
 * @property possessedCount The number of items in this category that the user possesses.
 * @property totalCount The total number of unique items defined for this category.
 * @property statusColorRes The pre-calculated color resource ID for the progress bar.
 */
data class CategoryInfo(
    val name: String,
    val possessedCount: Int,
    val totalCount: Int,
    @get:ColorRes val statusColorRes: Int
)
