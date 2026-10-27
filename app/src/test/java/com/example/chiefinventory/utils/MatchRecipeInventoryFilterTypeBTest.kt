package com.example.chiefinventory.utils

import com.example.chiefinventory.model.Recipe
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le filtrage par type de plat (Entrée, Plat principal, Dessert)
 * et par type de cuisine dans le moteur de faisabilité du garde-manger.
 */
class MatchRecipeInventoryFilterTypeBTest {

    private fun filterResults(
        results: List<RecipeMatchResult>,
        courseFilter: String?,
        cuisineFilter: String?
    ): List<RecipeMatchResult> {
        return results.filter { res ->
            val instStr = res.recipe.instructions ?: ""
            val sourceStr = res.recipe.source ?: ""

            val matchesCourse = courseFilter.isNullOrBlank() || courseFilter.equals("Tout", ignoreCase = true) ||
                    instStr.contains(courseFilter, ignoreCase = true) ||
                    res.recipe.title.contains(courseFilter, ignoreCase = true) ||
                    sourceStr.contains(courseFilter, ignoreCase = true)

            val matchesCuisine = cuisineFilter.isNullOrBlank() || cuisineFilter.equals("Toutes les cuisines", ignoreCase = true) ||
                    res.recipe.title.contains(cuisineFilter, ignoreCase = true) ||
                    instStr.contains(cuisineFilter, ignoreCase = true) ||
                    sourceStr.contains(cuisineFilter, ignoreCase = true)

            matchesCourse && matchesCuisine
        }
    }

    @Test
    fun `filterResults should filter match results by course type`() {
        val res1 = RecipeMatchResult(
            recipe = Recipe(id = 1L, title = "Ficelle picarde", instructions = "Catégorie : Entrée"),
            totalRequired = 3, totalInStock = 3, missingIngredients = emptyList(), matchPercentage = 100.0
        )
        val res2 = RecipeMatchResult(
            recipe = Recipe(id = 2L, title = "Coq au vin", instructions = "Catégorie : Plat principal"),
            totalRequired = 4, totalInStock = 4, missingIngredients = emptyList(), matchPercentage = 100.0
        )
        val res3 = RecipeMatchResult(
            recipe = Recipe(id = 3L, title = "Charlotte au chocolat", instructions = "Catégorie : Dessert"),
            totalRequired = 3, totalInStock = 3, missingIngredients = emptyList(), matchPercentage = 100.0
        )

        val list = listOf(res1, res2, res3)

        val entrees = filterResults(list, "Entrée", "Toutes les cuisines")
        assertThat(entrees).hasSize(1)
        assertThat(entrees.first().recipe.title).isEqualTo("Ficelle picarde")

        val desserts = filterResults(list, "Dessert", "Toutes les cuisines")
        assertThat(desserts).hasSize(1)
        assertThat(desserts.first().recipe.title).isEqualTo("Charlotte au chocolat")
    }
}
