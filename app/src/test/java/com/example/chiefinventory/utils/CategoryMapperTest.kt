package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Unit tests for the [CategoryMapper] object.
 */
class CategoryMapperTest {

    @Test
    fun `getSuperCategories should return a distinct and sorted list`() {
        val superCategories = CategoryMapper.getSuperCategories()
        val expected = listOf("Album", "Carte", "Divers", "Illustration", "Image", "Objets", "Presse", "Promos - Publicités")
        
        assertWithMessage("The list of super-categories should be distinct and sorted alphabetically")
            .that(superCategories).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun `getCategoriesFor should return correct sorted sub-categories for Image`() {
        val imageCategories = CategoryMapper.getCategoriesFor("Image")
        val expected = listOf("Affiches", "Ex-libris", "Offsets", "Portfolios", "Sérigraphies")
        
        assertWithMessage("The list of categories for 'Image' should match the expected sorted list")
            .that(imageCategories).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun `getCategoriesFor should return correct sorted sub-categories for Presse`() {
        val categories = CategoryMapper.getCategoriesFor("Presse")
        val expected = listOf("Travaux pour Spirou")
        
        assertWithMessage("The list of categories for 'Presse' should match")
            .that(categories).containsExactlyElementsIn(expected)
    }

    @Test
    fun `getCategoriesFor should return an empty list for a non-existent super-category`() {
        val result = CategoryMapper.getCategoriesFor("NON_EXISTENT_SUPER_CATEGORY")
        
        assertWithMessage("Requesting a non-existent super-category should return an empty list")
            .that(result).isEmpty()
    }
}
