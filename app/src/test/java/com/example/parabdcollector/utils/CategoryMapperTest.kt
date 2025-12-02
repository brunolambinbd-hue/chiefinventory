@file:Suppress("NonAsciiCharacters")

package com.example.parabdcollector.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the [CategoryMapper] object.
 *
 * This class verifies that the mapping logic between categories and super-categories
 * is correct and that the utility functions return the expected results.
 */
class CategoryMapperTest {

    /**
     * Verifies that [CategoryMapper.getSuperCategories] returns a distinct and sorted list
     * of all super-category values.
     */
    @Test
    fun `getSuperCategories should return a distinct and sorted list`() {
        // GIVEN: The predefined map in CategoryMapper.

        // WHEN: We get the list of super-categories.
        val superCategories = CategoryMapper.getSuperCategories()

        // THEN: The list should be sorted alphabetically and contain no duplicates.
        val expected = listOf(
            "Album",
            "Carte",
            "Divers",
            "Illustration",
            "Image",
            "Objets",
            "Promos - Publicités"
        ).sorted()

        assertEquals(expected, superCategories)
    }

    /**
     * Verifies that [CategoryMapper.getCategoriesFor] returns the correct sub-categories
     * for a given valid super-category.
     */
    @Test
    fun `getCategoriesFor should return correct categories for a given super-category`() {
        // GIVEN: A known super-category.
        val superCategory = "Image"

        // WHEN: We get the categories for that super-category.
        val categories = CategoryMapper.getCategoriesFor(superCategory)

        // THEN: The list should contain all and only the categories mapped to "Image", sorted.
        val expected = listOf("Affiches", "Ex-libris", "Offsets", "PORTFOLIOS", "Sérigraphies").sorted()
        assertEquals(expected, categories)
    }

    /**
     * Verifies that [CategoryMapper.getCategoriesFor] returns an empty list when an
     * unknown super-category is provided.
     */
    @Test
    fun `getCategoriesFor should return an empty list for an unknown super-category`() {
        // GIVEN: A super-category that does not exist in the map.
        val unknownSuperCategory = "Inconnu"

        // WHEN: We get the categories for it.
        val categories = CategoryMapper.getCategoriesFor(unknownSuperCategory)

        // THEN: The result should be an empty list.
        assertEquals(true, categories.isEmpty())
    }
}
