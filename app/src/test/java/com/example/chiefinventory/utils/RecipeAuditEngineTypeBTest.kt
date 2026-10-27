package com.example.chiefinventory.utils

import com.example.chiefinventory.dao.RecipeWithIngredients
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le moteur d'audit de santé des recettes.
 */
class RecipeAuditEngineTypeBTest {

    @Test
    fun `auditRecipes should detect recipes with missing ingredients or missing instructions`() {
        val recipeValid = RecipeWithIngredients(
            recipe = Recipe(id = 1L, title = "Ficelle picarde", instructions = "Cuire les crêpes", locationId = 10L),
            ingredients = listOf(RecipeIngredient(recipeId = 1L, ingredientName = "farine"))
        )

        val recipeNoIngr = RecipeWithIngredients(
            recipe = Recipe(id = 2L, title = "Pintade à la normande", instructions = "Faire dorer", locationId = 10L),
            ingredients = emptyList() // 0 ingrédients !
        )

        val recipeNoInst = RecipeWithIngredients(
            recipe = Recipe(id = 3L, title = "Xyz 123", instructions = "", locationId = null),
            ingredients = listOf(RecipeIngredient(recipeId = 3L, ingredientName = "ail"))
        )

        val recipeDrink = RecipeWithIngredients(
            recipe = Recipe(id = 4L, title = "Cocktail Embuscade", instructions = "Mélanger avec des glaçons", locationId = 20L),
            ingredients = listOf(RecipeIngredient(recipeId = 4L, ingredientName = "calvados"))
        )

        val locationsMap = mapOf(10L to "Plat principal", 20L to "Boisson")
        val report = RecipeAuditEngine.auditRecipes(listOf(recipeValid, recipeNoIngr, recipeNoInst, recipeDrink), locationsMap)

        assertThat(report.totalRecipes).isEqualTo(4)
        assertThat(report.validRecipesCount).isEqualTo(2)

        assertThat(report.missingIngredientsList).hasSize(1)
        assertThat(report.missingIngredientsList.first().recipe.title).isEqualTo("Pintade à la normande")

        assertThat(report.missingInstructionsList).hasSize(1)
        assertThat(report.missingInstructionsList.first().recipe.title).isEqualTo("Xyz 123")

        assertThat(report.missingCategoryList).hasSize(1)
        assertThat(report.missingCategoryList.first().recipe.title).isEqualTo("Xyz 123")

        assertThat(report.drinkRecipesList).hasSize(1)
        assertThat(report.drinkRecipesList.first().recipe.title).isEqualTo("Cocktail Embuscade")
    }
}
