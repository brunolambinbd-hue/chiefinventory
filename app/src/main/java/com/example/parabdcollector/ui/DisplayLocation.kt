package com.example.parabdcollector.ui

import com.example.parabdcollector.model.Location

/**
 * A view-specific data class that wraps a [Location] object with its hierarchy depth.
 *
 * This class is used to facilitate the display of locations in a flat list (like a dropdown menu)
 * while preserving the visual indentation that represents the parent-child relationships.
 *
 * @property location The original [Location] entity from the database.
 * @property depth The nesting level of the location in the hierarchy (e.g., 0 for root, 1 for a child of a root, etc.).
 */
data class DisplayLocation(val location: Location, val depth: Int)
