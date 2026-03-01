package com.example.chiefinventory.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the [CategoryMapper] object.
 */
class CategoryMapperTest {

    @Test
    fun `getSuperCategories should return a distinct and sorted list`() {
        // WHEN: The getSuperCategories function is called.
        val superCategories = CategoryMapper.getSuperCategories()

        // THEN: The list should be sorted and contain no duplicates.
        val expected = listOf("Album", "Carte", "Divers", "Illustration", "Image", "Objets", "Presse", "Promos - Publicités")
        assertEquals("The list of super-categories should be distinct and sorted alphabetically", expected, superCategories)
    }

    @Test
    fun `getCategoriesFor should return correct sorted sub-categories for Image`() {
        // WHEN: We request the categories for the "Image" super-category.
        val imageCategories = CategoryMapper.getCategoriesFor("Image")

        // THEN: The list should contain all and only the categories mapped to "Image", sorted alphabetically.
        // "Portfolios" has been renamed from "PORTFOLIOS" to match database consistency.
        val expected = listOf("Affiches", "Ex-libris", "Offsets", "Portfolios", "Sérigraphies")
        assertEquals("The list of categories for 'Image' should match the expected sorted list", expected, imageCategories)
    }

    @Test
    fun `getCategoriesFor should return correct sorted sub-categories for Presse`() {
        // WHEN: We request the categories for the new "Presse" super-category.
        val categories = CategoryMapper.getCategoriesFor("Presse")

        // THEN: It should return "Travaux pour Spirou".
        val expected = listOf("Travaux pour Spirou")
        assertEquals("The list of categories for 'Presse' should match", expected, categories)
    }

    @Test
    fun `getCategoriesFor should return an empty list for a non-existent super-category`() {
        // WHEN: We request categories for a super-category that doesn't exist.
        val result = CategoryMapper.getCategoriesFor("NON_EXISTENT_SUPER_CATEGORY")

        // THEN: The result should be an empty list.
        assertEquals("Requesting a non-existent super-category should return an empty list", emptyList<String>(), result)
    }
}
