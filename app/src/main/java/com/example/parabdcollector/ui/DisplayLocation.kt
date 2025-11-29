package com.example.parabdcollector.ui

import com.example.parabdcollector.model.Location

/**
 * Data class for displaying a location in a simple list with its hierarchy depth.
 * Used in dropdowns.
 */
data class DisplayLocation(val location: Location, val depth: Int)
