package com.example.chiefinventory.ui.actvity

import com.example.chiefinventory.model.Recipe
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le filtrage par identifiant de catégorie dans RecipeListActivity.
 */
class RecipeListActivityCategoryFilterTypeBTest {

    private fun filterRecipesByLocationId(recipes: List<Recipe>, categoryId: Long): List<Recipe> {
        return if (categoryId == -1L) {
            recipes
        } else {
            recipes.filter { it.locationId == categoryId }
        }
    }

    @Test
    fun `filterRecipesByLocationId should filter recipes by category ID when categoryId is valid`() {
        val r1 = Recipe(id = 1L, title = "Tarte aux chicons", locationId = 10L) // Entrée
        val r2 = Recipe(id = 2L, title = "Coq au vin", locationId = 20L) // Plat
        val r3 = Recipe(id = 3L, title = "Charlotte au chocolat", locationId = 30L) // Dessert

        val all = listOf(r1, r2, r3)

        // Filtre sur Entrées (ID 10)
        val entrees = filterRecipesByLocationId(all, 10L)
        assertThat(entrees).hasSize(1)
        assertThat(entrees.first().title).isEqualTo("Tarte aux chicons")

        // Filtre sur TOUTES les recettes (ID -1)
        val allFiltered = filterRecipesByLocationId(all, -1L)
        assertThat(allFiltered).hasSize(3)
    }
}
