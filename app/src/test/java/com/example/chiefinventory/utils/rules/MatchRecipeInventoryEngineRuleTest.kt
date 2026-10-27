package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.utils.MatchRecipeInventoryEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Niveau A vérifiant l'évaluation de la faisabilité des recettes
 * d'après le taux de couverture du stock du garde-manger.
 */
class MatchRecipeInventoryEngineRuleTest {

    @Test
    fun `evaluateRecipesFeasibility should rank 100 percent match recipe at the top`() {
        val recipe1 = Recipe(id = 1L, title = "Tarte aux chicons")
        val recipe2 = Recipe(id = 2L, title = "Roulade de jambon de Parme")

        val ingr1 = listOf(
            RecipeIngredient(recipeId = 1L, ingredientName = "chicons", quantityRequired = 1.0, unit = "kg"),
            RecipeIngredient(recipeId = 1L, ingredientName = "beurre", quantityRequired = 100.0, unit = "g")
        )

        val ingr2 = listOf(
            RecipeIngredient(recipeId = 2L, ingredientName = "jambon de Parme", quantityRequired = 2.0, unit = "paquets"),
            RecipeIngredient(recipeId = 2L, ingredientName = "melon", quantityRequired = 1.0, unit = "pièce")
        )

        val recipeIngredientsMap = mapOf(1L to ingr1, 2L to ingr2)

        val pantry = listOf(
            Ingredient(id = 10L, name = "Chicons / Endives", quantity = 1.0, unit = "kg"),
            Ingredient(id = 11L, name = "Beurre doux", quantity = 250.0, unit = "g"),
            Ingredient(id = 12L, name = "Jambon de Parme", quantity = 0.0, unit = "paquets") // Out of stock
        )

        val matches = MatchRecipeInventoryEngine.evaluateRecipesFeasibility(
            recipes = listOf(recipe1, recipe2),
            recipeIngredientsMap = recipeIngredientsMap,
            pantryIngredients = pantry
        )

        assertThat(matches).hasSize(2)

        // Recipe 1 (Tarte aux chicons) has 2/2 ingredients in stock -> 100%
        val topMatch = matches.first()
        assertThat(topMatch.recipe.title).isEqualTo("Tarte aux chicons")
        assertThat(topMatch.matchPercentage).isEqualTo(100.0)
        assertThat(topMatch.missingIngredients).isEmpty()

        // Recipe 2 (Roulade de jambon) has 0/2 ingredients in stock -> 0%
        val secondMatch = matches[1]
        assertThat(secondMatch.recipe.title).isEqualTo("Roulade de jambon de Parme")
        assertThat(secondMatch.matchPercentage).isEqualTo(0.0)
        assertThat(secondMatch.missingIngredients.first()).contains("jambon de Parme")
    }
}
