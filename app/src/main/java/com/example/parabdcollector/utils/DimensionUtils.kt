package com.example.parabdcollector.utils

import kotlin.math.abs

/**
 * Utility to parse and compare physical dimensions.
 */
object DimensionUtils {

    /**
     * Parses a dimension string like "21x29.7" or "15 x 21 cm" into a pair of width and height.
     * Returns null if parsing fails.
     */
    fun parseDimensions(dimStr: String?): Pair<Double, Double>? {
        if (dimStr.isNullOrBlank()) return null
        
        try {
            // Remove units and spaces, replace comma with dot
            val cleaned = dimStr.lowercase()
                .replace("cm", "")
                .replace("mm", "")
                .replace(",", ".")
                .trim()
            
            // Ajout du séparateur "/" très courant dans la base
            val parts = cleaned.split("x", "*", "/", " ", "-")
                .filter { it.isNotBlank() }
                .mapNotNull { it.toDoubleOrNull() }
            
            if (parts.size >= 2) {
                // Return sorted pair so orientation doesn't matter (min, max)
                return Pair(parts[0], parts[1])
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        return null
    }

    /**
     * Checks if the detected dimensions match the target dimensions within a tolerance.
     * @param tolerance Default 2.0 cm (quite generous for manual AR measurement).
     */
    fun isWithinTolerance(
        detected: Pair<Double, Double>, 
        target: Pair<Double, Double>, 
        tolerance: Double = 3.0 
    ): Boolean {
        // Sort both to be orientation agnostic
        val dSorted = listOf(detected.first, detected.second).sorted()
        val tSorted = listOf(target.first, target.second).sorted()
        
        return abs(dSorted[0] - tSorted[0]) <= tolerance && 
               abs(dSorted[1] - tSorted[1]) <= tolerance
    }

    /**
     * Compares two aspect ratios with a tolerance.
     */
    fun isRatioMatch(ratio1: Double, ratio2: Double, tolerance: Double = 0.15): Boolean {
        // Normalize ratios to be >= 1.0 (landscape/portrait agnostic)
        val r1 = if (ratio1 < 1.0) 1.0 / ratio1 else ratio1
        val r2 = if (ratio2 < 1.0) 1.0 / ratio2 else ratio2
        return abs(r1 - r2) <= tolerance
    }
}
