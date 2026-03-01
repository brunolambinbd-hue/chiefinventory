package com.example.chiefinventory.ui.model

import com.example.chiefinventory.model.Location
import com.example.chiefinventory.ui.adapter.LocationAdapter

/**
 * A view-specific data class that represents a location within an expandable list UI.
 *
 * It wraps the core [Location] entity with additional state information required by the [LocationAdapter]
 * to render the hierarchical tree structure correctly.
 *
 * @property location The original [Location] entity from the database.
 * @property depth The nesting level of the location in the hierarchy (e.g., 0 for root, 1 for a child), used for indentation.
 * @property isExpanded True if this location is currently expanded to show its children; false otherwise.
 * @property hasChildren True if this location has child locations, which determines whether to display an expand/collapse icon.
 * @property itemCount The number of collection items stored in this specific location.
 */
@Suppress("unused")
data class ExpandableLocation(
    val location: Location,
    val depth: Int,
    val isExpanded: Boolean,
    val hasChildren: Boolean,
    val itemCount: Int = 0
)
