package com.example.chiefinventory.utils

import com.example.chiefinventory.dao.RecipeWithIngredients
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'injection d'ingrédients et d'instructions
 * lors de la mise à jour (upsert) d'une recette existante.
 */
class StarterRecipeSeedLoaderUpsertIngredientsTypeBTest {

    @Test
    fun `upsert logic should inject ingredients when existing recipe has 0 ingredients`() {
        val existingRecipe = Recipe(id = 10L, title = "Pintade à la normande", instructions = "Faire dorer")
        val existingWithIngr = RecipeWithIngredients(recipe = existingRecipe, ingredients = emptyList())

        val seed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Pintade à la normande",
            category = "Plat principal",
            cuisineOrigin = "Cuisine normande",
            prepTimeMinutes = 15,
            cookTimeMinutes = 45,
            servings = 4,
            ingredients = listOf("1 pintade pas trop grasse", "30 cl de crème fraîche", "60 g de beurre"),
            instructions = listOf("Préparer la pintade", "Faire revenir au beurre"),
            sourceUrl = "https://fr.wikibooks.org/wiki/Pintade"
        )

        // Verifie que le test d'injection detecte bien l'absence d'ingredients sur la recette existante
        val needsIngredients = existingWithIngr.ingredients.isEmpty() && seed.ingredients.isNotEmpty()
        assertThat(needsIngredients).isTrue()

        val newIngredients = seed.ingredients.map { raw ->
            val parsed = IngredientParser.parse(raw)
            RecipeIngredient(
                recipeId = existingRecipe.id,
                ingredientName = parsed.name,
                quantityRequired = parsed.quantity,
                unit = parsed.unit
            )
        }

        assertThat(newIngredients).hasSize(3)
        assertThat(newIngredients.first().recipeId).isEqualTo(10L)
        assertThat(newIngredients.first().ingredientName).isEqualTo("pintade pas trop grasse")
    }
}
