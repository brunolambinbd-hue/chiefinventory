package com.example.chiefinventory.dao

import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitaires dédiés (1-à-1) pour [RecipeDao], [RecipeCategoryInfo], [RecipeIngredientWithLocation], et [RecipeWithIngredients].
 */
class RecipeDaoTest {

    @Test
    fun `RecipeCategoryInfo getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        val cat1 = RecipeCategoryInfo(categoryId = 10L, categoryName = "Desserts", recipeCount = 5)
        val cat1Same = RecipeCategoryInfo(categoryId = 10L, categoryName = "Desserts", recipeCount = 5)

        // Getters
        assertThat(cat1.categoryId).isEqualTo(10L)
        assertThat(cat1.categoryName).isEqualTo("Desserts")
        assertThat(cat1.recipeCount).isEqualTo(5)

        // Destructuring / componentN
        val (id, name, count) = cat1
        assertThat(id).isEqualTo(10L)
        assertThat(name).isEqualTo("Desserts")
        assertThat(count).isEqualTo(5)

        // Equals
        assertThat(cat1.equals(cat1)).isTrue()
        assertThat(cat1.equals(null)).isFalse()
        assertThat(cat1.equals("NotARecipeCategoryInfo")).isFalse()
        assertThat(cat1).isEqualTo(cat1Same)

        // Copy discrepancies
        assertThat(cat1).isNotEqualTo(cat1.copy(categoryId = 99L))
        assertThat(cat1).isNotEqualTo(cat1.copy(categoryName = "Entrées"))
        assertThat(cat1).isNotEqualTo(cat1.copy(recipeCount = 99))

        // HashCode & ToString
        assertThat(cat1.hashCode()).isEqualTo(cat1Same.hashCode())
        assertThat(cat1.hashCode()).isNotEqualTo(cat1.copy(categoryId = 99L).hashCode())
        assertThat(cat1.toString()).contains("Desserts")
    }

    @Test
    fun `RecipeIngredientWithLocation getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        val ingr = RecipeIngredient(id = 1L, recipeId = 10L, ingredientName = "Lait", locationId = 100L)
        val loc = Location(id = 100L, name = "Frigo", parentId = null)

        val rw1 = RecipeIngredientWithLocation(ingredient = ingr, location = loc)
        val rw1Same = RecipeIngredientWithLocation(ingredient = ingr, location = loc)
        val rwNullLoc = RecipeIngredientWithLocation(ingredient = ingr, location = null)

        // Getters
        assertThat(rw1.ingredient).isEqualTo(ingr)
        assertThat(rw1.location).isEqualTo(loc)

        // Destructuring / componentN
        val (i, l) = rw1
        assertThat(i).isEqualTo(ingr)
        assertThat(l).isEqualTo(loc)

        // Equals
        assertThat(rw1.equals(rw1)).isTrue()
        assertThat(rw1.equals(null)).isFalse()
        assertThat(rw1.equals("NotARecipeIngredientWithLocation")).isFalse()
        assertThat(rw1).isEqualTo(rw1Same)

        // Copy discrepancies
        val ingr2 = RecipeIngredient(id = 2L, recipeId = 10L, ingredientName = "Beurre")
        assertThat(rw1).isNotEqualTo(rw1.copy(ingredient = ingr2))
        assertThat(rw1).isNotEqualTo(rw1.copy(location = null))

        // HashCode & ToString
        assertThat(rw1.hashCode()).isEqualTo(rw1Same.hashCode())
        assertThat(rwNullLoc.hashCode()).isNotEqualTo(rw1.hashCode())
        assertThat(rw1.toString()).contains("Lait")
    }

    @Test
    fun `RecipeWithIngredients getters componentN equals copy hashCode and toString should reach 100 percent coverage`() {
        val recipe = Recipe(id = 10L, title = "Crêpes")
        val ingredients = listOf(RecipeIngredient(id = 1L, recipeId = 10L, ingredientName = "Farine"))

        val rwi1 = RecipeWithIngredients(recipe = recipe, ingredients = ingredients)
        val rwi1Same = RecipeWithIngredients(recipe = recipe, ingredients = ingredients)

        // Getters
        assertThat(rwi1.recipe).isEqualTo(recipe)
        assertThat(rwi1.ingredients).isEqualTo(ingredients)

        // Destructuring / componentN
        val (r, ings) = rwi1
        assertThat(r).isEqualTo(recipe)
        assertThat(ings).isEqualTo(ingredients)

        // Equals
        assertThat(rwi1.equals(rwi1)).isTrue()
        assertThat(rwi1.equals(null)).isFalse()
        assertThat(rwi1.equals("NotARecipeWithIngredients")).isFalse()
        assertThat(rwi1).isEqualTo(rwi1Same)

        // Copy discrepancies
        val recipe2 = Recipe(id = 20L, title = "Gaufres")
        assertThat(rwi1).isNotEqualTo(rwi1.copy(recipe = recipe2))
        assertThat(rwi1).isNotEqualTo(rwi1.copy(ingredients = emptyList()))

        // HashCode & ToString
        assertThat(rwi1.hashCode()).isEqualTo(rwi1Same.hashCode())
        assertThat(rwi1.toString()).contains("Crêpes")
    }
}
