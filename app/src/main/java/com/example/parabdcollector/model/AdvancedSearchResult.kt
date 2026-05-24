package com.example.parabdcollector.model

import com.example.parabdcollector.ui.model.SearchResultItem

/**
 * A data class to hold the results of an advanced search.
 *
 * @property results The list of items matching the criteria (potentially limited).
 * @property totalCount The total number of items that matched the criteria before limitation.
 */
data class AdvancedSearchResult(
    val results: List<SearchResultItem>,
    val totalCount: Int,
    /** True if the results are "best guesses" because no item met the high-confidence threshold. */
    val isFallback: Boolean = false
)
