package com.chefinventory.app.utils

import com.chefinventory.app.dao.RecipeWithIngredients
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.model.RecipeIngredient
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

class RecipeAuditEngineTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAuditRecipes_SourceCategoryAndNullInstructions() {
        val recipeNullInst = Recipe(id = 6L, title = "Tarte", instructions = null, locationId = null, source = "Dessert (Grand-mère)")
        val itemNullInst = RecipeWithIngredients(recipe = recipeNullInst, ingredients = listOf(RecipeIngredient(recipeId = 6L, ingredientName = "Pommes")))

        val report = RecipeAuditEngine.auditRecipes(listOf(itemNullInst))

        assertWithMessage("Le nombre total de recettes auditées doit être de 1")
            .that(report.totalRecipes)
            .isEqualTo(1)

        assertWithMessage("La recette sans instruction doit figurer dans missingInstructionsList")
            .that(report.missingInstructionsList)
            .containsExactly(itemNullInst)

        assertWithMessage("La liste des catégories manquantes doit être vide car la source définit la catégorie")
            .that(report.missingCategoryList)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAuditRecipes_DefaultLocationNamesMapOverload() {
        val recipe = Recipe(id = 10L, title = "Cocktail Mojito", instructions = "Chauffer")
        val item = RecipeWithIngredients(recipe = recipe, ingredients = listOf(RecipeIngredient(recipeId = 10L, ingredientName = "Vin")))

        // Call auditRecipes with 1 argument (uses default parameter locationNamesMap = emptyMap())
        val report = RecipeAuditEngine.auditRecipes(listOf(item))

        assertWithMessage("Le total de recettes doit valoir 1")
            .that(report.totalRecipes)
            .isEqualTo(1)

        assertWithMessage("La recette de cocktail 'Cocktail Mojito' doit être classifiée dans drinkRecipesList")
            .that(report.drinkRecipesList)
            .containsExactly(item)
    }

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

        assertWithMessage("Le total des recettes auditées doit valoir 5")
            .that(report.totalRecipes)
            .isEqualTo(5)

        assertWithMessage("Le nombre de recettes conformes doit être de 3 (valide, boisson, accompagnement)")
            .that(report.validRecipesCount)
            .isEqualTo(3)

        assertWithMessage("La recette sans ingrédient doit figurer dans missingIngredientsList")
            .that(report.missingIngredientsList)
            .containsExactly(itemNoIngr)

        assertWithMessage("La recette sans instruction doit figurer dans missingInstructionsList")
            .that(report.missingInstructionsList)
            .containsExactly(itemNoInst)

        assertWithMessage("Les recettes sans catégorie doivent contenir itemNoIngr et itemNoInst")
            .that(report.missingCategoryList)
            .containsExactly(itemNoIngr, itemNoInst)

        assertWithMessage("La recette de boisson doit figurer dans drinkRecipesList")
            .that(report.drinkRecipesList)
            .containsExactly(itemDrink)

        assertWithMessage("La recette de sauce doit figurer dans sideRecipesList")
            .that(report.sideRecipesList)
            .containsExactly(itemSide)
    }
}
