package com.example.parabdcollector.ui

import com.example.parabdcollector.model.Location

/**
 * Data class representing a location in the expandable tree view.
 *
 * @property location The original Location object from the database.
 * @property depth The depth of the location in the hierarchy (0 for root).
 * @property isExpanded True if the location's children are currently visible.
 * @property hasChildren True if the location has children, determining if the expand icon should be shown.
 */
data class ExpandableLocation(
    val location: Location,
    val depth: Int,
    val isExpanded: Boolean,
    val hasChildren: Boolean
)
