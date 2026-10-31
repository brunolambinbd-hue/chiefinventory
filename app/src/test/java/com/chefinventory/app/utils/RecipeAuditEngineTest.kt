package com.chefinventory.app.utils

import com.chefinventory.app.dao.RecipeWithIngredients
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

class RecipeAuditEngineTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAuditRecipes_FullCoverageOfCategoriesAndMissingFields() {
        val recipeValid = Recipe(id = 1L, title = "Crêpes", instructions = "Cuire à la poêle", locationId = 10L)
        val ingrValid = listOf(RecipeIngredient(recipeId = 1L, ingredientName = "Farine", quantityRequired = 250.0))
        val itemValid = RecipeWithIngredients(recipe = recipeValid, ingredients = ingrValid)

        val recipeNoIngr = Recipe(id = 2L, title = "Soupe", instructions = "Mijoter", locationId = null)
        val itemNoIngr = RecipeWithIngredients(recipe = recipeNoIngr, ingredients = emptyList())

        val recipeNoInst = Recipe(id = 3L, title = "Salade", instructions = "", locationId = null)
        val itemNoInst = RecipeWithIngredients(recipe = recipeNoInst, ingredients = listOf(RecipeIngredient(recipeId = 3L, ingredientName = "Laitue")))

        val recipeDrink = Recipe(id = 4L, title = "Cocktail Mojito", instructions = "Mélanger", locationId = null)
        val itemDrink = RecipeWithIngredients(recipe = recipeDrink, ingredients = listOf(RecipeIngredient(recipeId = 4L, ingredientName = "Rhum")))

        val recipeSide = Recipe(id = 5L, title = "Sauce Béarnaise", instructions = "Fouetter", locationId = null)
        val itemSide = RecipeWithIngredients(recipe = recipeSide, ingredients = listOf(RecipeIngredient(recipeId = 5L, ingredientName = "Beurre")))

        val locationMap = mapOf(10L to "Plat principal")

        val report = RecipeAuditEngine.auditRecipes(
            listOf(itemValid, itemNoIngr, itemNoInst, itemDrink, itemSide),
            locationMap
        )

        assertThat(report.totalRecipes).isEqualTo(5)
        assertThat(report.validRecipesCount).isEqualTo(3) // valid, drink, side
        assertThat(report.missingIngredientsList).containsExactly(itemNoIngr)
        assertThat(report.missingInstructionsList).containsExactly(itemNoInst)
        assertThat(report.missingCategoryList).containsExactly(itemNoIngr, itemNoInst)
        assertThat(report.drinkRecipesList).containsExactly(itemDrink)
        assertThat(report.sideRecipesList).containsExactly(itemSide)
    }
}
