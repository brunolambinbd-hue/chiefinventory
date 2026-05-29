package com.example.parabdcollector.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [DimensionUtils].
 */
class DimensionUtilsTest {

    @Test
    fun `parseDimensions should handle various formats`() {
        // Standard format
        assertEquals(Pair(21.0, 29.7), DimensionUtils.parseDimensions("21 x 29.7"))
        
        // With units and comma
        assertEquals(Pair(15.0, 21.0), DimensionUtils.parseDimensions("15,0 x 21cm"))
        
        // With slash separator
        assertEquals(Pair(20.0, 30.0), DimensionUtils.parseDimensions("20/30"))
        
        // With multiple spaces and trailing text
        assertEquals(Pair(24.0, 32.0), DimensionUtils.parseDimensions("  24  x  32  cm (luxe)"))
        
        // Invalid strings
        assertNull(DimensionUtils.parseDimensions(""))
        assertNull(DimensionUtils.parseDimensions("unknown"))
        assertNull(DimensionUtils.parseDimensions("21")) // Only one dimension
    }

    @Test
    fun `isWithinTolerance should be orientation agnostic`() {
        val target = Pair(20.0, 30.0)
        
        // Exact match
        assertTrue(DimensionUtils.isWithinTolerance(Pair(20.0, 30.0), target))
        
        // Flipped orientation
        assertTrue(DimensionUtils.isWithinTolerance(Pair(30.0, 20.0), target))
        
        // Within default tolerance (3.0)
        assertTrue(DimensionUtils.isWithinTolerance(Pair(22.0, 28.0), target))
        
        // Outside tolerance
        assertFalse(DimensionUtils.isWithinTolerance(Pair(24.0, 30.0), target))
        assertFalse(DimensionUtils.isWithinTolerance(Pair(20.0, 34.0), target))
    }

    @Test
    fun `isRatioMatch should compare aspect ratios regardless of orientation`() {
        // 2:3 ratio is 0.666 or 1.5
        val ratio1 = 1.5 // (30/20)
        
        // Same ratio
        assertTrue(DimensionUtils.isRatioMatch(ratio1, 1.5))
        
        // Flipped ratio (20/30 = 0.666) should match because of normalization
        assertTrue(DimensionUtils.isRatioMatch(ratio1, 0.6666666666666666))
        
        // Within tolerance
        assertTrue(DimensionUtils.isRatioMatch(ratio1, 1.6))
        
        // Outside tolerance
        assertFalse(DimensionUtils.isRatioMatch(ratio1, 1.3))
    }
}
