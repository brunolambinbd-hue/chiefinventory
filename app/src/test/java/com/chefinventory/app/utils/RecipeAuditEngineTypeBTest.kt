package com.chefinventory.app.utils

import com.chefinventory.app.dao.RecipeWithIngredients
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.model.RecipeIngredient
import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("Le total de recettes auditées doit être de 4")
            .that(report.totalRecipes)
            .isEqualTo(4)

        assertWithMessage("Le nombre de recettes conformes doit être de 2")
            .that(report.validRecipesCount)
            .isEqualTo(2)

        assertWithMessage("La liste des recettes sans ingrédient doit compter 1 élément")
            .that(report.missingIngredientsList)
            .hasSize(1)

        assertWithMessage("Le titre de la recette sans ingrédient doit être 'Pintade à la normande'")
            .that(report.missingIngredientsList.first().recipe.title)
            .isEqualTo("Pintade à la normande")

        assertWithMessage("La liste des recettes sans instruction doit compter 1 élément")
            .that(report.missingInstructionsList)
            .hasSize(1)

        assertWithMessage("Le titre de la recette sans instruction doit être 'Xyz 123'")
            .that(report.missingInstructionsList.first().recipe.title)
            .isEqualTo("Xyz 123")

        assertWithMessage("La liste des recettes sans catégorie doit compter 1 élément")
            .that(report.missingCategoryList)
            .hasSize(1)

        assertWithMessage("Le titre de la recette sans catégorie doit être 'Xyz 123'")
            .that(report.missingCategoryList.first().recipe.title)
            .isEqualTo("Xyz 123")

        assertWithMessage("La liste des boissons doit compter 1 élément")
            .that(report.drinkRecipesList)
            .hasSize(1)

        assertWithMessage("Le titre de la boisson doit être 'Cocktail Embuscade'")
            .that(report.drinkRecipesList.first().recipe.title)
            .isEqualTo("Cocktail Embuscade")
    }
}
